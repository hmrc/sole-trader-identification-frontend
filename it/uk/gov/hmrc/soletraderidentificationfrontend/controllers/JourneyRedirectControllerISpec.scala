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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testContinueUrl, testDeskProServiceId, testJourneyId, testSignOutUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.SoleTrader
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class JourneyRedirectControllerISpec extends ComponentSpecHelper with AuthStub {

  "GET /journey/redirect/:journeyId" should {
    "redirect to the journey config continue url" in {
      stubAuth(OK, successfulAuthResponse())
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl), SoleTrader)))

      lazy val result = get(s"$baseUrl/journey/redirect/$testJourneyId")

      result.status mustBe SEE_OTHER
      result.header(LOCATION) mustBe Some(testContinueUrl + s"?journeyId=$testJourneyId")
    }
  }

}
