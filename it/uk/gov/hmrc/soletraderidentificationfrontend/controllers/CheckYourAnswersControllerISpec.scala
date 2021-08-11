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
import play.api.libs.json.{JsBoolean, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass, BusinessVerificationUnchallenged, FullName, Registered, RegistrationNotCalled}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DeceasedCitizensDetails, DetailsMismatch, NinoNotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, AuthenticatorStub, BusinessVerificationStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests

class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with SoleTraderIdentificationStub
  with AuthStub
  with AuthenticatorStub
  with BusinessVerificationStub
  with WiremockHelper {

  def extraConfig = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  "GET /check-your-answers-business" when {
    "the applicant has a nino and an sautr" should {
      lazy val result: WSResponse = {
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
        stubAudit()
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
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

    "the applicant only has a nino" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersNoSautrView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()

          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          }
        }
      }
    }
  }

  "POST /check-your-answers-business" when {
    "the sautr check is enabled" should {
      "redirect to business verification url" when {
        "the provided details match what is held in the database" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testBusinessVerificationRedirectUrl))
          stubAudit()

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testBusinessVerificationRedirectUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyAudit()
        }
      }

      "redirect to continue url" when {
        "the sautr is not provided but the details match what is held in the database" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
          verifyAudit()
        }

        "business verification does not have enough information to identify the user" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(NOT_FOUND, Json.obj())
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyAudit()
        }

        "the user has been locked out of business verification" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(FORBIDDEN, Json.obj())
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationFail))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetails)
          verifyAudit()
        }
      }

      "redirect to personal information error controller" when {
        "the provided details do not match what is held in the database" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
          verifyAudit()
        }
      }

      "redirect to personal information error controller" when {
        "the provided details are for a deceased citizen" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(FAILED_DEPENDENCY, Json.obj())
          stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
          verifyAudit()
        }
      }

      "redirect to details not found controller" when {
        "the provided details do not exist in the database" in {
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
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubMatch(testIndividualDetails)(UNAUTHORIZED, notFoundErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetails))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
          verifyAudit()
        }
      }
    }

    "the sautr check is disabled" should {
      "redirect to continue url" when {
        "the provided details match what is held in the database" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = false
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
          stubStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(testContinueUrl)
          }

          verifyStoreAuthenticatorDetails(testJourneyId, testIndividualDetailsNoSautr)
          verifyAudit()
        }
      }

      "redirect to personal information error page" when {
        "the provided details do not match what is held in the database" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = false
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, mismatchErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DetailsMismatch)
          verifyAudit()
        }
      }

      "redirect to personal information error page" when {
        "the provided details are for a deceased citizen" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = false
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(FAILED_DEPENDENCY, Json.obj())
          stubStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, DeceasedCitizensDetails)
          verifyAudit()
        }
      }

      "redirect to details not found page" when {
        "the provided details do not exist in the database" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = false
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveIndividualDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubMatch(testIndividualDetailsNoSautr)(UNAUTHORIZED, notFoundErrorJson)
          stubStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)(OK)
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)
          stubAudit()
          stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveNino(testJourneyId)(OK, testNino)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, Json.toJson(testIndividualDetailsNoSautr))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationUnchallenged))
          stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationNotCalled))

          val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(routes.DetailsNotFoundController.show(testJourneyId).url)
          }

          verifyStoreAuthenticatorFailureResponse(testJourneyId, NinoNotFound)
          verifyAudit()
        }
      }
    }
  }

}