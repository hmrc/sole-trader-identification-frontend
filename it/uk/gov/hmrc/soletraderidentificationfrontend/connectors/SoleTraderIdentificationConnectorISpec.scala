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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors


import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.{TableFor2, Tables}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, JsValidationException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.SoleTraderIdentificationStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

import java.time.LocalDate

class SoleTraderIdentificationConnectorISpec extends ComponentSpecHelper with SoleTraderIdentificationStub {

  private val soleTraderIdentificationConnector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val DateOfBirthKey = "dateOfBirth"
  val FullNameKey = "fullName"
  val NinoKey = "nino"
  val SautrKey = "sautr"


  s"retrieveSoleTraderDetails($testJourneyId)" should {
    "return all possible values of SoleTraderDetailsMatchResult" in {
      val identifiersMatchScenarios: TableFor2[String, SoleTraderDetailsMatchResult] =
        Tables.Table(
          ("identifiersMatchStored", "identifiersMatchRetrieved"),
          ("SuccessfulMatch", SuccessfulMatch),
          ("NotEnoughInformationToMatch", NotEnoughInformationToMatch),
          ("DetailsMismatch", DetailsMismatch),
          ("NinoNotDeclaredButFound", NinoNotDeclaredButFound),
          ("DeceasedCitizensDetails", DeceasedCitizensDetails)
        )

      forAll(identifiersMatchScenarios) { (identifiersMatchStored: String, identifiersMatchRetrieved: SoleTraderDetailsMatchResult) => {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJson ++ Json.obj("identifiersMatch" -> identifiersMatchStored)
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails(testJourneyId))

        result mustBe Some(testSoleTraderDetails.copy(identifiersMatch = identifiersMatchRetrieved))
      }
      }
    }

    "throws an exception if SoleTraderDetailsMatchResult cannot be parsed correctly" in {
      stubRetrieveSoleTraderDetails(testJourneyId)(
        status = OK,
        body = testSoleTraderDetailsJson ++ Json.obj("identifiersMatch" -> "crappy value")
      )

      val exception = intercept[JsValidationException](await(soleTraderIdentificationConnector.retrieveSoleTraderDetails(testJourneyId)))
      exception.getMessage() must include("Error trying to match Sole Trader Matching Result. crappy value is not mapped to any match result")

    }

    "return None" when {
      "there is no SoleTraderDetails stored against the journeyId" in {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = NOT_FOUND
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails(testJourneyId))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderDetails($testJourneyId, $FullNameKey)" should {
    "return full name" when {
      "the full name key is given and a full name is stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        val testJson = Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        )
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsObject](testJourneyId, FullNameKey))

        result mustBe Some(testJson)
      }
    }

    "return None" when {
      "the firstName key is given but there is no first name stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsString](testJourneyId, FullNameKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderDetails($testJourneyId, $DateOfBirthKey)" should {
    "return date of birth" when {
      "the date of birth key is given and date of birth is stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[LocalDate](testJourneyId, DateOfBirthKey))

        result mustBe Some(testDateOfBirth)
      }
    }

    "return None" when {
      "the date of birth key is given but there is no date of birth stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[LocalDate](testJourneyId, DateOfBirthKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderDetails($testJourneyId, $NinoKey)" should {
    "return nino" when {
      "the nino key is given and nino is stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(OK, testNino)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsString](testJourneyId, NinoKey))

        result mustBe Some(JsString(testNino))
      }
    }

    "return None" when {
      "the nino key is given but there is no nino stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsString](testJourneyId, NinoKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderDetails($testJourneyId, $SautrKey)" should {
    "return sautr" when {
      "the sautr key is given and sautr is stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsString](testJourneyId, SautrKey))

        result mustBe Some(JsString(testSautr))
      }
    }

    "return None" when {
      "the sautr key is given but there is no sautr stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderDetails[JsString](testJourneyId, SautrKey))

        result mustBe None
      }
    }
  }

  s"storeData($testJourneyId, $FullNameKey)" should {
    "return SuccessfullyStored" in {
      stubStoreFullName(testJourneyId, FullName(testFirstName, testLastName))(status = OK)
      val result = await(soleTraderIdentificationConnector.storeData[FullName](
        testJourneyId, FullNameKey, FullName(testFirstName, testLastName)))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $DateOfBirthKey)" should {
    "return SuccessfullyStored" in {
      stubStoreDob(testJourneyId, testDateOfBirth)(status = OK)

      val result = await(soleTraderIdentificationConnector.storeData[LocalDate](testJourneyId, DateOfBirthKey, testDateOfBirth))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $NinoKey)" should {
    "return SuccessfullyStored" in {
      stubStoreNino(testJourneyId, testNino)(status = OK)

      val result = await(soleTraderIdentificationConnector.storeData[String](testJourneyId, NinoKey, testNino))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $SautrKey)" should {
    "return SuccessfullyStored" in {
      stubStoreSautr(testJourneyId, testSautr)(status = OK)
      val result = await(soleTraderIdentificationConnector.storeData[String](testJourneyId, SautrKey, testSautr))

      result mustBe SuccessfullyStored
    }
  }

  s"removeSoleTraderDetails($testJourneyId, $SautrKey)" should {
    "return SuccessfullyRemoved" when {
      "the sautr successfully removed from the database" in {
        stubRemoveSautr(testJourneyId)(NO_CONTENT)
        val result = await(soleTraderIdentificationConnector.removeSoleTraderDetails(testJourneyId, SautrKey))

        result mustBe SuccessfullyRemoved
      }
    }

    "throw an exception" when {
      "the sautr could not be deleted" in {
        stubRemoveSautr(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        intercept[InternalServerException] {
          await(soleTraderIdentificationConnector.removeSoleTraderDetails(testJourneyId, SautrKey))
        }
      }
    }
  }
}