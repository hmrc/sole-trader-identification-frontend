/*
 * Copyright 2021 HM Revenue & Customs
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

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.{MockAuditService, MockBusinessVerificationService, MockJourneyService, MockSoleTraderIdentificationService, MockSoleTraderMatchingService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.{BusinessVerificationJourneyCreated, NotEnoughEvidence}
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
    with MockAuditService {

  object TestService extends SubmissionService(mockJourneyService, mockSoleTraderMatchingService, mockSoleTraderIdentificationService, mockBusinessVerificationService, mockAuditService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "submit" should {
    s"return StartBusinessVerification($testBusinessVerificationRedirectUrl)" in {
      mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(true)))
      mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
      mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(true))(Future.successful(Right(testIndividualDetails)))
      mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
      mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Right(BusinessVerificationJourneyCreated(testBusinessVerificationRedirectUrl))))

      val result = await(TestService.submit(testJourneyId))

      result mustBe StartBusinessVerification(testBusinessVerificationRedirectUrl)
    }
  }
  "return JourneyCompleted" when {
    "Business Verification Journey Creation fails" in {
      mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(true)))
      mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
      mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(true))(Future.successful(Right(testIndividualDetails)))
      mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
      mockCreateBusinessVerificationJourney(testJourneyId, testSautr)(Future.successful(Left(NotEnoughEvidence)))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      val result = await(TestService.submit(testJourneyId))

      result mustBe JourneyCompleted(testContinueUrl)
    }
    "no sautr is provided" in {
      mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(true)))
      mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
      mockMatchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig(true))(Future.successful(Right(testIndividualDetailsNoSautr)))
      mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      val result = await(TestService.submit(testJourneyId))

      result mustBe JourneyCompleted(testContinueUrl)
    }
  }
  "return SoleTraderDetailsMismatch" when {
    "the details received from Authenticator do not match" in {
      mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(true)))
      mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
      mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(true))(Future.successful(Left(DetailsMismatch)))
      mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      val result = await(TestService.submit(testJourneyId))

      result mustBe SoleTraderDetailsMismatch(DetailsMismatch)
    }
    "the nino is not found on Authenticator" in {
      mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(true)))
      mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
      mockMatchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(true))(Future.successful(Left(NinoNotFound)))
      mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
      mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

      val result = await(TestService.submit(testJourneyId))

      result mustBe SoleTraderDetailsMismatch(NinoNotFound)
    }
  }

}
