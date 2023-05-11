package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, DetailsDidNotMatch => messages, Header}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait DetailsDidNotMatchViewTests {
  this: ComponentSpecHelper =>

  def testDetailsDidNotMatchView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

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

    "have the correct second line" in {
      doc.getParagraphs.get(2).text mustBe messages.line_2
    }

    "have the correct link to sign out" in {
      val links: Elements = doc.getLink(id = "sign_out")
      links.size mustBe 1
      links.first.text mustBe messages.button
      links.first.attr("href") mustBe testSignOutUrl
    }
  }

}
