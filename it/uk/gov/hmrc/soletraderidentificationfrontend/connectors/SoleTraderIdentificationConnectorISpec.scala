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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors


import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.{NOT_FOUND, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
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


  s"retrieveSoleTraderIdentification($testJourneyId)" should {
    "return Sole Trader Identification" when {
      "there is Sole Trader Identification stored against the journeyId" in {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJson
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification(testJourneyId))

        result mustBe Some(testSoleTraderDetails)
      }
    }
    "return None" when {
      "there is no Sole Trader Identification stored against the journeyId" in {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = NOT_FOUND
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification(testJourneyId))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $FullNameKey)" should {
    "return full name" when {
      "the full name key is given and a full name is stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullNameModel(testFirstName, testLastName)))
        val testJson = Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        )
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsObject](testJourneyId, FullNameKey))

        result mustBe Some(testJson)
      }
    }

    "return None" when {
      "the firstName key is given but there is no first name stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, FullNameKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $DateOfBirthKey)" should {
    "return date of birth" when {
      "the date of birth key is given and date of birth is stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[LocalDate](testJourneyId, DateOfBirthKey))

        result mustBe Some(testDateOfBirth)
      }
    }

    "return None" when {
      "the date of birth key is given but there is no date of birth stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[LocalDate](testJourneyId, DateOfBirthKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $NinoKey)" should {
    "return nino" when {
      "the nino key is given and nino is stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(OK, testNino)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, NinoKey))

        result mustBe Some(JsString(testNino))
      }
    }

    "return None" when {
      "the nino key is given but there is no nino stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, NinoKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $SautrKey)" should {
    "return sautr" when {
      "the sautr key is given and sautr is stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, SautrKey))

        result mustBe Some(JsString(testSautr))
      }
    }

    "return None" when {
      "the sautr key is given but there is no sautr stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, SautrKey))

        result mustBe None
      }
    }
  }

  s"storeData($testJourneyId, $FullNameKey)" should {
    "return SuccessfullyStored" in {
      stubStoreFullName(testJourneyId, FullNameModel(testFirstName, testLastName))(status = OK)
      val result = await(soleTraderIdentificationConnector.storeData[FullNameModel](
        testJourneyId, FullNameKey, FullNameModel(testFirstName, testLastName)))

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
}