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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureSaPostcodeViewTests

class CaptureSaPostcodeControllerISpec extends ComponentSpecHelper with CaptureSaPostcodeViewTests with SoleTraderIdentificationStub with AuthStub {

  "GET /self-assessment-postcode" should {
    lazy val result = {
      await(
        journeyConfigRepository.insertJourneyConfig(
          journeyId      = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig  = testIndividualJourneyConfigWithCallingService
        )
      )
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/self-assessment-postcode")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureSaPostcodeView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/self-assessment-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(
            "/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fself-assessment-postcode" +
              "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }

  }

  "POST /self-assessment-postcode" when {
    "the SA Postcode is correctly formatted" should {
      "redirect to Overseas Tax Identifiers page" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubStoreSaPostcode(testJourneyId, testSaPostcode)(status = OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/self-assessment-postcode")("saPostcode" -> testSaPostcode)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureOverseasTaxIdentifierController.show(testJourneyId).url)
        )
      }
    }
    "no SA postcode is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfigWithCallingService
          )
        )
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/self-assessment-postcode")("saPostcode" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSaPostcodeErrorMessageNoEntryPostcode(result)
    }

    "an invalid SA postcode is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfigWithCallingService
          )
        )
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/self-assessment-postcode")("saPostcode" -> "AA!0!!")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSaPostcodeErrorMessageInvalidPostcode(result)
    }
  }

  "GET /no-self-assessment-postcode" should {
    "redirect to Overseas Tax Identifiers page" when {
      "the SA postcode is successfully removed" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/no-self-assessment-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureOverseasTaxIdentifierController.show(testJourneyId).url)
        )
      }
    }

    "throw an exception" when {
      "the backend returns a failure" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRemoveSaPostcode(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/no-self-assessment-postcode")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
