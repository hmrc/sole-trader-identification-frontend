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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.EnableNoNinoJourney
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureNinoViewTests

class CaptureNinoControllerISpec extends ComponentSpecHelper
  with CaptureNinoViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  "GET /national-insurance-number" when {
    "the feature switch is turned off" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        get(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCaptureNinoView(result)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fnational-insurance-number" +
              "&origin=sole-trader-identification-frontend"
            )
          )
        }
      }
    }
    "the feature switch is turned on" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        get(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testNoNinoCaptureNinoView(result)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fnational-insurance-number" +
              "&origin=sole-trader-identification-frontend"
            )
          )
        }
      }
    }
  }

  "POST /national-insurance-number" when {
    "the nino is correctly formatted" should {
      "redirect to check your answers page" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubStoreNino(testJourneyId, testNino)(status = OK)
        stubRemoveAddress(testJourneyId)(NO_CONTENT)
        stubRemoveOverseasTaxIdentifiers(testJourneyId)(NO_CONTENT)
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> testNino)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
      "redirect to the capture sautr page" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubStoreNino(testJourneyId, testNino)(status = OK)
        stubRemoveAddress(testJourneyId)(NO_CONTENT)
        stubRemoveOverseasTaxIdentifiers(testJourneyId)(NO_CONTENT)
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> testNino)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSautrController.show(testJourneyId).url)
        )
      }
    }

    "no nino is submitted" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureNinoErrorMessages(result)
    }

    "an invalid nino is submitted" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> "AAAAAAAAAA")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureNinoErrorMessages(result)
    }

    "there is a form error and full name is defined" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> "to simulate an error")
      }

      testTitleAndHeadingInTheErrorView(result)
    }

    "there is a form error and full name is NOT defined" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())

        post(s"/identify-your-sole-trader-business/$testJourneyId/national-insurance-number")("nino" -> "to simulate an error")
      }

      "return an internal server error" in {
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      testTitleAndHeadingGivenNoCustomerFullName(result)

    }

  }

  "GET /no-nino" should {
    "redirect to capture address page" when {
      "the nino is successfully removed" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRemoveNino(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/no-nino")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureAddressController.show(testJourneyId).url)
        )
      }
    }

    "throw an exception" when {
      "the backend returns a failure" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRemoveNino(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/no-nino")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to check your answers page" when {
      "there is no nino on the individual journey" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRemoveNino(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/no-nino")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }
  }
}
