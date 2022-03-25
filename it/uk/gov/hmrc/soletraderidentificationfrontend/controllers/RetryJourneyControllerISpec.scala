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

class RetryJourneyControllerISpec extends ComponentSpecHelper
  with AuthStub
  with SoleTraderIdentificationStub {

  "GET /try-again" should {

    "remove any existing journey data" when {

      "a user elects to restart the sole trader journey" in {

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse())
        stubRemoveAllData(testJourneyId)(NO_CONTENT)

        val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/try-again")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureFullNameController.show(testJourneyId).url)
        )

        verifyRemoveAllData(testJourneyId)
      }
    }

    "redirect to sign in page" when {

      "the user is not authorised" in {

        stubAuthFailure()

        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/try-again")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(signInRedirectUrl(testJourneyId, "try-again"))
        )
      }
    }
  }

  "POST /try-again" should {

    "return not found" in {

      lazy val result: WSResponse = post(s"/identify-your-sole-trader-business/$testJourneyId/try-again")()

      result.status mustBe NOT_FOUND
    }
  }

}
