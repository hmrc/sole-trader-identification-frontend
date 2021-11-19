/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.api.controllers

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, JourneyStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with SoleTraderIdentificationStub with AuthStub {

  "POST /api/journey" should {
    val testSoleTraderJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
      "businessVerificationCheck" -> testSoleTraderJourneyConfig.businessVerificationCheck,
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl,
      "enableSautrCheck" -> testSoleTraderJourneyConfig.pageConfig.enableSautrCheck
    )
    "redirect to Capture Full Name Controller" in {
      stubAuth(OK, successfulAuthResponse())
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/sole-trader-identification/api/journey", testSoleTraderJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

      await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig)
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/sole-trader-identification/api/journey", testSoleTraderJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fsole-trader-identification%2Fapi%2Fjourney" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /api/sole-trader-journey" should {
    val testSoleTraderJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
      "businessVerificationCheck" -> Some(true),
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl
    )
    "redirect to Capture Full Name Controller" in {
      stubAuth(OK, successfulAuthResponse())
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testSoleTraderJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

      await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig.copy(businessVerificationCheck = Some(true)))

    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testSoleTraderJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fsole-trader-identification%2Fapi%2Fsole-trader-journey" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /api/individual-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl
    )
    "redirect to Capture Full Name Controller" in {
      stubAuth(OK, successfulAuthResponse())
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/sole-trader-identification/api/individual-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

      await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testIndividualJourneyConfig)
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/sole-trader-identification/api/individual-journey", testJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fsole-trader-identification%2Fapi%2Findividual-journey" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "GET /api/journey/:journeyId" should {
    "return captured data" when {
      "the journeyId exists and the identifiers match" in {
        stubAuth(OK, successfulAuthResponse())
        insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = true)
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJson(identifiersMatch = true)
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.toJsObject(testSoleTraderDetails(identifiersMatch = true))
      }

      "the journeyId exists and the identifiers do not match" in {
        stubAuth(OK, successfulAuthResponse())
        insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = true)
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJson()
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.toJsObject(testSoleTraderDetails())
      }

      "the journeyId exists for an individual with a nino" in {
        stubAuth(OK, successfulAuthResponse())
        insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = false)
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJsonIndividual
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.toJsObject(testSoleTraderDetailsIndividualJourney)
      }

      "the journeyId exists for an individual with no nino" in {
        stubAuth(OK, successfulAuthResponse())
        insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = false)
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJsonIndividualNoNino
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.toJsObject(testSoleTraderDetailsIndividualJourneyNoNino)
      }
    }

    "return not found" when {
      "the journey Id does not exist" in {
        stubAuth(OK, successfulAuthResponse())
        insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = true)
        stubRetrieveSoleTraderDetails(testJourneyId)(status = NOT_FOUND)

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe NOT_FOUND
      }
    }

    "redirect to Sign In Page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fsole-trader-identification%2Fapi%2Fjourney%2F$testJourneyId" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }

    "throw an internal server exception" when {
      "the data is in an unexpected state" when {
        "the BV and Registration fields are missing on the Sole Trader Journey" in {
          val testSoleTraderDetailsJson: JsObject = {
            Json.obj("fullName" -> Json.obj(
              "firstName" -> testFirstName,
              "lastName" -> testLastName
            ),
              "dateOfBirth" -> testDateOfBirth,
              "nino" -> testNino,
              "saPostcode" -> testSaPostcode,
              "sautr" -> testSautr,
              "identifiersMatch" -> true
            )
          }

          stubAuth(OK, successfulAuthResponse())
          insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = true)
          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testSoleTraderDetailsJson
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
        "the BV and Registration fields are found on the Individual Journey" in {
          val testSoleTraderDetailsJson: JsObject = {
            Json.obj("fullName" -> Json.obj(
              "firstName" -> testFirstName,
              "lastName" -> testLastName
            ),
              "dateOfBirth" -> testDateOfBirth,
              "nino" -> testNino,
              "saPostcode" -> testSaPostcode,
              "identifiersMatch" -> true,
              "businessVerification" -> Json.obj(
                "verificationStatus" -> "UNCHALLENGED"
              ),
              "registration" -> Json.obj(
                "registrationStatus" -> "REGISTRATION_NOT_CALLED"
              )
            )
          }

          stubAuth(OK, successfulAuthResponse())
          insertJourneyConfig(testJourneyId, testInternalId, testContinueUrl, None, None, testDeskProServiceId, testSignOutUrl, enableSautrCheck = false)
          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testSoleTraderDetailsJson
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

}
