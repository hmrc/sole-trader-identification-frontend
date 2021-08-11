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

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{AuthenticatorStub, FeatureSwitching}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.AuthenticatorStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class AuthenticatorConnectorISpec extends ComponentSpecHelper with AuthenticatorStub with FeatureSwitching {
  lazy val testConnector: AuthenticatorConnector = app.injector.instanceOf[AuthenticatorConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "matchSoleTraderDetails" should {
    "return successful match" when {
      "authenticator returns data" when {
        "the stub authenticator feature switch is disabled" in {
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))

          val res = await(testConnector.matchSoleTraderDetails(testIndividualDetails))

          res mustBe Right(testIndividualDetails)
        }

        "the stub authenticator feature switch is enabled" in {
          enable(AuthenticatorStub)
          stubMatchStub(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))

          val res = await(testConnector.matchSoleTraderDetails(testIndividualDetails))

          res mustBe Right(testIndividualDetails)
        }
      }
    }
    "return details mismatch" when {
      "authenticator returns an error with matching" in {
        stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)

        val res = await(testConnector.matchSoleTraderDetails(testIndividualDetails))

        res mustBe Left(SoleTraderDetailsMatching.DetailsMismatch)
      }
    }
    "return details not found" when {
      "authenticator returns a not found error" in {
        stubMatch(testIndividualDetails)(UNAUTHORIZED, notFoundErrorJson)

        val res = await(testConnector.matchSoleTraderDetails(testIndividualDetails))

        res mustBe Left(SoleTraderDetailsMatching.NinoNotFound)
      }
    }
    "return user is deceased" when {
      "authenticator returns dependency failed" in {
        stubMatch(testIndividualDetails)(FAILED_DEPENDENCY, Json.obj())

        val res = await(testConnector.matchSoleTraderDetails(testIndividualDetails))

        res mustBe Left(SoleTraderDetailsMatching.DeceasedCitizensDetails)
      }
    }
    "throw an exception" when {
      "any other status is received" in {
        stubMatch(testIndividualDetails)(INTERNAL_SERVER_ERROR, Json.obj())

        intercept[InternalServerException](await(testConnector.matchSoleTraderDetails(testIndividualDetails)))
      }
    }
  }

}
