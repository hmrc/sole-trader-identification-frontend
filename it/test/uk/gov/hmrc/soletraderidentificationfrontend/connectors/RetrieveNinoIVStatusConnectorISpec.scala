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
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testBusinessVerificationJourneyId
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, NinoIVJourneyStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.NinoIVStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RetrieveNinoIVStatusConnectorISpec extends ComponentSpecHelper with NinoIVStub with FeatureSwitching {

  private val retrieveNinoIVStatusConnector = app.injector.instanceOf[RetrieveNinoIVStatusConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "retrieveNinoIVStatusConnector" when {
    s"the $NinoIVJourneyStub feature switch is enabled" should {
      "return BvPass" in {
        enable(NinoIVJourneyStub)
        stubRetrieveNinoIVResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

        val result = await(retrieveNinoIVStatusConnector.retrieveNinoIVStatus(testBusinessVerificationJourneyId))

        result mustBe BusinessVerificationPass
      }
      "return BvFail" in {
        enable(NinoIVJourneyStub)
        stubRetrieveNinoIVResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "FAIL"))

        val result = await(retrieveNinoIVStatusConnector.retrieveNinoIVStatus(testBusinessVerificationJourneyId))

        result mustBe BusinessVerificationFail
      }
    }

    s"the $NinoIVJourneyStub feature switch is disabled" should {
      "return BvPass" in {
        disable(NinoIVJourneyStub)
        stubRetrieveNinoIVResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

        val result = await(retrieveNinoIVStatusConnector.retrieveNinoIVStatus(testBusinessVerificationJourneyId))

        result mustBe BusinessVerificationPass
      }
      "return BvFail" in {
        disable(NinoIVJourneyStub)
        stubRetrieveNinoIVResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "FAIL"))

        val result = await(retrieveNinoIVStatusConnector.retrieveNinoIVStatus(testBusinessVerificationJourneyId))

        result mustBe BusinessVerificationFail
      }
    }
  }
}
