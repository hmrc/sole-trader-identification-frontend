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

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, FeatureSwitching}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationPass, FullName, Registered}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, RegisterStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessVerificationControllerISpec extends ComponentSpecHelper
  with FeatureSwitching
  with AuthStub
  with BusinessVerificationStub
  with RegisterStub
  with SoleTraderIdentificationStub
  with BeforeAndAfterEach
  with WiremockHelper {

  def extraConfig = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(BusinessVerificationStub)
    journeyConfigRepository.drop
  }

  "GET /business-verification-result" when {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "redirect to the continue url if BV status is stored successfully" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRegister(testNino, testSautr)(OK, Registered(testSafeId))
        stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
        stubAudit()
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(Registered(testSafeId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(testContinueUrl)
        }

        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
        verifyAudit()
      }

      "throw an exception when the query string is missing" in {
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }
    }

    s"the $BusinessVerificationStub feature switch is disabled" should {
      "redirect to the journey redirect controller if BV status is stored successfully" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))

        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRegister(testNino, testSautr)(OK, Registered(testSafeId))
        stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
        stubAudit()
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(Registered(testSafeId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(testContinueUrl)
        }

        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
        verifyAudit()
      }

      "throw an exception when the query string is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }
    }
  }

}
