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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, DetailsNotFound => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait DetailsNotFoundViewTests {
  this: ComponentSpecHelper =>

  val four: Int = 4

  def testDetailsNotFoundView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have the correct title" in {
      doc.title mustBe messages.title
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

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.get(1).text mustBe messages.line_1
    }

    "have the correct second line" in {
      doc.getParagraphs.get(2).text mustBe messages.line_2
    }

    "have the correct link to try again" in {
      val links: Elements = doc.getLink(id = "TryAgain")
      links.size mustBe 1
      links.first.text mustBe messages.link_2
      links.first.attr("href") mustBe routes.RetryJourneyController.tryAgain(testJourneyId).url
    }

    "have the correct third line" in {
      doc.getParagraphs.get(3).text mustBe messages.line_3
    }

    "have the correct fourth line" in {
      doc.getParagraphs.get(four).text mustBe messages.line_4
    }

    "have the correct link text for contacting Nino Team" in {
      doc.getLink(id = "ninoTeam").text mustBe messages.link_4
    }
  }
}
