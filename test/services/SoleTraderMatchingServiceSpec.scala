/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.Mockito.reset
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.{KnownFactsNoContentError, KnownFactsResponse}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderMatchingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderMatchingServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockAuthenticatorConnector
    with MockRetrieveKnownFactsConnector
    with MockSoleTraderIdentificationService
    with org.scalatest.prop.TableDrivenPropertyChecks {

  object TestService
      extends SoleTraderMatchingService(mockAuthenticatorConnector, mockRetrieveKnownFactsConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "matchSoleTraderDetails" should {
    "return SuccessfulMatch" when {
      "the provided details match those from authenticator" when {
        "the enableSautrCheck is true and the sautr matches the returned one" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetails)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe SuccessfulMatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)
          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
        }
        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetailsNoSautr)))
          mockStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe SuccessfulMatch

          verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
          verifyStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)
          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
        }
      }
    }

    "return DetailsMismatch" when {
      "the provided details do not match those from authenticator" when {
        "the enableSautrCheck is true and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }
        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig()))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
          verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }
        "the enableSautrCheck is false and the sautr is provided" in {
          mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DetailsMismatch)))
          mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
          mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

          result mustBe DetailsMismatch

          verifyMatchSoleTraderDetails(testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
          verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        }
      }

      "the provided sautr does not exist on authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Right(testIndividualDetailsNoSautr)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig(enableSautrCheck = true)))

        result mustBe DetailsMismatch

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
      }

      "the user has not provided an sautr but one is returned from authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetailsNoSautr)(Future.successful(Right(testIndividualDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))

        val result =
          await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetailsNoSautr, testJourneyConfig(enableSautrCheck = true)))

        result mustBe DetailsMismatch

        verifyMatchSoleTraderDetails(testIndividualDetailsNoSautr)
        verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
      }
    }

    "return NinoNotFound" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(NinoNotFound)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, NinoNotFound)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe NinoNotFound

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, NinoNotFound)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
      }
    }

    "return DeceasedCitizensDetails" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testIndividualDetails)(Future.successful(Left(DeceasedCitizensDetails)))
        mockStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(Future.successful(SuccessfullyStored))
        mockStoreIdentifiersMatch(testJourneyId, DeceasedCitizensDetails)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetails(testJourneyId, testIndividualDetails, testJourneyConfig()))

        result mustBe DeceasedCitizensDetails

        verifyMatchSoleTraderDetails(testIndividualDetails)
        verifyStoreIdentifiersMatch(testJourneyId, DeceasedCitizensDetails)
        verifyStoryAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
      }
    }

  }

  "matchSoleTraderDetailsNoNino" should {
    "return NinoNotDeclaredButFound" when {
      "the user declares no NINO but retrieveKnownFacts find one" in {
        val knownFactsResponseScenarios: TableFor2[Option[String], Option[Boolean]] =
          Table(
            ("postcode", "isAbroad"),
            (Some(testSaPostcode), Some(true)),
            (Some(testSaPostcode), Some(false)),
            (Some(testSaPostcode), None),
            (None, Some(true)),
            (None, Some(false)),
            (None, None)
          )

        forAll(knownFactsResponseScenarios) { (optPostCode: Option[String], optIsAbroad: Option[Boolean]) =>
          {

            val incomingKnownFactsResponse = KnownFactsResponse(optPostCode, optIsAbroad, nino = Some(testKnownFactsRecordedNino))

            mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
            mockRetrieveKnownFacts(testSautr)(Future.successful(Right(incomingKnownFactsResponse)))
            mockStoreIdentifiersMatch(testJourneyId, NinoNotDeclaredButFound)(Future.successful(SuccessfullyStored))
            mockStoreES20Details(testJourneyId, incomingKnownFactsResponse)(Future.successful(SuccessfullyStored))

            val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

            result mustBe NinoNotDeclaredButFound

            verifyRetrieveKnownFacts(testSautr)
            verifyStoreIdentifiersMatch(testJourneyId, NinoNotDeclaredButFound)

            // Reset mock classes separately to suppress compiler warning
            reset(mockRetrieveKnownFactsConnector)
            reset(mockSoleTraderIdentificationService)
          }
        }
      }
    }
    "return SuccessfulMatch" when {
      "the user's postcode matches the postcode returned from ES20" when {
        "the postcode's are provided in the same format" in {
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(Right(KnownFactsResponse(Some(testSaPostcode), None, None))))
          mockStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(Future.successful(SuccessfullyStored))
          mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

          result mustBe SuccessfulMatch

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)
        }
        "the postcode's are provided in different formats" in {
          val testPostcode: String = "aa1 1aa"
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(Right(KnownFactsResponse(Some(testSaPostcode), None, None))))
          mockStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(Future.successful(SuccessfullyStored))
          mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

          result mustBe SuccessfulMatch

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)
        }
      }
      "the user does not provide a postcode and isAbroad is true" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(Right(KnownFactsResponse(Some(testSaPostcode), Some(true), None))))
        mockStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), Some(true), None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe SuccessfulMatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)
      }
    }
    "return NotEnoughInformationToMatch" when {
      "the user does not have an sautr" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
        mockStoreIdentifiersMatch(testJourneyId, NotEnoughInformationToMatch)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNinoNoSautr))

        result mustBe NotEnoughInformationToMatch

        verifyStoreIdentifiersMatch(testJourneyId, NotEnoughInformationToMatch)
      }
    }
    "return DetailsMismatch" when {
      "the user's postcode does not match the postcode returned from ES20" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
        mockRetrieveKnownFacts(testSautr)(Future.successful(Right(KnownFactsResponse(Some("TF4 3ER"), None, None))))
        mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(Some("TF4 3ER"), None, None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe DetailsMismatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
      }
      "the user does not provide a postcode but isAbroad is false" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(Right(KnownFactsResponse(None, Some(false), None))))
        mockStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(Future.successful(SuccessfullyStored))
        mockStoreES20Details(testJourneyId, KnownFactsResponse(None, Some(false), None))(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe DetailsMismatch

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, DetailsMismatch)
      }
    }
    "return KnownFactsNoContent" when {
      "the ES20 service cannot find the user's details" in {
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(Left(KnownFactsNoContentError)))
        mockStoreIdentifiersMatch(testJourneyId, KnownFactsNoContent)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchSoleTraderDetailsNoNino(testJourneyId, testIndividualDetailsNoNino))

        result mustBe KnownFactsNoContent

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, KnownFactsNoContent)
      }
    }
  }
}
