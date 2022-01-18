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

import play.api.Application
import play.api.http.Status.FORBIDDEN
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsBoolean, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{EnableNoNinoJourney, KnownFactsStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DeceasedCitizensDetails, DetailsMismatch, NinoNotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests

import scala.concurrent.ExecutionContext.Implicits.global

class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with SoleTraderIdentificationStub
  with AuthStub
  with AuthenticatorStub
  with BusinessVerificationStub
  with WiremockHelper
  with CreateTrnStub
  with KnownFactsStub
  with RegisterStub {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  def extraConfig = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    super.beforeEach()
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
          "the user has a sautr and a nino" in {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testSoleTraderJourneyConfig
            ))
            stubAuth(OK, successfulAuthResponse())
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
            stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
            stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
            stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
            stubRetrieveNino(testJourneyId)(OK, testNino)
            stubRetrieveAddress(testJourneyId)(NOT_FOUND)
            stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
            stubAudit()

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(testBusinessVerificationRedirectUrl)
            }

            verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
            verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
            verifyAudit()
          }
          "the user does not have a nino" in {
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
            stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
            stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, None))(OK)
            stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
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
            verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
            verifyAudit()
          }
        }
      }

      "redirect to continue url" when {
        "the sautr is not provided but the details match what is held in the database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationUnchallengedJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
        }

        "business verification does not have enough information to identify the user" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(NOT_FOUND, Json.obj())
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationUnchallengedJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
        }

        "the user has been locked out of business verification" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(FORBIDDEN, Json.obj())
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationFailJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr)
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
          stubRetrieveAddress(testJourneyId)(OK, Json.toJson(testAddress))
          stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))
          stubStoreTrn(testJourneyId, testTrn)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyAudit()
        }

        "the nino is provided and the user enters a wrong postcode format" in {
          enable(EnableNoNinoJourney)
          enable(KnownFactsStub)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr)
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
          stubRetrieveAddress(testJourneyId)(OK, Json.toJson(testAddressWrongPostcodeFormat))
          stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))
          stubStoreTrn(testJourneyId, testTrn)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyAudit()
        }
        "the user enter their full name in lowercase" in {
          enable(EnableNoNinoJourney)
          enable(KnownFactsStub)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNinoNoSautr)
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullNameLowerCase))
          stubRetrieveAddress(testJourneyId)(OK, Json.toJson(testAddress))
          stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))
          stubStoreTrn(testJourneyId, testTrn)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
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
            stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
            stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
            stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
            stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
            stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
            stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
            stubAudit()
            stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
            stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DetailsMismatch")
            stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationUnchallengedJson)
            stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

            val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
            }

            verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
            verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
            verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
            verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
            verifyAudit()
          }
          "the user does not have a nino" when {
            "the SA postcode does not match postcode returned from ES20" in {
              enable(EnableNoNinoJourney)
              await(journeyConfigRepository.insertJourneyConfig(
                journeyId = testJourneyId,
                authInternalId = testInternalId,
                journeyConfig = testSoleTraderJourneyConfig
              ))
              stubAuth(OK, successfulAuthResponse())
              stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino)
              stubRetrieveSaPostcode(testJourneyId)(OK, testPostcode)
              stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
              stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseNino)
              stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
              stubStoreES20Details(testJourneyId, KnownFactsResponse(Some(testSaPostcode), None, Some(testNino)))(OK)
              stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
              stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
              stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
              stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
              stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
              stubAudit()

              val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

              result must have {
                httpStatus(SEE_OTHER)
                redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
              }

              verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
              verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
              verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
              verifyAudit()
            }
            "the user does not provide a SA postcode and ES20 returns N for isAbroad flag" in {
              enable(EnableNoNinoJourney)
              await(journeyConfigRepository.insertJourneyConfig(
                journeyId = testJourneyId,
                authInternalId = testInternalId,
                journeyConfig = testSoleTraderJourneyConfig
              ))
              stubAuth(OK, successfulAuthResponse())
              stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoNino)
              stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
              stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
              stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseIsAbroad("N"))
              stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
              stubStoreES20Details(testJourneyId, KnownFactsResponse(None, Some(false), None))(OK)
              stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
              stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
              stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
              stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
              stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
              stubAudit()

              val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

              result must have {
                httpStatus(SEE_OTHER)
                redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
              }

              verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
              verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
              verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
              verifyAudit()
            }
          }
        }

        "the provided details are for a deceased citizen" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testSoleTraderJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(FAILED_DEPENDENCY, Json.obj())
          stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DeceasedCitizensDetails")
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationUnchallengedJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(UNAUTHORIZED, notFoundErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "NinoNotFound")
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationUnchallengedJson)
          stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAudit()
        }
      }
    }

    "the sautr check is disabled" should {
      "redirect to continue url" when {
        "the provided details match what is held in the database" in {
          enable(EnableNoNinoJourney)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testIndividualJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
          stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
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
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(NOT_FOUND)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorDetails(testJourneyId)(NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
          stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAudit()
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, mismatchErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DetailsMismatch")

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAudit()
        }

        "the provided details are for a deceased citizen" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testIndividualJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(FAILED_DEPENDENCY, Json.obj())
          stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "DeceasedCitizensDetails")

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAudit()
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, notFoundErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(false))
          stubRetrieveAuthenticatorFailureResponse(testJourneyId)(OK, "NinoNotFound")

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAudit()
        }
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
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)

          stubRegister(testNino, testSautr)(OK, Registered(testSafeId))

          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(OK)
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyRegister(testNino, testSautr)
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
          stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.CannotConfirmBusinessErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)

          verifyAudit()

        }

      }
    }
  }

}
