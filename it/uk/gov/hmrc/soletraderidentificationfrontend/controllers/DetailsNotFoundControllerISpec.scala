
package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.DetailsNotFoundViewTests

class DetailsNotFoundControllerISpec extends ComponentSpecHelper
  with DetailsNotFoundViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  "GET /details-not-found" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        continueUrl = testContinueUrl,
        businessVerificationCheck = None,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        enableSautrCheck = false
      ))
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/details-not-found")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testDetailsNotFoundView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/details-not-found")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fdetails-not-found" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }
  "GET /details-not-found" should {
    "remove all data" when {
      "the user tries again" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          businessVerificationCheck = None,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRemoveAllData(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-sole-trader-business/$testJourneyId/try-again")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureFullNameController.show(testJourneyId).url)
        )
      }
    }
  }
}
