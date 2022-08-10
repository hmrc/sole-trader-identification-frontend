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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers.{OK, BAD_REQUEST, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testInsightsReturnBody, testNino}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.NinoInsightsStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class NinoInsightsConnectorISpec extends ComponentSpecHelper with NinoInsightsStub {

  private val connector = app.injector.instanceOf[NinoInsightsConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "retrieveNinoInsights" should {
    "return a JsObject" in {
      stubNinoInsights(testNino)(OK, testInsightsReturnBody)

      val result = await(connector.retrieveNinoInsights(testNino))

      result mustBe testInsightsReturnBody
    }
    "throw an exception" when {
      "an unexpected status is returned" in {
        stubNinoInsights(testNino)(BAD_REQUEST, Json.obj())

        intercept[InternalServerException](
          await(connector.retrieveNinoInsights(testNino))
        )
      }
    }
  }

}