/*
 * Copyright 2024 HM Revenue & Customs
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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SoleTraderDetailsMatchResult
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderMatchingService

import scala.concurrent.{ExecutionContext, Future}

trait MockSoleTraderMatchingService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockSoleTraderMatchingService: SoleTraderMatchingService = mock[SoleTraderMatchingService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSoleTraderMatchingService)
  }

  def mockMatchSoleTraderDetails(journeyId: String, individualDetails: IndividualDetails, journeyConfig: JourneyConfig)(
    response: Future[SoleTraderDetailsMatchResult]
  ): OngoingStubbing[_] =
    when(
      mockSoleTraderMatchingService.matchSoleTraderDetails(
        ArgumentMatchers.eq(journeyId),
        ArgumentMatchers.eq(individualDetails),
        ArgumentMatchers.eq(journeyConfig)
      )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(response)

  def verifyMatchSoleTraderDetails(journeyId: String, individualDetails: IndividualDetails, journeyConfig: JourneyConfig): Unit =
    verify(mockSoleTraderMatchingService).matchSoleTraderDetails(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(individualDetails),
      ArgumentMatchers.eq(journeyConfig)
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])

  def mockMatchSoleTraderDetailsNoNino(journeyId: String, individualDetails: IndividualDetails)(
    response: Future[SoleTraderDetailsMatchResult]
  ): OngoingStubbing[_] =
    when(
      mockSoleTraderMatchingService.matchSoleTraderDetailsNoNino(
        ArgumentMatchers.eq(journeyId),
        ArgumentMatchers.eq(individualDetails)
      )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(response)

}
