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

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, CreateTrnStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CannotConfirmBusinessErrorViewTests

class CannotConfirmBusinessErrorControllerISpec extends ComponentSpecHelper
  with CannotConfirmBusinessErrorViewTests
  with SoleTraderIdentificationStub
  with CreateTrnStub
  with AuthStub {

  val testFirstName = "John"
  val testLastName = "Smith"

  "GET /cannot-confirm-business" should {
    lazy val result = {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testIndividualJourneyConfig
      ))
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/cannot-confirm-business")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCannotConfirmBusinessView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/cannot-confirm-business")
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcannot-confirm-business" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /cannot-confirm-business" when {
    "the user selects yes" when {
      "the user has previously provided a nino" should {
        "redirect to the contineurl" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testIndividualJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveNino(testJourneyId)(OK, testNino)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "yes"
          )
          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(testContinueUrl)
          )
        }
      }
      "the user has not provided a nino" should {
        "create a trn redirect to the contineurl" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testIndividualJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveNino(testJourneyId)(NOT_FOUND)
          stubRetrieveFullName(testJourneyId)(OK, Json.toJson(testFullName))
          stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
          stubRetrieveAddress(testJourneyId)(OK, testAddressJson)
          stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))
          stubStoreTrn(testJourneyId, testTrn)(OK)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "yes"
          )
          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(testContinueUrl)
          )
        }
      }
    }

    "the user selects no" should {
      "redirect to the capture fullname page" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRemoveAllData(testJourneyId)(NO_CONTENT)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/cannot-confirm-business")(
          "yes_no" -> "no"
        )
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureFullNameController.show(testJourneyId).url)
        )
      }
    }
  }
}
