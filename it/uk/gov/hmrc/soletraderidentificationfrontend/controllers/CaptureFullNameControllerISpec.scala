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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureFullNameViewTests

class CaptureFullNameControllerISpec extends ComponentSpecHelper
  with CaptureFullNameViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  val testFirstName = "John"
  val testLastName = "Smith"

  "GET /full-name" should {
    lazy val result = {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testIndividualJourneyConfig
      ))
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/full-name")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureFullNameView(result)
    }

    "return, given a persisted pageConfig with a custom full name page label, a view which" should {
        lazy val viewWithCustomFullNamePageLabel: WSResponse = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testIndividualJourneyConfig.copy(pageConfig = testIndividualPageConfig.copy(optFullNamePageLabel = Some(testFullNamePageLabel)))
          ))
          stubAuth(OK, successfulAuthResponse())
          get(s"/identify-your-sole-trader-business/$testJourneyId/full-name")
        }

        testCaptureFullNameViewWithCustomFullNameLabel(result = viewWithCustomFullNamePageLabel)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/full-name")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Ffull-name" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /full-name" when {
    "the whole form is correctly formatted" should {
      "redirect to the Capture Date of Birth page and store the data in the backend" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        stubStoreFullName(testJourneyId, FullName(testFirstName, testLastName))(status = OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> testFirstName,
          "last-name" -> testLastName
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureDateOfBirthController.show(testJourneyId).url)
        )
      }
    }

    "the whole form is missing" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> "",
          "last-name" -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessage(result)
    }

    "the first name is missing" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> "",
          "last-name" -> testLastName
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoFirstName(result)
    }

    "the last name is missing" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> testFirstName,
          "last-name" -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoLastName(result)
    }

    "the first name and last name are missing" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> "",
          "last-name" -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoFirstNameAndLastName(result)
    }

    "the first name is invalid" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> "00000",
          "last-name" -> testLastName
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageInvalidFirstName(result)
    }

    "the last name is invalid" should {
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> testFirstName,
          "last-name" -> "00000"
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageInvalidLastName(result)
    }

    "there is a form error and the given persisted pageConfig has a custom full name page label" should {
      lazy val viewWithCustomFullNamePageLabel = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testIndividualJourneyConfig.copy(pageConfig = testIndividualPageConfig.copy(optFullNamePageLabel = Some(testFullNamePageLabel)))
        ))
        stubAuth(OK, successfulAuthResponse())
        post(uri = s"/identify-your-sole-trader-business/$testJourneyId/full-name")(
          "first-name" -> testFirstName,
          "last-name" -> ""
        )
      }
      testCaptureFullNameErrorViewWithCustomFullNameLabel(result = viewWithCustomFullNamePageLabel)
    }
  }
}
