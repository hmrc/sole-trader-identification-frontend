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

package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, CaptureFullName => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureFullNameViewTests {
  this: ComponentSpecHelper =>

  def testCaptureFullNameView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.get(0).text mustBe messages.line_1
    }

    "have correct labels in the form" in {
      doc.getLabelElement.first.text() mustBe messages.form_field_1
      doc.getLabelElement.get(1).text() mustBe messages.form_field_2
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

  }

  def testCaptureFullNameErrorMessage(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noFirstNameEntered + " " + messages.Error.noLastNameEntered
    }
    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noFirstNameEntered + " " + Base.Error.error + messages.Error.noLastNameEntered
    }
  }

  def testCaptureFullNameErrorMessageNoFirstName(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noFirstNameEntered
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noFirstNameEntered
    }
  }

  def testCaptureFullNameErrorMessageNoLastName(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noLastNameEntered
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noLastNameEntered
    }
  }

  def testCaptureFullNameErrorMessageNoFirstNameAndLastName(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noFirstNameEntered + " " + messages.Error.noLastNameEntered
    }
    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noFirstNameEntered + " " + Base.Error.error + messages.Error.noLastNameEntered
    }
  }

}
