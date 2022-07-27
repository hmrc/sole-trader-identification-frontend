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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureSautrNewViewTests

class CaptureSautrNewControllerISpec extends ComponentSpecHelper
  with CaptureSautrNewViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  "GET /unique-taxpayer-reference-radio-buttons" should {
    lazy val result = {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testIndividualJourneyConfig
      ))
      stubAuth(OK, successfulAuthResponse())
      stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

      get(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureSautrView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Funique-taxpayer-reference-radio-buttons" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }

    "throw an Internal Server Error" when {
      "the user does not have an internal Id" in {
        stubAuth(OK, Json.obj(
          "internalId" -> None
        ))

        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /unique-taxpayer-reference-radio-buttons" when {
    "the sautr is correctly formatted" should {
      "redirect to SA postcode Page and store the data in the backend" when {
        "the user does not have a nino" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubStoreSautr(testJourneyId, testSautr)(status = OK)
          stubRetrieveNino(testJourneyId)(NOT_FOUND)
          stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
            "sa-utr" -> testSautr)

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureSaPostcodeController.show(testJourneyId).url)
          )
        }
      }
      "redirect to the CYA page" when {
        "the user has a nino" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubStoreSautr(testJourneyId, testSautr)(status = OK)
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
            "sa-utr" -> testSautr)

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
          )
        }
      }
    }

    "no sautr is submitted" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRemoveSautr(testJourneyId)(NO_CONTENT)
        stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)
        stubRetrieveNino(testJourneyId)(OK, testNino)

        post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
          "sa-utr" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSautrErrorMessages(result)
    }

    "an invalid sautr is submitted" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
          "sa-utr" -> "123456789")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSautrErrorMessages(result)
    }

    "no radio option is submitted" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRemoveSautr(testJourneyId)(NO_CONTENT)
        stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)
        stubRetrieveNino(testJourneyId)(OK, testNino)

        post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSautrRadioErrorMessages(result)
    }

    "there is a form error and full name is defined" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "")
      }

      testTitleAndHeadingInTheErrorView(result)
    }

    "there is a form error and full name is NOT defined" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(NOT_FOUND)

        post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "")
      }


      "return an internal server error" in {
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is UNAUTHORISED" should {
      "redirect to sign in page" in {
        stubAuthFailure()
        lazy val result: WSResponse = post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
          "sa-utr" -> testSautr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Funique-taxpayer-reference-radio-buttons" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }

    "throw an Internal Server Error" when {
      "the user does not have an internal Id" in {
        stubAuth(OK, Json.obj(
          "internalId" -> None
        ))

        lazy val result: WSResponse = post(s"/identify-your-sole-trader-business/$testJourneyId/unique-taxpayer-reference-radio-buttons")("optSautr" -> "Yes",
          "sa-utr" -> testSautr)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
