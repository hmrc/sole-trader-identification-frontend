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

package services

import connectors.mocks.MockAuditConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.DetailsMismatch
import uk.gov.hmrc.soletraderidentificationfrontend.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends AnyWordSpec with Matchers with MockAuditConnector with MockSoleTraderIdentificationService with GuiceOneAppPerSuite {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object TestService extends AuditService(appConfig, mockAuditConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditIndividualJourney" should {
    "send an event" when {
      "the entity is an individual and identifiers match" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))

        val result: Unit = await(TestService.auditJourney(testJourneyId, testIndividualJourneyConfig))

        result mustBe a[Unit]

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualSuccessfulAuditEventJson
      }

      "the entity is an individual, identifiers match and the journey config defines a calling service" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))

        val result: Unit = await(TestService.auditJourney(testJourneyId, testIndividualJourneyConfigWithCallingService))

        result mustBe a[Unit]

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualSuccessfulWithCallingServiceAuditEventJson
      }

      "the entity is an individual and identifiers do not match" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveAuthenticatorFailureResponse(testJourneyId)(Future.successful(Some(DetailsMismatch.toString)))

        val result: Unit = await(TestService.auditJourney(testJourneyId, testIndividualJourneyConfig))

        result mustBe a[Unit]

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualFailureAuditEventJson
      }
    }

    "throw an exception" when {
      "there is missing data for the audit" in {
        mockRetrieveIndividualDetails(testJourneyId)(Future.successful(None))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(None))
        mockRetrieveAuthenticatorFailureResponse(testJourneyId)(Future.successful(None))

        intercept[InternalServerException](
          await(TestService.auditJourney(testJourneyId, testIndividualJourneyConfig))
        )
      }
    }
  }

  "auditSoleTraderJourney" should {
    "send an event" when {
      "the entity is a Sole Trader and identifiers match" when {
        "there is an sautr" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetails)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))


          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJson(identifiersMatch = true)
        }
        "there is an sautr and the journey config defines a calling service" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetails)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))


          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfigWithCallingService))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderWithCallingServiceAuditEventJson(identifiersMatch = true)
        }
        "there is an sa utr and the journey config has disabled business verification" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderWithoutBVCheckDetails)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))


          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfigWithBVCheckDisabled))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderWithoutBVCheckAuditEventJson(identifiersMatch = true)
        }
        "there is an sautr but registration fails" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsRegistrationFailed)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))


          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderRegistrationFailedAuditEventJson(identifiersMatch = true)
        }
        "there is not an sautr" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoSautr)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))

          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoSautr(true)
        }
        "there is not a nino" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoNinoButUtr)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(Some(testKnownFactsResponseUK)))

          val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

          result mustBe a[Unit]

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoNino(identifiersMatch = true)
        }
      }
      "the entity is a Sole Trader and identifiers do not match" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoMatch)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveAuthenticatorFailureResponse(testJourneyId)(Future.successful(Some(DetailsMismatch.toString)))
        mockRetrieveES20Response(testJourneyId)(Future.successful(None))

        val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe a[Unit]

        verifySendExplicitAuditSoleTraders()
        auditEventCaptor.getValue mustBe testSoleTraderFailureAuditEventJson()
      }
      "the entity is a Sole Trader and the user is overseas" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoNinoAndOverseas)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
        mockRetrieveES20Response(testJourneyId)(Future.successful(Some(testKnownFactsResponseOverseas)))


        val result: Unit = await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))

        result mustBe a[Unit]

        verifySendExplicitAuditSoleTraders()
        auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoNinoOverseas(identifiersMatch = true)
      }
    }

    "throw an exception" when {
      "there is missing data for the audit" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.failed(new InternalServerException("failed")))

        intercept[InternalServerException](
          await(TestService.auditJourney(testJourneyId, testSoleTraderJourneyConfig))
        )
      }
    }
  }

}
