/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationPass, FullName, Registered, SoleTraderDetails}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SuccessfulMatch
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails.format
import helpers.TestConstants.*

import java.time.format.DateTimeFormatter

class SoleTraderDetailsSpec extends AnyWordSpec with Matchers {

  "SoleTraderDetails" should {

    "be able to de-serialise a valid minimal JSON object" in new Setup {

      val json: JsValue = Json.parse(minimalJsonImplementation)

      json.validate[SoleTraderDetails] match {
        case JsSuccess(value, _) =>
          value mustBe SoleTraderDetails(
            fullName = FullName(firstName = testFirstName, lastName = testLastName),
            dateOfBirth = testDateOfBirth,
            optNino = None,
            address = None,
            optSaPostcode = None,
            optSautr = None,
            identifiersMatch = SuccessfulMatch,
            businessVerification = None,
            registrationStatus = None,
            optTrn = None,
            optConfirmOverseasTaxIdentifier = None,
            optOverseasTaxIdentifierCountry = None,
            optNinoInsights = None
          )
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

    "be able to de-serialise a valid complete JSON object" in new Setup {

      /**
       * Note in practice a fully defined instance of SoleTraderDetails would not be returned from the backend,
       * as some fields are mutually exclusive. However, this test is to ensure that the JSON reads work correctly for all fields.
       */

      val json: JsValue = Json.parse(completeJsonImplementation)

      json.validate[SoleTraderDetails] match {
        case JsSuccess(value, _) =>
          value mustBe SoleTraderDetails(
            fullName = FullName(firstName = testFirstName, lastName = testLastName),
            dateOfBirth = testDateOfBirth,
            optNino = Some(testNino),
            address = Some(testAddress),
            optSaPostcode = Some(testSaPostcode),
            optSautr = Some(testSautr),
            identifiersMatch = SuccessfulMatch,
            businessVerification = Some(BusinessVerificationPass),
            registrationStatus = Some(Registered(testSafeId)),
            optTrn = Some(testTrn),
            optConfirmOverseasTaxIdentifier = Some(testConfirmOverseasTaxIdentifier),
            optOverseasTaxIdentifierCountry = Some(testOverseasIdentifierCountry),
            optNinoInsights = Some(testInsightsReturnBody)
          )
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

    "be able to de-serialise a valid JSON object where the user has confirmed that they do not have an overseas tax identifier" in new Setup {

      val json: JsValue = Json.parse(overseasTaxIdentifierNotConfirmedJsonImplementation)

      json.validate[SoleTraderDetails] match {
        case JsSuccess(value, _) =>
          value mustBe SoleTraderDetails(
            fullName = FullName(firstName = testFirstName, lastName = testLastName),
            dateOfBirth = testDateOfBirth,
            optNino = None,
            address = None,
            optSaPostcode = None,
            optSautr = None,
            identifiersMatch = SuccessfulMatch,
            businessVerification = None,
            registrationStatus = None,
            optTrn = None,
            optConfirmOverseasTaxIdentifier = Some(testOverseasTaxIdentifierNotConfirmed),
            optOverseasTaxIdentifierCountry = None,
            optNinoInsights = None
          )
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

    "raise an error" when {

      "de-serialising a JSON object where the user has declared they have an overseas tax identifier, but has not defined a country" in new Setup {

        val json: JsValue = Json.parse(overseasTaxIdentifierWithoutCountryJsonImplementation)

        val exception = intercept[InternalServerException](json.validate[SoleTraderDetails])

        exception.isInstanceOf[InternalServerException] mustBe true
        exception.getMessage mustBe "Error: Tax identifier country not set, but user has declared they do have an overseas tax identifier"

      }

      "de-serialising a JSON object where the user has defined a country, but has not declared they have an overseas tax identifier" in new Setup {

        val json: JsValue = Json.parse(countryWithOverseasTaxIdentifierNotConfirmedJsonImplementation)

        val exception = intercept[InternalServerException](json.validate[SoleTraderDetails])

        exception.isInstanceOf[InternalServerException] mustBe true
        exception.getMessage mustBe "Error: Tax identifier country set, but user has declared they don't have an overseas tax identifier"

      }

      "de-serialising a JSON object where the user has defined a country, but there is no confirmation status" in new Setup {

        val json: JsValue = Json.parse(countryWithOverseasTaxIdentifierConfirmedNotSetJsonImplementation)

        val exception = intercept[InternalServerException](json.validate[SoleTraderDetails])

        exception.isInstanceOf[InternalServerException] mustBe true
        exception.getMessage mustBe "Error: Invalid combination of tax identifier and country"

      }

    }

    "be able to de-serialise the original JSON implementation for an overseas tax identifier" in new Setup {

      val json: JsValue = Json.parse(originalOverseasTaxIdentifierJsonImplementation)

      json.validate[SoleTraderDetails] match {
        case JsSuccess(value, _) =>
          value mustBe SoleTraderDetails(
            fullName = FullName(firstName = testFirstName, lastName = testLastName),
            dateOfBirth = testDateOfBirth,
            optNino = None,
            address = None,
            optSaPostcode = None,
            optSautr = None,
            identifiersMatch = SuccessfulMatch,
            businessVerification = None,
            registrationStatus = None,
            optTrn = None,
            optConfirmOverseasTaxIdentifier = Some(testConfirmOverseasTaxIdentifier),
            optOverseasTaxIdentifierCountry = Some(testOverseasIdentifierCountry),
            optNinoInsights = None
          )
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

  }


  trait Setup {

    val testDateOfBirthAsString: String = testDateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val minimalJsonImplementation: String =
      s"""
        |{
        |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
        |  "dateOfBirth": "$testDateOfBirthAsString",
        |  "identifiersMatch": "SuccessfulMatch"
        |}
        |""".stripMargin

    val completeJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "nino": "$testNino",
         |  "address": {
         |               "line1": "line1",
         |               "line2": "line2",
         |               "line3": "line3",
         |               "line4": "line4",
         |               "line5": "line5",
         |               "postcode": "AA1 1AA",
         |               "countryCode": "GB" },
         |  "saPostcode": "$testSaPostcode",
         |  "sautr": "$testSautr",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "businessVerification": { "verificationStatus": "PASS" },
         |  "registration": { "registrationStatus" : "REGISTERED", "registeredBusinessPartnerId" : "$testSafeId" },
         |  "trn": "$testTrn",
         |  "confirmOverseasTaxIdentifier": { "hasOverseasTaxIdentifier": "Yes", "overseasTaxIdentifier": "$testOverseasIdentifier" },
         |  "country": "$testOverseasIdentifierCountry",
         |  "reputation":{
         |    "ninoInsightsCorrelationId": "$testCorrelationId",
         |    "code":0,
         |    "reason":"0 code"
         |   }
         |}
         |""".stripMargin

    val overseasTaxIdentifierNotConfirmedJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "confirmOverseasTaxIdentifier": { "hasOverseasTaxIdentifier": "No" }
         |}
         |""".stripMargin


    val overseasTaxIdentifierWithoutCountryJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "confirmOverseasTaxIdentifier": { "hasOverseasTaxIdentifier": "Yes", "overseasTaxIdentifier": "$testOverseasIdentifier" }
         |}
         |""".stripMargin

    val countryWithOverseasTaxIdentifierNotConfirmedJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "confirmOverseasTaxIdentifier": { "hasOverseasTaxIdentifier": "No" },
         |  "country": "$testOverseasIdentifierCountry"
         |}
         |""".stripMargin

    val countryWithOverseasTaxIdentifierConfirmedNotSetJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "country": "$testOverseasIdentifierCountry"
         |}
         |""".stripMargin

    val originalOverseasTaxIdentifierJsonImplementation: String =
      s"""
         |{
         |  "fullName": { "firstName": "$testFirstName", "lastName": "$testLastName" },
         |  "dateOfBirth": "$testDateOfBirthAsString",
         |  "identifiersMatch": "SuccessfulMatch",
         |  "overseasTaxIdentifiers": "$testOverseasIdentifier",
         |  "country": "$testOverseasIdentifierCountry"
         |}
         |""".stripMargin

  }

}
