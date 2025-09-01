/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, CaptureOverseasTaxIdentifiersCountry => messages, Header}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureOverseasTaxIdentifierCountryViewTests {
  this: ComponentSpecHelper =>

  def testCaptureCaptureOverseasTaxIdentifiersCountryView(result: => WSResponse): Unit = {
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
      doc.getLabelElement.text mustBe messages.heading
    }

    "have a continue button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCaptureCaptureOverseasTaxIdentifiersErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.get(0).text mustBe messages.Error.no_entry_country
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.get(0).text mustBe Base.Error.error + messages.Error.no_entry_country
    }
  }

}
