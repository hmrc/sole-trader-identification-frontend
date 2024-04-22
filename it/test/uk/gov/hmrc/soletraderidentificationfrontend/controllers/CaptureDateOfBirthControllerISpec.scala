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
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureDateOfBirthViewTests

import java.time.LocalDate

class CaptureDateOfBirthControllerISpec extends ComponentSpecHelper with CaptureDateOfBirthViewTests with SoleTraderIdentificationStub with AuthStub {

  "GET /date-of-birth" should {
    lazy val result = {
      await(
        journeyConfigRepository.insertJourneyConfig(
          journeyId      = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig  = testIndividualJourneyConfig
        )
      )
      stubAuth(OK, successfulAuthResponse())
      stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
      get(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureDateOfBirthView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(
            "/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fdate-of-birth" +
              "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /date-of-birth" when {
    "the whole form is correctly formatted" should {
      "redirect to the Capture Nino page and store the data in the backend" in {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubStoreDob(testJourneyId, testDateOfBirth)(status = OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> testDateOfBirth.getYear.toString
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureNinoController.show(testJourneyId).url)
        )
      }
    }

    "the whole form is missing" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")()
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessage(result)
    }

    "the date of birth is missing" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "",
          "date-of-birth-month" -> "",
          "date-of-birth-year"  -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoDob(result)
    }

    "a future year is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> "2050"
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageFutureDate(result)
    }

    "not real date is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "31",
          "date-of-birth-month" -> "02",
          "date-of-birth-year"  -> "2020"
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNotRealDate(result)
    }

    "an invalid date is submitted" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "31",
          "date-of-birth-month" -> "O2", // 0 => O
          "date-of-birth-year"  -> "2020"
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidDate(result)
    }

    "the dob submitted is less than 16 years ago" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> LocalDate.now.minusYears(10).getYear.toString
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidAge(result)
    }

    "the year in the date of birth is before 1900" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> "1899"
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessageYearBeforeNineteenHundred(result)
    }

    "the day component of the date of birth submitted is missing" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "",
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> testDateOfBirth.getYear.toString
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageMissingDay(result)
    }

    "the month component of the date of birth submitted is missing" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> "",
          "date-of-birth-year"  -> testDateOfBirth.getYear.toString
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessagesMissingMonth(result)
    }

    "the year component of the date of birth submitted is missing" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year"  -> ""
        )
      }
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessagesMissingYear(result)
    }

    "the day and month components of the date of birth submitted are missing" should {

      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "",
          "date-of-birth-month" -> "",
          "date-of-birth-year"  -> testDateOfBirth.getYear.toString
        )
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessagesMissingDayAndMonth(result)
    }

    "the day and year components of the date of birth submitted are missing" should {

      lazy val result: WSResponse = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "",
          "date-of-birth-month" -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-year"  -> ""
        )
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessagesMissingDayAndYear(result)
    }

    "the month and year components of the date of birth submitted are missing" should {
      lazy val result: WSResponse = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))
        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> "",
          "date-of-birth-year"  -> ""
        )
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureDateOfBirthErrorMessagesMissingMonthAndYear(result)
    }

    "there is a form error and customer full name exists" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullName(testFirstName, testLastName)))

        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "31",
          "date-of-birth-month" -> "02",
          "date-of-birth-year"  -> "to simulate an error"
        )
      }
      testTitleAndHeadingInTheErrorView(result)
    }

    "there is a form error and customer full name does NOT exist" should {
      lazy val result = {
        await(
          journeyConfigRepository.insertJourneyConfig(
            journeyId      = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig  = testIndividualJourneyConfig
          )
        )
        stubAuth(OK, successfulAuthResponse())

        post(s"/identify-your-sole-trader-business/$testJourneyId/date-of-birth")(
          "date-of-birth-day"   -> "31",
          "date-of-birth-month" -> "02",
          "date-of-birth-year"  -> "to simulate an error"
        )
      }

      "return an internal server error" in {
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      testTitleAndHeadingGivenNoCustomerFullName(result)
    }
  }
}
