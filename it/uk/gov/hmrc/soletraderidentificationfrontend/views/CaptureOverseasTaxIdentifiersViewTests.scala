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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureOverseasTaxIdentifiers => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testSignOutUrl

trait CaptureOverseasTaxIdentifiersViewTests {
  this: ComponentSpecHelper =>

  def testCaptureCaptureOverseasTaxIdentifiersView(result: => WSResponse): Unit = {
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
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the correct hint text" in {
      doc.getElementsByClass("govuk-hint").text mustBe messages.hint
    }

    "have correct labels in the form" in {
      doc.getLabelElement.first.text() mustBe messages.form_field_1
      doc.getLabelElement.get(1).text() mustBe messages.form_field_2
    }

    "have a correct skip link" in {
      doc.getElementById("no-overseas-tax-identifiers").text() mustBe messages.no_identifierLink
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.no_entry_tax_identifier + " " + messages.Error.no_entry_country
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.no_entry_tax_identifier
      doc.getFieldErrorMessage.get(1).text mustBe Base.Error.error + messages.Error.no_entry_country
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessagesInvalidIdentifier(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.invalid_tax_identifier
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.invalid_tax_identifier
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessagesTooLongIdentifier(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.invalid_length_tax_identifier
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.invalid_length_tax_identifier
    }
  }


}
