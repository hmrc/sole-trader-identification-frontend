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

import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testNino, testRegime, testSafeId, testSautr, testTrn}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Registered, RegistrationFailed}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.RegisterStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "registerWithNino" should {
    "return Registered" when {
      "the registration has been successful" when {
        "the user has a nino and a sautr" in {
          stubRegister(testNino, Some(testSautr), testRegime)(OK, Registered(testSafeId))

          val result = await(registrationConnector.registerWithNino(testNino, Some(testSautr), testRegime))

          result mustBe Registered(testSafeId)
        }
        "the user only has a nino" in {
          stubRegister(testNino, None, testRegime)(OK, Registered(testSafeId))

          val result = await(registrationConnector.registerWithNino(testNino, None, testRegime))

          result mustBe Registered(testSafeId)
        }
      }
    }

    "capitalise the NINO" when {
      "it has been entered in lower case" in {
        val testLowerCaseNino = "aa111111a"
        stubRegister(testNino, Some(testSautr), testRegime)(OK, Registered(testSafeId))

        val result = await(registrationConnector.registerWithNino(testLowerCaseNino, Some(testSautr), testRegime))

        result mustBe Registered(testSafeId)
      }
    }

    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubRegister(testNino, Some(testSautr), testRegime)(INTERNAL_SERVER_ERROR, RegistrationFailed)

        val result = await(registrationConnector.registerWithNino(testNino,  Some(testSautr), testRegime))

        result mustBe RegistrationFailed
      }
    }
  }

  "registerWithTrn" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegisterWithTrn(testTrn, testSautr, testRegime)(OK, Registered(testSafeId))

        val result = await(registrationConnector.registerWithTrn(testTrn, testSautr, testRegime))

        result mustBe Registered(testSafeId)
      }
    }

    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        stubRegisterWithTrn(testTrn, testSautr, testRegime)(INTERNAL_SERVER_ERROR, RegistrationFailed)

        val result = await(registrationConnector.registerWithTrn(testTrn, testSautr, testRegime))

        result mustBe RegistrationFailed
      }
    }
  }

}
