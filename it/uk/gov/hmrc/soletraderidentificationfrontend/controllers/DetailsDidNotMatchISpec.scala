package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.http.Status.NOT_FOUND
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{signInRedirectUrl, testInternalId, testJourneyId, testSoleTraderJourneyConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.DetailsDidNotMatchViewTests

class DetailsDidNotMatchISpec extends ComponentSpecHelper with DetailsDidNotMatchViewTests with AuthStub {

  "GET /details-did-not-match" should {
    lazy val result = {
      await(
        journeyConfigRepository.insertJourneyConfig(
          journeyId      = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig  = testSoleTraderJourneyConfig
        )
      )
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/details-did-not-match")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testDetailsDidNotMatchView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/details-did-not-match")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(signInRedirectUrl(testJourneyId, "details-did-not-match"))
        )
      }
    }
  }

  "POST /details-did-not-match" should {

    "return not found" in {

      lazy val result: WSResponse = post(s"/identify-your-sole-trader-business/$testJourneyId/details-not-found")()

      result.status mustBe NOT_FOUND
    }
  }
}
