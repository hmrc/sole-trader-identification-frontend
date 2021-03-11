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
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.SoleTraderIdentificationStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests


class CheckYourAnswersControllerISpec extends ComponentSpecHelper with CheckYourAnswersViewTests with SoleTraderIdentificationStub {

  "GET /check-your-answers-business" should {
    lazy val result: WSResponse = {
      stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = Json.toJsObject(testSoleTraderDetails))

      get(s"/check-your-answers-business/$testJourneyId")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCheckYourAnswersView(result, testJourneyId)
    }
  }

  "POST /check-your-answers-business" should {
    lazy val result = post("/check-your-answers-business")()

    "return NotImplemented" in {
      result.status mustBe NOT_IMPLEMENTED
    }
  }
}
