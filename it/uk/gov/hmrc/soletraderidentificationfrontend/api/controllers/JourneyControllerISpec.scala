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

package uk.gov.hmrc.soletraderidentificationfrontend.api.controllers

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.{TableFor1, Tables}
import play.api.http.Status.CREATED
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, JourneyStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with SoleTraderIdentificationStub with AuthStub {

  "POST /sole-trader-identification/api/<typeOfJourney> for all journey types" should {

    val testSoleTraderJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
      "businessVerificationCheck" -> testSoleTraderJourneyConfig.businessVerificationCheck,
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl,
      "enableSautrCheck" -> testSoleTraderJourneyConfig.pageConfig.enableSautrCheck,
      "accessibilityUrl" -> testSoleTraderJourneyConfig.pageConfig.accessibilityUrl,
      "regime" -> testSoleTraderJourneyConfig.regime
    )

    val createJourneyApiUrlSuffixScenarios: TableFor1[String] =
      Tables.Table(
        "createJourneyApiUrlSuffix",
        "journey",
        "sole-trader-journey",
        "individual-journey"
      )

    val incomingUrlJsonKeyScenarios: TableFor1[String] =
      Tables.Table(
        "incomingNonRelativeUrlJsonKey",
        "continueUrl",
        "signOutUrl",
        "accessibilityUrl"
      )

    "respond with Bad Request" when {
      "incoming json contains a non relative url" in {

        forAll(createJourneyApiUrlSuffixScenarios) { (createJourneyApiUrlSuffix: String) => {

          forAll(incomingUrlJsonKeyScenarios) { (incomingUrlJsonKey: String) => {
            stubAuth(OK, successfulAuthResponse())

            stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

            val journeyUrl = "/sole-trader-identification/api/" + createJourneyApiUrlSuffix

            post(
              uri = journeyUrl,
              json = testSoleTraderJourneyConfigJson ++ Json.obj(incomingUrlJsonKey -> "https://www.google.com/")
            ).status must be(BAD_REQUEST)

            post(
              uri = journeyUrl,
              json = testSoleTraderJourneyConfigJson ++ Json.obj(incomingUrlJsonKey -> "/")
            ).status must be(BAD_REQUEST)

          }
          }
        }
        }
      }
    }

    "respond with Created " when {
      "incoming json contains a localhost url" in {

        forAll(createJourneyApiUrlSuffixScenarios) { (createJourneyApiUrlSuffix: String) => {

          forAll(incomingUrlJsonKeyScenarios) { (incomingUrlJsonKey: String) => {

            stubAuth(OK, successfulAuthResponse())

            stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

            val incomingJson = testSoleTraderJourneyConfigJson ++ Json.obj(incomingUrlJsonKey -> "https://localhost:9000/some/url")

            lazy val result = post(uri = "/sole-trader-identification/api/" + createJourneyApiUrlSuffix, json = incomingJson)

            result.status must be(CREATED)

            journeyConfigRepository.drop

          }
          }
        }
        }
      }
    }
  }

  "POST /api/journey" should {
    val testSoleTraderJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testSoleTraderJourneyConfig.continueUrl,
      "businessVerificationCheck" -> testSoleTraderJourneyConfig.businessVerificationCheck,
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl,
      "enableSautrCheck" -> testSoleTraderJourneyConfig.pageConfig.enableSautrCheck,
      "accessibilityUrl" -> testSoleTraderJourneyConfig.pageConfig.accessibilityUrl,
      "regime" -> testSoleTraderJourneyConfig.regime
    )

    "returns json containing the url to Capture Full Name Controller" when {

      "an optFullNamePageLabel field is not provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/journey", testSoleTraderJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig)
      }

      "an optFullNamePageLabel field is provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        post(
          uri = "/sole-trader-identification/api/journey",
          json = testSoleTraderJourneyConfigJson + ("optFullNamePageLabel" -> JsString(testFullNamePageLabel))
        )

        val expectedSoleTraderJourneyConfig = testSoleTraderJourneyConfig
          .copy(pageConfig = testSoleTraderPageConfig.copy(optFullNamePageLabel = Some(testFullNamePageLabel)))

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(expectedSoleTraderJourneyConfig)
      }

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
      "businessVerificationCheck" -> false,
      "deskProServiceId" -> testSoleTraderJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testSoleTraderJourneyConfig.pageConfig.signOutUrl,
      "accessibilityUrl" -> testSoleTraderJourneyConfig.pageConfig.accessibilityUrl,
      "regime" -> testSoleTraderJourneyConfig.regime
    )

    "returns json containing the url to Capture Full Name Controller" when {

      "an optFullNamePageLabel field is not provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/sole-trader-journey", testSoleTraderJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig.copy(businessVerificationCheck = false))
      }

      "an optFullNamePageLabel field is provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        post(
          uri = "/sole-trader-identification/api/sole-trader-journey",
          json = testSoleTraderJourneyConfigJson + ("optFullNamePageLabel" -> JsString(testFullNamePageLabel))
        )

        val expectedSoleTraderJourneyConfig = testSoleTraderJourneyConfig
          .copy(businessVerificationCheck = false)
          .copy(pageConfig = testSoleTraderPageConfig.copy(optFullNamePageLabel = Some(testFullNamePageLabel)))

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(expectedSoleTraderJourneyConfig)
      }

    }

    "ignore an incoming enableSautrCheck json field set to false" in {

      stubAuth(OK, successfulAuthResponse())
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      val incomingJson = testSoleTraderJourneyConfigJson ++ Json.obj("enableSautrCheck" -> false)

      lazy val result = post("/sole-trader-identification/api/sole-trader-journey", incomingJson)

      (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

      await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testSoleTraderJourneyConfig.copy(businessVerificationCheck = false))

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
      "continueUrl" -> testIndividualJourneyConfig.continueUrl,
      "businessVerificationCheck" -> false,
      "deskProServiceId" -> testIndividualJourneyConfig.pageConfig.deskProServiceId,
      "signOutUrl" -> testIndividualJourneyConfig.pageConfig.signOutUrl,
      "accessibilityUrl" -> testIndividualJourneyConfig.pageConfig.accessibilityUrl,
      "regime" -> testSoleTraderJourneyConfig.regime
    )

    "returns json containing the url to Capture Full Name Controller" when {

      "an optFullNamePageLabel field is not provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/sole-trader-identification/api/individual-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureFullNameController.show(testJourneyId).url)

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testIndividualJourneyConfig)
      }

      "an optFullNamePageLabel field is provided" in {
        stubAuth(OK, successfulAuthResponse())
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        post(
          uri = "/sole-trader-identification/api/individual-journey",
          json = testJourneyConfigJson + ("optFullNamePageLabel" -> JsString(testFullNamePageLabel))
        )

        val expectedIndividualJourneyConfig = testIndividualJourneyConfig
          .copy(businessVerificationCheck = false)
          .copy(pageConfig = testIndividualPageConfig.copy(optFullNamePageLabel = Some(testFullNamePageLabel)))

        await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(expectedIndividualJourneyConfig)
      }

    }

    "ignore an incoming enableSautrCheck json field set to true" in {

      stubAuth(OK, successfulAuthResponse())
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      val incomingJson = testJourneyConfigJson ++ Json.obj("enableSautrCheck" -> true)

      lazy val result = post("/sole-trader-identification/api/individual-journey", incomingJson)

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
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = Json.toJson(testSoleTraderDetails)
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testSoleTraderDetailsJson
      }

      "the journeyId exists and the identifiers do not match" when {
        "the journeyId exists and verificationStatus is BusinessVerificationNotEnoughInfoToCallBV (remapped to UNCHALLENGED)" in {
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testSoleTraderDetailsJsonMisMatch(testBusinessVerificationNotEnoughInfoToCallJson)
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractBusinessVerificationJsonBranch(fullJson = result.json) mustBe
            testJourneyControllerBusinessVerificationJson(verificationStatus = "UNCHALLENGED")

        }

        "the journeyId exists and verificationStatus is BusinessVerificationNotEnoughInformationToChallenge (remapped to UNCHALLENGED)" in {
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testSoleTraderDetailsJsonMisMatch(testBusinessVerificationNotEnoughInfoToChallengeJson)
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractBusinessVerificationJsonBranch(fullJson = result.json) mustBe
            testJourneyControllerBusinessVerificationJson(verificationStatus = "UNCHALLENGED")

        }
      }

      "the journeyId exists for an individual with a nino" when {

        val testExpectedRetrievedJourneyDataJson: JsObject = {
          Json.obj("fullName" -> Json.obj(
            "firstName" -> testFirstName,
            "lastName" -> testLastName
          ),
            "dateOfBirth" -> testDateOfBirth,
            "nino" -> testNino,
            "identifiersMatch" -> true
          )
        }

        "the Nino is uppercase" in {
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testExpectedRetrievedJourneyDataJson ++
              Json.obj("nino" -> testNino) ++
              Json.obj("identifiersMatch" ->  "SuccessfulMatch")
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testExpectedRetrievedJourneyDataJson
        }

        "the Nino is lowercase" in {
          stubAuth(OK, successfulAuthResponse())

          stubRetrieveSoleTraderDetails(testJourneyId)(
            status = OK,
            body = testExpectedRetrievedJourneyDataJson ++
              Json.obj("nino" -> testNino.toLowerCase) ++
              Json.obj("identifiersMatch" ->  "SuccessfulMatch")
          )

          lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testExpectedRetrievedJourneyDataJson
        }
      }

      "the journeyId exists for an individual with no nino" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testSoleTraderDetailsIndividualJourneyNoNino)
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testSoleTraderDetailsJsonIndividualNoNino
      }

      "the journeyId exists but no business verification status is stored" in {
        val testSoleTraderDetailsJson: JsObject = {
          Json.obj(
            "fullName" -> Json.obj(
              "firstName" -> testFirstName,
              "lastName" -> testLastName
            ),
            "dateOfBirth" -> testDateOfBirth,
            "nino" -> testNino,
            "saPostcode" -> testSaPostcode,
            "sautr" -> testSautr,
            "identifiersMatch" -> true,
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTERED",
              "registeredBusinessPartnerId" -> testSafeId
            )
          )
        }

        stubAuth(OK, successfulAuthResponse())
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testSoleTraderDetailsNoBV)
        )

        lazy val result = get(s"/sole-trader-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testSoleTraderDetailsJson
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

  def testJourneyControllerBusinessVerificationJson(verificationStatus: String): JsObject = Json.obj(
    "businessVerification" -> Json.obj(
      "verificationStatus" -> verificationStatus
    )
  )

  private def extractBusinessVerificationJsonBranch(fullJson: JsValue): JsObject = fullJson.transform((JsPath \ "businessVerification").json.pickBranch).get

}
