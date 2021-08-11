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

import connectors.mocks.MockAuthenticatorConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DeceasedCitizensDetails, DetailsMismatch, NinoNotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderMatchingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderMatchingServiceSpec extends AnyWordSpec with Matchers with MockAuthenticatorConnector with MockSoleTraderIdentificationService {

  object TestService extends SoleTraderMatchingService(mockAuthenticatorConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "matchSoleTraderDetails" should {
    "return Right(IndividualDetails)" when {
      "the provided details match those from authenticator" when {
        "the enableSautrCheck is true and the sautr matches the returned one" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetails)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe Right(testIndividualDetails)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetailsNoSautr)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe Right(testIndividualDetailsNoSautr)
        }
      }
    }

    "return Left(Mismatch)" when {
      "the provided details do not match those from authenticator" when {
        "the enableSautrCheck is true and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe Left(DetailsMismatch)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe Left(DetailsMismatch)
        }

        "the enableSautrCheck is false and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

          result mustBe Left(DetailsMismatch)
        }
      }

      "the provided sautr does not exist on authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetailsNoSautr)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

        result mustBe Left(DetailsMismatch)
      }

      "the user has not provided an sautr but one is returned from authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig(enableSautrCheck = true)))

        result mustBe Left(DetailsMismatch)
      }
    }

    "return Left(NotFound)" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(NinoNotFound)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe Left(NinoNotFound)
      }
    }

    "return Left(Deceased)" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DeceasedCitizensDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe Left(DeceasedCitizensDetails)
      }
    }
  }

}
