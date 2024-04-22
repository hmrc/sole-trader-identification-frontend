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

import play.api.http.Status.NOT_FOUND
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{signInRedirectUrl, testInternalId, testJourneyId, testSoleTraderJourneyConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.DetailsDidNotMatchViewTests

class DetailsDidNotMatchISpec extends ComponentSpecHelper with DetailsDidNotMatchViewTests with AuthStub {

  "GET /details-did-not-match" should {
    lazy val result = {
      await(
        journeyConfigRepository.insertJourneyConfig(
          journeyId      = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig  = testSoleTraderJourneyConfig
        )
      )
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/details-did-not-match")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testDetailsDidNotMatchView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/details-did-not-match")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(signInRedirectUrl(testJourneyId, "details-did-not-match"))
        )
      }
    }
  }

  "POST /details-did-not-match" should {

    "return not found" in {

      lazy val result: WSResponse = post(s"/identify-your-sole-trader-business/$testJourneyId/details-not-found")()

      result.status mustBe NOT_FOUND
    }
  }
}
