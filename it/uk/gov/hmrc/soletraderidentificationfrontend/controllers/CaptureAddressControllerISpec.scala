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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.EnableNoNinoJourney
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureAddressViewTests

class CaptureAddressControllerISpec extends ComponentSpecHelper
  with CaptureAddressViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  "GET /address" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        continueUrl = testContinueUrl,
        businessVerificationCheck = None,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        enableSautrCheck = true
      ))
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/address")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureAddressView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/address")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Faddress" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /address" when {
    "the form is correctly formatted" should {
      "redirect to the Capture Sautr page and store the data in the backend" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))

        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())

        stubStoreAddress(testJourneyId, testAddress)(status = OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> testAddress1,
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> testCountry
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSautrController.show(testJourneyId).url)
        )
      }
    }
    "the form is missing the first line of the address" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> "",
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> testCountry
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageNoLine1(result)
    }
    "the form is missing the second line of the address" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> testAddress1,
          "address2" -> "",
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> testCountry
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageNoLine2(result)
    }
    "there is an invalid character in the address form" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> "*&%^$",
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> testCountry
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageInvalid(result)
    }
    "the form has a line that is too long" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> "thisisastringthatisoverthirtyfivecharcterslong",
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> testCountry
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageTooManyCharacters(result)
    }
    "the form has invalid characters in the postcode" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> testAddress1,
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> "%4£@",
          "country" -> testCountry
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageInvalidPostcode(result)
    }
    "the form has no country selected" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true,
        ))
        enable(EnableNoNinoJourney)
        stubAuth(OK, successfulAuthResponse())
        stubStoreAddress(testJourneyId, testAddress)(status = OK)
        post(s"/identify-your-sole-trader-business/$testJourneyId/address")(
          "address1" -> testAddress1,
          "address2" -> testAddress2,
          "address3" -> testAddress3,
          "address4" -> testAddress4,
          "address5" -> testAddress5,
          "postcode" -> testPostcode,
          "country" -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureAddressErrorMessageNoEntryCountry(result)
    }

  }
}
