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

  val testJourneyConfigJson: JsObject = Json.obj(
    "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
    "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
    "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl,
    "enableSautrCheck" -> testSoleTraderJourneyConfig.pageConfig.enableSautrCheck
  )

  "POST /api/journey" should {
    "redirect to Capture Full Name Controller" when {
      "enableSautrCheck is false" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig)

      }

      "enableSautrCheck is true" in {
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testSoleTraderJourneyConfigSautrEnabled.continueUrl,
          "deskProServiceId" -> testSoleTraderJourneyConfigSautrEnabled.pageConfig.deskProServiceId,
          "signOutUrl" -> testSoleTraderJourneyConfigSautrEnabled.pageConfig.signOutUrl,
          "enableSautrCheck" -> testSoleTraderJourneyConfigSautrEnabled.pageConfig.enableSautrCheck
        )
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfigSautrEnabled)

      }
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/sole-trader-identification/api/journey", testJourneyConfigJson)

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
    "redirect to Capture Full Name Controller" when {
      "enableSautrCheck is false" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig)

      }

      "enableSautrCheck is true" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig)

      }
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testJourneyConfigJson)

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
    "redirect to Capture Full Name Controller" when {
      "enableSautrCheck is false" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/individual-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testIndividualJourneyConfig)

      }

      "enableSautrCheck is true" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/individual-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testIndividualJourneyConfig)

      }
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
      "the journeyId exists" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = testSoleTraderDetailsJson
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.toJsObject(testSoleTraderDetails)
      }
    }

    "return not found" when {
      "the journey Id does not exist" in {
        stubAuth(OK, successfulAuthResponse())
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
  }

}
