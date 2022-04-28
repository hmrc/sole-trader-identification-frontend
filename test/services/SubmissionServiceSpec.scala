/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import helpers.TestConstants
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.{BusinessVerificationJourneyCreated, NotEnoughEvidence}
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, EnableNoNinoJourney => EnableOptionalNinoJourney}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DetailsMismatch, NinoNotFound, NotEnoughInformationToMatch, SuccessfulMatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services.SubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockSoleTraderIdentificationService
    with MockJourneyService
    with MockSoleTraderMatchingService
    with MockBusinessVerificationService
    with MockCreateTrnService
    with MockRegistrationOrchestrationService
    with FeatureSwitching {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "submit: sole trader journey with businessVerificationCheck = false" when {

    "the user has a sautr" should {
      "register Without Business Verification" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, journeyConfigWithoutBV)(Future.successful(SuccessfulMatch))
        mockRegisterWithoutBusinessVerification(testJourneyId, testIndividualDetails.optNino, testIndividualDetails.optSautr, journeyConfigWithoutBV)(Future.successful(Registered(testSafeId)))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)
      }
    }

    "the user has no sautr" should {
      "register and return JourneyCompleted" in {
        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, journeyConfigWithoutBV)(Future.successful(SuccessfulMatch))
        mockRegisterWithoutBusinessVerification(testJourneyId, Some(testNino), None, journeyConfigWithoutBV)(Future.successful(Registered(testSafeId)))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)
      }
    }

    "the user has no Nino and no Sautr" should {
      "return JourneyCompleted and create a trn" in {
        enable(EnableOptionalNinoJourney)

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
        mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(NotEnoughInformationToMatch))
        mockCreateTrn(testJourneyId)(Future.successful(testTrn))
        mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)

        verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)

        verifyCreateTrn(testJourneyId)
      }
    }

    "the user has no Nino (possible only with EnableOptionalNinoJourney set to true) and EnableOptionalNinoJourney set to false" should {
      "not be possible and throw an exception" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))

        val exception = intercept[IllegalStateException](await(TestService.submit(testJourneyId, journeyConfigWithoutBV)))

        exception.getMessage mustBe "[Submission Service] Unexpected state of Nino"
      }
    }

    "there is a matching error" should {
      "return a SoleTraderDetailsMismatch" in {
        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, journeyConfigWithoutBV)(Future.successful(DetailsMismatch))
        mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe SoleTraderDetailsMismatch(DetailsMismatch)

        verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
      }
    }
  }

  "submit: sole trader journey with businessVerificationCheck = true" when {
    "for sole trader journey: the user has a nino and sautr" should {
      s"return StartBusinessVerification($testBusinessVerificationRedirectUrl)" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true))(Future.successful(SuccessfulMatch))
        mockCreateBusinessVerificationJourney(testJourneyId, testSautr, testSoleTraderJourneyConfig)(Future.successful(Right(BusinessVerificationJourneyCreated(testBusinessVerificationRedirectUrl))))

        val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe StartBusinessVerification(testBusinessVerificationRedirectUrl)
      }
      "return JourneyCompleted" when {
        "Business Verification Journey Creation fails" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(SuccessfulMatch))
          mockCreateBusinessVerificationJourney(testJourneyId, testSautr, testSoleTraderJourneyConfig)(Future.successful(Left(NotEnoughEvidence)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToChallenge)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
        "no sautr is provided" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testSoleTraderJourneyConfig)(Future.successful(SuccessfulMatch))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
          mockRegisterWithoutBusinessVerification(testJourneyId, Some(testNino), None, testSoleTraderJourneyConfig)(Future.successful(Registered(testSafeId)))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
      }
      "return SoleTraderDetailsMismatch" when {
        "the details received from Authenticator do not match" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(DetailsMismatch))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe SoleTraderDetailsMismatch(DetailsMismatch)
        }
        "the nino is not found on Authenticator" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(NinoNotFound))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe SoleTraderDetailsMismatch(NinoNotFound)
        }
      }
    }

    "for sole trader journey: the user does not have a nino" should {
      s"return StartBusinessVerification($testBusinessVerificationRedirectUrl)" in {
        enable(EnableOptionalNinoJourney)
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
        mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(SuccessfulMatch))
        mockCreateBusinessVerificationJourney(testJourneyId, testSautr, testSoleTraderJourneyConfig)(Future.successful(Right(BusinessVerificationJourneyCreated(testBusinessVerificationRedirectUrl))))

        val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe StartBusinessVerification(testBusinessVerificationRedirectUrl)
      }
      "return JourneyCompleted" when {
        "Business Verification Journey Creation fails" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(SuccessfulMatch))
          mockCreateBusinessVerificationJourney(testJourneyId, testSautr, testSoleTraderJourneyConfig)(Future.successful(Left(NotEnoughEvidence)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
        "no sautr is provided" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(NotEnoughInformationToMatch))
          mockStoreIdentifiersMatch(testJourneyId, NotEnoughInformationToMatch)(Future.successful(SuccessfullyStored))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
        }
      }

      "return SoleTraderDetailsMismatch" when {
        "the details received from ES20 do not match" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(DetailsMismatch))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe SoleTraderDetailsMismatch(DetailsMismatch)

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
        }
      }
    }

    "for sole trader journey: the user does not have a sautr" should {
      "return JourneyCompleted" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testSoleTraderJourneyConfig)(Future.successful(SuccessfulMatch))
        mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(Future.successful(SuccessfullyStored))
        mockRegisterWithoutBusinessVerification(testJourneyId, Some(testNino), None, testSoleTraderJourneyConfig)(Future.successful(Registered(testSafeId)))

        val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe JourneyCompleted(testContinueUrl)

        verifyMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testSoleTraderJourneyConfig)
        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
        verifyRegisterWithoutBusinessVerification(testJourneyId, Some(testNino), None, testSoleTraderJourneyConfig)
      }
    }

    "for individual journey: the user has no Nino (possible only with EnableOptionalNinoJourney set to true) and matching true or false" should {
      "return JourneyCompleted" in {
        enable(EnableOptionalNinoJourney)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
        mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(NotEnoughInformationToMatch))

        val result = await(TestService.submit(testJourneyId, TestConstants.testIndividualJourneyConfig))

        result mustBe JourneyCompleted(testContinueUrl)
      }
    }

    "for individual journey: the user has No Nino and the EnableOptionalNinoJourney FS set to false" should {
      "be not possible and throws an exception" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))

        val exception = intercept[IllegalStateException](await(TestService.submit(testJourneyId, TestConstants.testIndividualJourneyConfig)))

        exception.getMessage mustBe "[Submission Service] Unexpected state of Nino"

      }
    }

    "for individual journey: the user's details do not match" should {
      "return a failed submission response" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testIndividualJourneyConfig)(Future.successful(DetailsMismatch))

        val result: SubmissionResponse = await(TestService.submit(testJourneyId, testIndividualJourneyConfig))

        result mustBe SoleTraderDetailsMismatch(DetailsMismatch)
      }
    }

  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    disable(EnableOptionalNinoJourney)
  }

  object TestService extends SubmissionService(
    mockSoleTraderMatchingService,
    mockSoleTraderIdentificationService,
    mockBusinessVerificationService,
    mockCreateTrnService,
    mockRegistrationOrchestrationService
  )
}
