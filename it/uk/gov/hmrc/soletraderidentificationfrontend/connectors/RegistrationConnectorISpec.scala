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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
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
          stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)

          val result = await(registrationConnector.registerWithNino(testNino, Some(testSautr), testRegime))

          result mustBe Registered(testSafeId)
        }
        "the user only has a nino" in {
          stubRegister(testNino, None, testRegime)(OK, testBackendSuccessfulRegistrationJson)

          val result = await(registrationConnector.registerWithNino(testNino, None, testRegime))

          result mustBe Registered(testSafeId)
        }
      }
    }

    "capitalise the NINO" when {
      "it has been entered in lower case" in {
        val testLowerCaseNino = "aa111111a"
        stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerWithNino(testLowerCaseNino, Some(testSautr), testRegime))

        result mustBe Registered(testSafeId)
      }
    }

    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        val registrationFailure = Json.arr(Json.obj(
          "code" -> "PARTY_TYPE_MISMATCH",
          "reason" -> "The remote endpoint has indicated there is Party Type mismatch"
        ))

        stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendFailedRegistrationJson(registrationFailure))

        val result = await(registrationConnector.registerWithNino(testNino, Some(testSautr), testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe testRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }
      "multiple failures have been returned" in {
        val registrationFailure = Json.arr(Json.obj(
          "code" -> "INVALID_REGIME",
          "reason" -> "Request has not passed validation.  Invalid regime"
        ),
          Json.obj(
            "code" -> "INVALID_PAYLOAD",
            "reason" -> "Request has not passed validation. Invalid payload."
          ))

        stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendFailedRegistrationJson(registrationFailure))

        val result = await(registrationConnector.registerWithNino(testNino, Some(testSautr), testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe testMultipleRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }
    }
  }

  "registerWithTrn" should {
    "return Registered" when {
      "the registration has been successful" in {
        stubRegisterWithTrn(testTrn, testSautr, testRegime)(OK, testBackendSuccessfulRegistrationJson)

        val result = await(registrationConnector.registerWithTrn(testTrn, testSautr, testRegime))

        result mustBe Registered(testSafeId)
      }
    }

    "return RegistrationFailed" when {
      "the registration has not been successful" in {
        val registrationFailure = Json.arr(Json.obj(
          "code" -> "PARTY_TYPE_MISMATCH",
          "reason" -> "The remote endpoint has indicated there is Party Type mismatch"
        ))

        stubRegisterWithTrn(testTrn, testSautr, testRegime)(OK, testBackendFailedRegistrationJson(registrationFailure))

        val result = await(registrationConnector.registerWithTrn(testTrn, testSautr, testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe testRegistrationFailure
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }
    }
  }

}
