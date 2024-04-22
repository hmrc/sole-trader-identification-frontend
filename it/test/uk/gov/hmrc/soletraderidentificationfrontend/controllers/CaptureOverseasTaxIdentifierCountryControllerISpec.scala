/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.test.Helpers.{BAD_REQUEST, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureOverseasTaxIdentifierCountryViewTests

class CaptureOverseasTaxIdentifierCountryControllerISpec
    extends ComponentSpecHelper
    with CaptureOverseasTaxIdentifierCountryViewTests
    with AuthStub
    with SoleTraderIdentificationStub {

  "GET /overseas-tax-identifier-country" should {
    lazy val result = {
      await(
        journeyConfigRepository.insertJourneyConfig(
          journeyId      = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig  = testIndividualJourneyConfig
        )
      )
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/overseas-tax-identifier-country")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureCaptureOverseasTaxIdentifiersCountryView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/overseas-tax-identifier-country")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(
            "/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Foverseas-tax-identifier-country" +
              "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /overseas-tax-identifier-country" when {
    "the tax identifiers country is correctly formatted" should {
      "redirect to Check Your Answers" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testSoleTraderJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubStoreOverseasTaxIdentifiersCountry(testJourneyId, testCountry)(OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/overseas-tax-identifier-country")("country" -> "GB")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )

        verifyStoreCountry(testJourneyId, testCountry)
      }
    }
    "no tax identifier country is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/overseas-tax-identifier-country")("country" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCaptureOverseasTaxIdentifiersErrorMessages(result)
    }
  }
}
