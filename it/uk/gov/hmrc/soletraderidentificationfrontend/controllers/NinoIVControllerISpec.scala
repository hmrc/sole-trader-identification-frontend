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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, NinoIVJourneyStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SuccessfulMatch
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationPass, Registered}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}

class NinoIVControllerISpec
    extends ComponentSpecHelper
    with FeatureSwitching
    with AuthStub
    with NinoIVStub
    with RegisterStub
    with SoleTraderIdentificationStub
    with CreateTrnStub
    with BeforeAndAfterEach
    with WiremockHelper {

  def extraConfig = Map(
    "auditing.enabled"               -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build()

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    super.beforeEach()
  }

  "GET /identity-verification-result" when {
    s"the $NinoIVJourneyStub feature switch is enabled" should {
      "redirect to the continue url" when {
        "the verification status is PASS" in {
          await(
            journeyConfigRepository.insertJourneyConfig(
              journeyId      = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig  = testSoleTraderJourneyConfig
            )
          )
          enable(NinoIVJourneyStub)
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveNinoIVResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRetrieveSoleTraderDetails(testJourneyId)(OK, Json.toJson(testSoleTraderDetails))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveTrn(testJourneyId)(NOT_FOUND)
          stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatch)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testSuccessfulRegistrationJson)

          lazy val result = get(s"$baseUrl/$testJourneyId/identity-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          verifyAudit()
        }
      }

      "throw an exception when the query string is missing" in {
        enable(NinoIVJourneyStub)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNinoIVResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testSoleTraderJourneyConfig
          )
        )
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/identity-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }
    }

    s"the $NinoIVJourneyStub feature switch is disabled" should {
      "redirect to the continue url" when {
        "the verification status is PASS" in {
          await(
            journeyConfigRepository.insertJourneyConfig(
              journeyId      = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig  = testSoleTraderJourneyConfig
            )
          )

          stubAuth(OK, successfulAuthResponse())
          stubRetrieveNinoIVResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
          stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRetrieveSoleTraderDetails(testJourneyId)(OK, Json.toJson(testSoleTraderDetails))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveAddress(testJourneyId)(NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveTrn(testJourneyId)(NOT_FOUND)
          stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatch)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testSuccessfulRegistrationJson)

          lazy val result = get(s"$baseUrl/$testJourneyId/identity-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          verifyAudit()
        }
      }

      "throw an exception when the query string is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNinoIVResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubAudit()

        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testSoleTraderJourneyConfig
          )
        )

        lazy val result = get(s"$baseUrl/$testJourneyId/identity-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }
    }
  }

}
