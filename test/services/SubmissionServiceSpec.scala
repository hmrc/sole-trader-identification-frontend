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
import org.mockito.Mockito.reset
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.{MockCreateTrnService, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.{BusinessVerificationJourneyCreated, NotEnoughEvidence}
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, EnableNoNinoJourney => EnableOptionalNinoJourney}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DetailsMismatch, NinoNotFound}
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

      s"register Without Business Verification" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, journeyConfigWithoutBV)(Future.successful(Right(true)))
        mockRegisterWithoutBusinessVerification(testJourneyId, testIndividualDetails.optNino, testIndividualDetails.optSautr.get)(Future.successful(Registered(testSafeId)))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)

      }
    }

    "the user has no sautr" should {
      s"not be registered (matching does not matter)" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        List(true, false).foreach(matchingTrueOrFalse => {

          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, journeyConfigWithoutBV)(Future.successful(Right(matchingTrueOrFalse)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

          result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)

          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)

          resetAllMocks()
        })

      }
    }

    "the user has no Nino (possible only when EnableOptionalNinoJourney is true)" should {

      s"not register but create a trn (matching does not matter)" in {

        enable(EnableOptionalNinoJourney)

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        List(true, false).foreach(matchingTrueOrFalse => {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(Right(matchingTrueOrFalse)))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

          result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)

          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)

          verifyCreateTrn(testJourneyId)
          resetAllMocks()
        })

      }
    }

    "the user has no Nino (possible only with EnableOptionalNinoJourney set to true) and EnableOptionalNinoJourney set to false" should {

      s"not be possible and throw an exception" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))

        val exception = intercept[IllegalStateException](await(TestService.submit(testJourneyId, journeyConfigWithoutBV)))

        exception.getMessage mustBe "This cannot be because Nino is empty and EnableOptionalNinoJourney is false"

      }
    }

    "there is a matching error" should {
      s"return a SoleTraderDetailsMismatch" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, journeyConfigWithoutBV)(Future.successful(Left(DetailsMismatch)))
        mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe SoleTraderDetailsMismatch(DetailsMismatch)

        verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
      }
    }

    "there is no matching" should {
      s"return a JourneyCompleted" in {

        val journeyConfigWithoutBV = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)

        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, journeyConfigWithoutBV)(Future.successful(Right(false)))
        mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

        val result = await(TestService.submit(testJourneyId, journeyConfigWithoutBV))

        result mustBe JourneyCompleted(journeyConfigWithoutBV.continueUrl)

        verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
      }
    }

  }

  "submit" when {
    "for sole trader journey: the user has a nino and sautr" should {
      s"return StartBusinessVerification($testBusinessVerificationRedirectUrl)" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true))(Future.successful(Right(true)))
        mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Right(BusinessVerificationJourneyCreated(testBusinessVerificationRedirectUrl))))

        val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe StartBusinessVerification(testBusinessVerificationRedirectUrl)
      }
      "return JourneyCompleted" when {
        "Business Verification Journey Creation fails" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(Right(true)))
          mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Left(NotEnoughEvidence)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
        "no sautr is provided" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testSoleTraderJourneyConfig)(Future.successful(Right(true)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
      }
      "return SoleTraderDetailsMismatch" when {
        "the details received from Authenticator do not match" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(Left(DetailsMismatch)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe SoleTraderDetailsMismatch(DetailsMismatch)
        }
        "the nino is not found on Authenticator" in {
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testSoleTraderJourneyConfig)(Future.successful(Left(NinoNotFound)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
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
        mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(Right(true)))
        mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Right(BusinessVerificationJourneyCreated(testBusinessVerificationRedirectUrl))))

        val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe StartBusinessVerification(testBusinessVerificationRedirectUrl)
      }
      "return JourneyCompleted" when {
        "Business Verification Journey Creation fails" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(Right(true)))
          mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Left(NotEnoughEvidence)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)

        }
        "no sautr is provided" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(Right(true)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
        }
      }

      "return SoleTraderDetailsMismatch" when {
        "the details received from ES20 do not match" in {
          enable(EnableOptionalNinoJourney)
          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino)(Future.successful(Left(DetailsMismatch)))
          mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          val result = await(TestService.submit(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe SoleTraderDetailsMismatch(DetailsMismatch)
        }
      }
    }

    "for individual journey: the user has no Nino (possible only with EnableOptionalNinoJourney set to true) and matching true or false" should {
      "return JourneyCompleted" in {
        enable(EnableOptionalNinoJourney)

        List(true, false).foreach(matchingTrueOrFalse => {

          mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNinoNoSautr)))
          mockMatchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr)(Future.successful(Right(matchingTrueOrFalse)))

          val result = await(TestService.submit(testJourneyId, TestConstants.testIndividualJourneyConfig))

          result mustBe JourneyCompleted(testContinueUrl)
          resetAllMocks()
        })
      }
    }

    "for individual journey: the user has No Nino (possible only with EnableOptionalNinoJourney set to true) and EnableOptionalNinoJourney set to false" should {
      "be not possible and throws an exception" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))

        val exception = intercept[IllegalStateException](await(TestService.submit(testJourneyId, TestConstants.testIndividualJourneyConfig)))

        exception.getMessage mustBe "This cannot be because Nino is empty and EnableOptionalNinoJourney is false"

      }
    }

    "for individual journey: the user's details do not match" should {
      "return a failed submission response" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
        mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testIndividualJourneyConfig)(Future.successful(Left(DetailsMismatch)))

        val result: SubmissionResponse = await(TestService.submit(testJourneyId, testIndividualJourneyConfig))

        result mustBe SoleTraderDetailsMismatch(DetailsMismatch)

        verifyStoreBusinessVerificationStatus(0)
        verifyStoreRegistrationResponse(0)
      }
    }

  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    disable(EnableOptionalNinoJourney)
  }

  def resetAllMocks(): Unit =
    reset(
      mockSoleTraderMatchingService,
      mockSoleTraderIdentificationService,
      mockBusinessVerificationService,
      mockCreateTrnService,
      mockRegistrationOrchestrationService
    )

  object TestService extends SubmissionService(
    mockSoleTraderMatchingService,
    mockSoleTraderIdentificationService,
    mockBusinessVerificationService,
    mockCreateTrnService,
    mockRegistrationOrchestrationService
  )
}

