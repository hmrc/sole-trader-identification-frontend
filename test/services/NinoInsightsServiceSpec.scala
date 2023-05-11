/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import connectors.mocks.MockInsightsConnector
import helpers.TestConstants.{testInsightsReturnBody, testJourneyId, testNino}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.services.NinoInsightsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NinoInsightsServiceSpec extends AnyWordSpec
  with Matchers
  with MockSoleTraderIdentificationService
  with MockInsightsConnector {

  object TestService extends NinoInsightsService(
    mockNinoInsightsConnector,
    mockSoleTraderIdentificationService
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "ninoInsights" should {
    "retrieve and store insights json" in {
      mockRetrieveNinoInsights(testNino)(Future.successful(testInsightsReturnBody))
      mockStoreInsights(testJourneyId, testInsightsReturnBody)(Future.successful(SuccessfullyStored))

      val result = await(TestService.ninoInsights(testJourneyId, testNino))

      result mustBe testInsightsReturnBody
    }
  }

}