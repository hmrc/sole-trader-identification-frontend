/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testNino, testSautr, testSafeId}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Registered, RegistrationFailed}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.RegisterStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "register" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegister(testNino, testSautr)(OK, Registered(testSafeId))

        val result = await(registrationConnector.register(testNino, testSautr))

        result mustBe Registered(testSafeId)
      }
    }

    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubRegister(testNino, testSautr)(INTERNAL_SERVER_ERROR, RegistrationFailed)

        val result = await(registrationConnector.register(testNino, testSautr))

        result mustBe RegistrationFailed
      }
    }
  }

}
