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

import com.github.tomakehurst.wiremock.client.WireMock
import play.api.Application
import play.api.http.Status.FORBIDDEN
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{EnableNoNinoJourney, KnownFactsStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit, verifyAuditTypeFor}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests

class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with SoleTraderIdentificationStub
  with AuthStub
  with AuthenticatorStub
  with BusinessVerificationStub
  with WiremockHelper
  with CreateTrnStub
  with KnownFactsStub
  with RegisterStub
  with NinoInsightsStub {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    WireMock.resetAllScenarios()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    WireMock.resetAllScenarios()
    super.afterEach()
  }

  "GET /check-your-answers-business" when {
    "the applicant has a nino and an sautr" should {
      lazy val result: WSResponse = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
        stubRetrieveAddress(testJourneyId)(NOT_FOUND)
        stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK

        verifyAudit()
      }

      "return a view which" should {
        testCheckYourAnswersFullView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          }

          verifyAudit()
        }
      }
    }

    "the applicant does not have a sautr" should {
      lazy val result: WSResponse = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
        stubRetrieveAddress(testJourneyId)(NOT_FOUND)
        stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK

        verifyAudit()
      }

      "return a view which" should {
        testCheckYourAnswersNoSautrView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAudit()
          stubAuthFailure()

          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          }

          verifyAudit()
        }
      }
    }

    "the applicant does not have a nino but has an address" should {
      lazy val result: WSResponse = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino)
        stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK

        verifyAudit()
      }

      "return a view which" should {
        testCheckYourAnswersNoNinoView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          }

          verifyAudit()
        }
      }
    }

    "the applicant doesn't have a nino on the individual flow" should {
      lazy val result: WSResponse = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr)
        stubRetrieveAddress(testJourneyId)(NOT_FOUND)
        stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK

        verifyAudit()
      }

      "return a view which" should {
        testCheckYourAnswersNoNinoIndividualFlowView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          }

          verifyAudit()
        }
      }
    }
  }

  "POST /check-your-answers-business" when {
    "the sautr check is enabled" should {
      "redirect to business verification url" when {
        "the provided details match what is held in the database" when {
          "the user has a sautr and a nino with no IR-SA enrolment" in {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse())
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
            stubNinoInsights(testNino)(OK, testInsightsReturnBody)
            stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
            stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
            stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
            stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
            stubRetrieveNino(testJourneyId)(OK, testNino)
            stubRetrieveAddress(testJourneyId)(NOT_FOUND)
            stubCreateBusinessVerificationJourney(testSautr, testJourneyId, testSoleTraderJourneyConfig)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
            stubAudit()

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(testBusinessVerificationRedirectUrl)
            }

            verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
            verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
            verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
            verifyAudit()
          }
          "the user does not have a nino or IR-SA enrolment" in {
            enable(EnableNoNinoJourney)
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse())
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino)
            stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
            stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
            stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponse)
            stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
            stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(OK)
            stubCreateBusinessVerificationJourney(testSautr, testJourneyId, testSoleTraderJourneyConfig)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
            stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
            stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
            stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
            stubAudit()

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(testBusinessVerificationRedirectUrl)
            }

            verifyStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))
            verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
            verifyAudit()
          }
          "the user has an IR-SA enrolment that doesn't match" in {
            val IrSAEnrolmentSautr = "1234567891"
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse(irSaEnrolment(IrSAEnrolmentSautr)))
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
            stubNinoInsights(testNino)(OK, testInsightsReturnBody)
            stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
            stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
            stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
            stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
            stubRetrieveNino(testJourneyId)(OK, testNino)
            stubRetrieveAddress(testJourneyId)(NOT_FOUND)
            stubCreateBusinessVerificationJourney(testSautr, testJourneyId, testSoleTraderJourneyConfig)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
            stubAudit()

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(testBusinessVerificationRedirectUrl)
            }

            verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
            verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
            verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
            verifyAudit()
          }
        }
      }

      "redirect to continue url" when {
        "the user has been locked out of business verification" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())

          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson).setNewScenarioState("auditing")
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId, testSoleTraderJourneyConfig)(FORBIDDEN, Json.obj())
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson ++ Json.obj("identifiersMatch" -> SuccessfulMatchKey)).setRequiredScenarioState("auditing")
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatch)
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationFailJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
          verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
        }
        "the sautr and nino are not provided" in {
          enable(EnableNoNinoJourney)
          enable(KnownFactsStub)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr).setNewScenarioState("auditing")
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
          stubStoreIdentifiersMatch(testJourneyId, NotEnoughInformationToMatch)(OK)
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
          stubRetrieveAddress(testJourneyId)(OK, Json.toJson(testAddress))
          stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))
          stubStoreTrn(testJourneyId, testTrn)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr ++ Json.obj("identifiersMatch" -> NotEnoughInfoToMatchKey)).setRequiredScenarioState("auditing")
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, NotEnoughInformationToMatch)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, JsString(NotEnoughInfoToMatchKey))
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
        }
        "the user has an IR-SA enrolment with matching sautr" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse(irSaEnrolment(testSautr)))
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, SaEnrolled)(OK)
          stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
          verifyStoreBusinessVerificationStatus(testJourneyId, SaEnrolled)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
          verifyAudit()
        }
      }

      "redirect to cannot confirm business error controller" when {
        "the provided details do not match what is held in the database" when {
          "the user has a nino" in {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse())
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson).setNewScenarioState("auditing")
            stubNinoInsights(testNino)(OK, testInsightsReturnBody)
            stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
            stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
            stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
            stubStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(OK)
            stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
            stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
            stubAudit()
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson ++ Json.obj("identifiersMatch" -> DetailsMismatchKey)).setRequiredScenarioState("auditing")
            stubRetrieveIdentifiersMatch(testJourneyId)(OK, DetailsMismatch)
            stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DetailsMismatch")
            stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
            stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
            stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
            }

            verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
            verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
            verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
            verifyStoreIdentifiersMatch(testJourneyId, JsString(DetailsMismatchKey))
            verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
            verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
          }
        }
        "the user does not have a nino (and KnowFacts does not have a nino either)" when {
          "the SA postcode does not match postcode returned from ES20" in {
            enable(EnableNoNinoJourney)
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse())
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino).setNewScenarioState("auditing")
            stubRetrieveSaPostcode(testJourneyId)(OK, testPostcode)
            stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
            stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseWithoutNino)
            stubStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(OK)
            stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(OK)
            stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
            stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
            stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
            stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
            stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
            stubAudit()
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino ++ Json.obj("identifiersMatch" -> DetailsMismatchKey)).setRequiredScenarioState("auditing")
            stubRetrieveES20Result(testJourneyId)(OK, testKnownFactsResponse)
            stubRetrieveIdentifiersMatch(testJourneyId)(OK, DetailsMismatch)

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
            }

            verifyStoreIdentifiersMatch(testJourneyId, JsString(DetailsMismatchKey))
            verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
            verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
            verifyAudit()
          }
        }
        "the provided details are for a deceased citizen" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson).setNewScenarioState("auditing")
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(FAILED_DEPENDENCY, Json.obj())
          stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson ++ Json.obj("identifiersMatch" -> DeceasedCitizensDetailsKey)).setRequiredScenarioState("auditing")
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, DeceasedCitizensDetails)
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DeceasedCitizensDetails")
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(DeceasedCitizensDetailsKey))
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
          verifyAudit()
        }
      }

      "redirect to details not found controller" when {
        "the provided details do not exist in the database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson).setNewScenarioState("auditing")
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(UNAUTHORIZED, notFoundErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
          stubStoreIdentifiersMatch(testJourneyId, NinoNotFound)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson ++ Json.obj("identifiersMatch" -> NinoNotFoundKey)).setRequiredScenarioState("auditing")
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, NinoNotFound)
          stubRetrieveInsights(testJourneyId)(OK, testInsightsReturnBody)
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "NinoNotFound")
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(NinoNotFoundKey))
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
          verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
        }
      }

      "redirect to 'we could not confirm your business' controller" when {
        "the user declares SaUTR and no Nino but KnownFacts found one" in {
          enable(EnableNoNinoJourney)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())

          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino).setNewScenarioState("auditing")
          stubRetrieveSaPostcode(testJourneyId)(OK, testPostcode)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
          stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseNino(testNinoRecordedByKnownFacts))
          stubStoreIdentifiersMatch(testJourneyId, NinoNotDeclaredButFound)(OK)
          stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, Some(testNinoRecordedByKnownFacts)))(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          stubAudit()
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino ++ Json.obj("identifiersMatch" -> NinoNotDeclaredButFoundKey)).setRequiredScenarioState("auditing")
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, NinoNotDeclaredButFound)
          stubRetrieveAuthenticatorDetails(testJourneyId)(NOT_FOUND)
          stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CouldNotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(NinoNotDeclaredButFoundKey))
          verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
        }
      }
    }
  }

  "individual journey: the sautr check is disabled (KnowFacts is not contacted in this scenario)" should {
    "redirect to continue url" when {
      "the provided details match what is held in the database" in {
        enable(EnableNoNinoJourney)
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr).setNewScenarioState("auditing")
        stubNinoInsights(testNino)(OK, testInsightsReturnBody)
        stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
        stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
        stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(OK)
        stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr ++ Json.obj("identifiersMatch" -> SuccessfulMatchKey)).setRequiredScenarioState("auditing")
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveInsights(testJourneyId)(OK, testInsightsReturnBody)
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatch)
        stubRetrieveAuthenticatorDetails(testJourneyId)(NOT_FOUND)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
        stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(testContinueUrl)
        }

        verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
        verifyStoreIdentifiersMatch(testJourneyId, JsString(SuccessfulMatchKey))
        verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
        verifyAuditTypeFor(auditTypeToBeFound = "IndividualIdentification")
      }
      "the user does not have a nino" in {
        enable(EnableNoNinoJourney)
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr)
        stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
        stubStoreIdentifiersMatch(testJourneyId, NotEnoughInformationToMatch)(OK)
        stubAudit()
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(NOT_FOUND)
        stubRetrieveInsights(testJourneyId)(NOT_FOUND)
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, NotEnoughInformationToMatch)
        stubRetrieveAuthenticatorDetails(testJourneyId)(NOT_FOUND)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
        stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(testContinueUrl)
        }

        verifyStoreIdentifiersMatch(testJourneyId, JsString(NotEnoughInfoToMatchKey))
        verifyAuditTypeFor(auditTypeToBeFound = "IndividualIdentification")
      }
    }

    "redirect to cannot confirm business error controller" when {
      "the provided details do not match what is held in the database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr).setNewScenarioState("auditing")
        stubNinoInsights(testNino)(OK, testInsightsReturnBody)
        stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
        stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, mismatchErrorJson)
        stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
        stubStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(OK)
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr ++ Json.obj("identifiersMatch" -> DetailsMismatchKey)).setRequiredScenarioState("auditing")
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveInsights(testJourneyId)(OK, testInsightsReturnBody)
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, DetailsMismatch)
        stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DetailsMismatch")

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
        }

        verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
        verifyStoreIdentifiersMatch(testJourneyId, JsString(DetailsMismatchKey))
        verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
        verifyAuditTypeFor(auditTypeToBeFound = "IndividualIdentification")
      }
      "the provided details are for a deceased citizen" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr).setNewScenarioState("auditing")
        stubNinoInsights(testNino)(OK, testInsightsReturnBody)
        stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
        stubMatch(testIndividualDetailsNoSautr)(FAILED_DEPENDENCY, Json.obj())
        stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
        stubStoreIdentifiersMatch(testJourneyId, DeceasedCitizensDetails)(OK)
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr ++ Json.obj("identifiersMatch" -> DeceasedCitizensDetailsKey)).setRequiredScenarioState("auditing")
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveInsights(testJourneyId)(OK, testInsightsReturnBody)
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, DeceasedCitizensDetails)
        stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DeceasedCitizensDetails")

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
        }

        verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
        verifyStoreIdentifiersMatch(testJourneyId, JsString(DeceasedCitizensDetailsKey))
        verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
        verifyAuditTypeFor(auditTypeToBeFound = "IndividualIdentification")
      }
    }

    "redirect to details not found controller" when {
      "the provided details do not exist in the database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr).setNewScenarioState("auditing")
        stubNinoInsights(testNino)(OK, testInsightsReturnBody)
        stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
        stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, notFoundErrorJson)
        stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
        stubStoreIdentifiersMatch(testJourneyId, NinoNotFound)(OK)
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr ++ Json.obj("identifiersMatch" -> NinoNotFoundKey)).setRequiredScenarioState("auditing")
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        stubRetrieveInsights(testJourneyId)(OK, testInsightsReturnBody)
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, NinoNotFound)
        stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "NinoNotFound")

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
        }

        verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
        verifyStoreIdentifiersMatch(testJourneyId, JsString(NinoNotFoundKey))
        verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
        verifyAuditTypeFor(auditTypeToBeFound = "IndividualIdentification")
      }
    }
  }

  "businessVerificationCheck false scenario: POST /check-your-answers-business" should {
    "redirect to journey config continue url" when {
      "the provided details match what is held in the database" when {
        "the user has a sautr and a nino" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatch)(OK)

          stubRegister(testNino, Some(testSautr), testRegime)(OK, testBackendSuccessfulRegistrationJson)

          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyRegister(testNino, Some(testSautr), testRegime)
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)
          verifyAudit()
        }
      }
    }

    "redirect to cannot confirm business error controller" when {
      "the provided details do not match what is held in the database" when {
        "the user has a nino" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubNinoInsights(testNino)(OK, testInsightsReturnBody)
          stubStoreNinoInsights(testJourneyId, testInsightsReturnBody)(OK)
          stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
          stubStoreIdentifiersMatch(testJourneyId, DetailsMismatch)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreIdentifiersMatch(testJourneyId, JsString(DetailsMismatchKey))
          verifyStoreNinoInsights(testJourneyId, testInsightsReturnBody)

          verifyAudit()
        }
      }
    }

    "redirect to 'we could not confirm your business' controller" when {
      "the user declare no Nino but KnownFacts found one" in {
        enable(EnableNoNinoJourney)
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testSoleTraderJourneyConfig.copy(businessVerificationCheck = false)
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino).setNewScenarioState("auditing")
        stubRetrieveSaPostcode(testJourneyId)(OK, testPostcode)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
        stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseNino(testNinoRecordedByKnownFacts))
        stubStoreIdentifiersMatch(testJourneyId, NinoNotDeclaredButFound)(OK)
        stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, Some(testNinoRecordedByKnownFacts)))(OK)
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino ++ Json.obj("identifiersMatch" -> NinoNotDeclaredButFoundKey)).setRequiredScenarioState("auditing")
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
        stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, NinoNotDeclaredButFound)
        stubRetrieveES20Result(testJourneyId)(NOT_FOUND)

        val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(routes.CouldNotConfirmBusinessErrorController.show(testJourneyId).url)
        }

        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verifyStoreIdentifiersMatch(testJourneyId, JsString(NinoNotDeclaredButFoundKey))
        verifyAuditTypeFor(auditTypeToBeFound = "SoleTraderRegistration")
      }
    }
  }

}