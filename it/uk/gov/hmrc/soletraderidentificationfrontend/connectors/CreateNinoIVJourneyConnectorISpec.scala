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

import play.api.http.Status.FORBIDDEN
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, NOT_FOUND, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateNinoIVJourneyConnector.{JourneyCreated, NotEnoughEvidence, UserLockedOut}
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, NinoIVJourneyStub}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.NinoIVStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class CreateNinoIVJourneyConnectorISpec extends ComponentSpecHelper with NinoIVStub with FeatureSwitching {

  private lazy val createNinoIVJourneyConnector = app.injector.instanceOf[CreateNinoIVJourneyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en"), Lang("cy")))

  "createNinoIVJourneyConnector" when {
    s"the $NinoIVJourneyStub feature switch is enabled" should {
      "return the redirectUri" when {
        "the journey creation has been successful" in {
          enable(NinoIVJourneyStub)
          stubCreateNinoIVJourneyFromStub(testNino, testJourneyId, testSoleTraderJourneyConfig)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Right(JourneyCreated(testContinueUrl))
        }

      }
      "return no redirect URL and an appropriate IV status" when {
        "the journey creation has been unsuccessful because IV cannot find the record" in {
          enable(NinoIVJourneyStub)
          stubCreateNinoIVJourneyFromStub(testNino, testJourneyId, testSoleTraderJourneyConfig)(NOT_FOUND, Json.obj())

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Left(NotEnoughEvidence)
        }
        "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
          enable(NinoIVJourneyStub)
          stubCreateNinoIVJourneyFromStub(testNino, testJourneyId, testSoleTraderJourneyConfig)(FORBIDDEN, Json.obj())

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Left(UserLockedOut)
        }
      }
    }
    s"the $NinoIVJourneyStub feature switch is disabled" should {
      "return the redirectUri and therefore no BV status" when {
        "the journey creation has been successful" in {
          disable(NinoIVJourneyStub)
          stubCreateNinoIdentityVerificationJourney(testNino, testJourneyId, testSoleTraderJourneyConfig)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Right(JourneyCreated(testContinueUrl))
        }

      }
      "return no redirect URL and an appropriate BV status" when {
        "the journey creation has been unsuccessful because BV cannot find the record" in {
          disable(NinoIVJourneyStub)
          stubCreateNinoIdentityVerificationJourney(testNino, testJourneyId, testSoleTraderJourneyConfig)(NOT_FOUND, Json.obj())

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Left(NotEnoughEvidence)
        }
        "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
          disable(NinoIVJourneyStub)
          stubCreateNinoIdentityVerificationJourney(testNino, testJourneyId, testSoleTraderJourneyConfig)(FORBIDDEN, Json.obj())

          val result = await(createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(testJourneyId, testNino, testSoleTraderJourneyConfig))

          result mustBe Left(UserLockedOut)
        }
      }
    }
  }

}
