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
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CouldNotConfirmBusiness => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testJourneyId, testSignOutUrl, testTechnicalHelpUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CouldNotConfirmBusinessErrorViewTests {
  this: ComponentSpecHelper =>

  def testCouldNotConfirmBusinessView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    lazy val config: AppConfig = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header with the correct text" in {
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

    "have the correct first line" in {
      doc.getParagraphs.get(1).text mustBe messages.line_1
    }

    "have a try again link configured to appear link as a button" in {
      val links: Elements = doc.getLink("tryAgain")
      links.size mustBe 1
      links.first.attr("class") mustBe "govuk-button"
      links.first.attr("href") mustBe routes.RetryJourneyController.tryAgain(testJourneyId).url
      links.first.attr("role") mustBe "button"
      links.first.attr("data-module") mustBe "govuk-button"
      links.first.text mustBe Base.try_again
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }

  }

}
