/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureNinoViewTests
import scala.concurrent.ExecutionContext.Implicits.global

class CaptureNinoControllerISpec extends ComponentSpecHelper with CaptureNinoViewTests {
  val testJourneyId = "testJourneyId"

  "GET /national-insurance-number" should {
    lazy val result = get(s"/national-insurance-number/$testJourneyId")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureNinoView(result)
    }
  }

  "POST /national-insurance-number" should {
    val testNino = "AA111111A"

    lazy val result = post(s"/national-insurance-number/$testJourneyId")("nino" -> testNino)

    "store the NINO in the database" in {
      val optNino = await(app.injector.instanceOf[SoleTraderDetailsRepository].retrieveNino(testJourneyId))
      optNino mustBe Some(testNino)
    }

    "redirect to the capture sautr page" in {
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureSautrController.show().url)
      )
    }
  }

  "no nino is submitted" should {
    lazy val result = post(s"/national-insurance-number/$testJourneyId")("nino" -> "")

    "return a bad request" in {
      result.status mustBe BAD_REQUEST
    }

    testCaptureNinoErrorMessages(result)
  }

  "an invalid nino is submitted" should {
    lazy val result = post(s"/national-insurance-number/$testJourneyId")("nino" -> "AAAAAAAAAA")

    "return a bad request" in {
      result.status mustBe BAD_REQUEST
    }

    testCaptureNinoErrorMessages(result)
  }

}
