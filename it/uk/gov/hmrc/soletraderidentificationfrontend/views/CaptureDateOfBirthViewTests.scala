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

package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureDateOfBirth => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions


trait CaptureDateOfBirthViewTests {
  this: ComponentSpecHelper =>

  def testTitleAndHeadingInTheErrorView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

  }

  def testTitleAndHeadingGivenNoCustomerFullName(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.technicalDifficultiesTitle
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe Base.technicalDifficultiesHeading
    }

  }

  def testCaptureDateOfBirthView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have a back link" in {
      doc.getBackLinkText mustBe Base.back
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

    "have the correct hint" in {
      doc.getElementsByClass("govuk-hint").text() mustBe messages.hint
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }

  }

  def testCaptureDateOfBirthErrorMessage(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noDobEntered
    }

    "correctly define link to erroneous input" in {
        doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noDobEntered
    }
    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessageNoDob(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noDobEntered
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noDobEntered
    }
    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessageFutureDate(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.futureDate
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.futureDate
    }
    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessageNotRealDate(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.notRealDate
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.notRealDate
    }
    "assign error class to date input components" in {
      println(doc.body())
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessageInvalidDate(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidDate
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidDate
    }
    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include("govuk-input--error")
    }
  }
  def testCaptureDateOfBirthErrorMessageInvalidAge(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidAge
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidAge
    }
    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessageYearBeforeNineteenHundred(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidDate
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidDate
    }

    "assign error class to date input components" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }

  }

  def testCaptureDateOfBirthErrorMessageMissingDay(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingDay
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingDay
    }

    "assign error class to day input input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must not include "govuk-input--error"
    }
  }

  def testCaptureDateOfBirthErrorMessagesMissingMonth(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingMonth
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-month"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingMonth
    }

    "assign error class to month input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must not include "govuk-input--error"
    }
  }

  def testCaptureDateOfBirthErrorMessagesMissingYear(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingYear
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-year"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingYear
    }

    "assign error class to year input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessagesMissingDayAndMonth(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingDayAndMonth
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingDayAndMonth
    }

    "assign error class to year input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must not include "govuk-input--error"
    }

  }

  def testCaptureDateOfBirthErrorMessagesMissingDayAndYear(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingDayAndYear
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-day"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingDayAndYear
    }

    "assign error class to year input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

  def testCaptureDateOfBirthErrorMessagesMissingMonthAndYear(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.missingMonthAndYear
    }

    "correctly define link to erroneous input" in {
      doc.getErrorSummaryLink.attr("href") mustBe "#date-of-birth-month"
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.missingMonthAndYear
    }

    "assign error class to year input component" in {
      doc.getTextFieldInput("date-of-birth-day").first().attr("class") must not include "govuk-input--error"
      doc.getTextFieldInput("date-of-birth-month").first().attr("class") must include ("govuk-input--error")
      doc.getTextFieldInput("date-of-birth-year").first().attr("class") must include ("govuk-input--error")
    }
  }

}
