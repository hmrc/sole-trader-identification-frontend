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

import play.api.http.Status.ACCEPTED
import play.api.libs.json.JsObject
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{fraudulentNinoJson, nonFraudulentNinoJson, testBusinessVerificationJourneyId, testNino}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.SoleTraderIdentificationStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RetrieveFraudulentNinoStatusConnectorISpec extends ComponentSpecHelper with SoleTraderIdentificationStub {

  private val retrieveFraudulentNinoStatusConnector = app.injector.instanceOf[RetrieveFraudulentNinoStatusConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "isFraudulentNino" when {
    s"nino is not fraudulent" should {
      "return false" in {
        stubIsFraudulentNino(testNino)(OK, nonFraudulentNinoJson)

        val result = await(retrieveFraudulentNinoStatusConnector.isFraudulentNino(testNino))

        result mustBe false
      }
    }

    s"nino is fraudulent" should {
      "return true" in {
        stubIsFraudulentNino(testNino)(OK, fraudulentNinoJson)

        val result = await(retrieveFraudulentNinoStatusConnector.isFraudulentNino(testNino))

        result mustBe true
      }
    }

    s"response status is not 200" should {
      "throws an InternalServerException" in {

        stubIsFraudulentNino(testNino)(ACCEPTED, fraudulentNinoJson)

        val exception = intercept[InternalServerException](await(retrieveFraudulentNinoStatusConnector.isFraudulentNino(testNino)))

        exception.getMessage mustBe "Invalid response status returned from backend. Expecting 200 got 202"

      }
    }

    s"response is not a valid json" should {
      "throws an InternalServerException" in {

        stubIsFraudulentNino(testNino)(OK, JsObject.empty)

        val exception = intercept[InternalServerException](await(retrieveFraudulentNinoStatusConnector.isFraudulentNino(testNino)))

        exception.getMessage mustBe "Expecting json { isAFraudulentNino : true/false }"

      }
    }

  }
}

