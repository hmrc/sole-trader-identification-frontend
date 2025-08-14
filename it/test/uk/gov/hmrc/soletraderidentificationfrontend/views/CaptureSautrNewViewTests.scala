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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, CaptureSautr => messages, Header}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureSautrNewViewTests {
  this: ComponentSpecHelper =>

  def testCaptureSautrView(result: => WSResponse): Unit = {
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
      doc.title mustBe messages.new_title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.new_heading
    }

    "have the correct first line" in {
      doc.getParagraphs.get(1).text mustBe messages.new_line_1
    }

    "have the correct input options" in {
      val radios = doc.getRadioButtons
      radios.size mustBe 2
      radios.first.attr("value") mustBe Base.yes
      radios.get(1).attr("value") mustBe Base.no

      doc.getLabelElement.get(1).text mustBe messages.new_label
    }

    "have a continue button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCaptureSautrErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.new_title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalid_sautr_entered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalid_sautr_entered
    }
  }

  def testCaptureSautrRadioErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.new_title
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.new_no_selection
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.new_no_selection
    }
  }

  def testTitleAndHeadingInTheErrorView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe Base.Error.error + messages.new_title
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.new_heading
    }

  }

}
