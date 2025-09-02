/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, RegistrationStatus}
import uk.gov.hmrc.soletraderidentificationfrontend.services.RegistrationOrchestrationService

import scala.concurrent.Future

trait MockRegistrationOrchestrationService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockRegistrationOrchestrationService: RegistrationOrchestrationService = mock[RegistrationOrchestrationService]

  def mockRegisterWithoutBusinessVerification(journeyId: String, optNino: Option[String], optSautr: Option[String], journeyConfig: JourneyConfig)(
    response: Future[RegistrationStatus]
  ): OngoingStubbing[_] =
    when(
      mockRegistrationOrchestrationService.registerWithoutBusinessVerification(
        ArgumentMatchers.eq(journeyId),
        ArgumentMatchers.eq(optNino),
        ArgumentMatchers.eq(optSautr),
        ArgumentMatchers.eq(journeyConfig)
      )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def verifyRegisterWithoutBusinessVerification(journeyId: String,
                                                optNino: Option[String],
                                                optSautr: Option[String],
                                                journeyConfig: JourneyConfig
                                               ): Unit =
    verify(mockRegistrationOrchestrationService).registerWithoutBusinessVerification(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(optNino),
      ArgumentMatchers.eq(optSautr),
      ArgumentMatchers.eq(journeyConfig)
    )(ArgumentMatchers.any[HeaderCarrier])

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRegistrationOrchestrationService)
  }

}
