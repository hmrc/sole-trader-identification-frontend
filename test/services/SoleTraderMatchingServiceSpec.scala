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

import connectors.mocks.{MockAuthenticatorConnector, MockRetrieveKnownFactsConnector}
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.KnownFactsResponse
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderMatchingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderMatchingServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockAuthenticatorConnector
    with MockRetrieveKnownFactsConnector
    with MockSoleTraderIdentificationService {

  object TestService extends SoleTraderMatchingService(mockAuthenticatorConnector, mockRetrieveKnownFactsConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "matchSoleTraderDetails" should {
    "return SuccessfulMatch" when {
      "the provided details match those from authenticator" when {
        "the enableSautrCheck is true and the sautr matches the returned one" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetails)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe SuccessfulMatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetailsNoSautr)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe SuccessfulMatch

          verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
        }
      }
    }

    "return DetailsMismatch" when {
      "the provided details do not match those from authenticator" when {
        "the enableSautrCheck is true and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }

        "the enableSautrCheck is false and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }
      }

      "the provided sautr does not exist on authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetailsNoSautr)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

        result mustBe DetailsMismatch

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
      }

      "the user has not provided an sautr but one is returned from authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig(enableSautrCheck = true)))

        result mustBe DetailsMismatch

        verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
      }
    }

    "return NinoNotFound" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(NinoNotFound)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe NinoNotFound

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
      }
    }

    "return DeceasedCitizensDetails" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DeceasedCitizensDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe DeceasedCitizensDetails

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
      }
    }
  }

  "matchSoleTraderDetailsNoNino" should {
    "return SuccessfulMatch" when {
      "the user's postcode matches the postcode returned from ES20" when {
        "the postcode's are provided in the same format" in {
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFactsResponse(Some(testSaPostcode), None, None)))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
          mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

          result mustBe SuccessfulMatch

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
        "the postcode's are provided in different formats" in {
          val testPostcode: String = "aa1 1aa"
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFactsResponse(Some(testSaPostcode), None, None)))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
          mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

          result mustBe SuccessfulMatch

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
      }
      "the user does not provide a postcode and isAbroad is true" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFactsResponse(Some(testSaPostcode), Some(true), None)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), Some(true), None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe SuccessfulMatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
      }
    }
    "return NotEnoughInformationToMatch" when {
      "the user does not have an sautr" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr))

        result mustBe NotEnoughInformationToMatch

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }
    "return DetailsMismatch" when {
      "the user's postcode does not match the postcode returned from ES20" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFactsResponse(Some("TF4 3ER"), None, None)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(Some("TF4 3ER"), None, None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe DetailsMismatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
      "the user does not provide a postcode but isAbroad is false" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFactsResponse(None, Some(false), None)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(None, Some(false), None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe DetailsMismatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }
  }
}
