/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.mocks.{MockCreateNinoIVJourneyConnector, MockRetrieveNinoIVStatusConnector}
import helpers.TestConstants.{testContinueUrl, testJourneyId, testNino, testSoleTraderJourneyConfig}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateNinoIVJourneyConnector.{JourneyCreated, NotEnoughEvidence, UserLockedOut}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationNotEnoughInformationToChallenge, BusinessVerificationPass}
import uk.gov.hmrc.soletraderidentificationfrontend.services.NinoIVService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class NinoIVServiceSpec extends AnyWordSpec
  with Matchers
  with MockSoleTraderIdentificationService
  with MockCreateNinoIVJourneyConnector
  with MockRetrieveNinoIVStatusConnector {

  object TestService extends NinoIVService(mockCreateNinoIVJourneyConnector, mockRetrieveNinoIVStatusConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "createNinoIVJourney" should {
    "return JourneyCreated" in {
      mockCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)(Future.successful(Right(JourneyCreated(testContinueUrl))))

      val result = await(TestService.createNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

      result mustBe Right(JourneyCreated(testContinueUrl))

      verifyCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)
    }
    "return and store NotEnoughEvidence" in {
      mockCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)(Future.successful(Left(NotEnoughEvidence)))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToChallenge)(Future.successful(SuccessfullyStored))

      val result = await(TestService.createNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

      result mustBe Left(NotEnoughEvidence)

      verifyCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)
      verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToChallenge)
    }
    "return and store UserLockedOut" in {
      mockCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)(Future.successful(Left(UserLockedOut)))
      mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(Future.successful(SuccessfullyStored))

      val result = await(TestService.createNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

      result mustBe Left(UserLockedOut)

      verifyCreateNinoIVJourney(testJourneyId, testNino, testSoleTraderJourneyConfig)
      verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)
    }
  }

  "retrieveNinoIVStatus" should {
    "return the IV status" in {
      mockRetrieveNinoIVStatus(testJourneyId)(Future.successful(BusinessVerificationPass))

      val result = await(TestService.retrieveNinoIVStatus(testJourneyId))

      result mustBe BusinessVerificationPass

      verifyRetrieveNinoIVStatus(testJourneyId)
    }
  }

}
