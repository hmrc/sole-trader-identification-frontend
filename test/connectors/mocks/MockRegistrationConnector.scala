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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.RegistrationStatus

import scala.concurrent.Future

trait MockRegistrationConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRegistrationConnector)
  }

  def mockRegister(nino: String, sautr: Option[String], regime: String)(response: Future[RegistrationStatus]): OngoingStubbing[_] = {
    when(
      mockRegistrationConnector.registerWithNino(
        ArgumentMatchers.eq(nino),
        ArgumentMatchers.eq(sautr),
        ArgumentMatchers.eq(regime)
      )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyRegistration(nino: String, sautr: Option[String], regime: String): Unit = {
    verify(mockRegistrationConnector).registerWithNino(
      ArgumentMatchers.eq(nino),
      ArgumentMatchers.eq(sautr),
      ArgumentMatchers.eq(regime)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

  def mockRegisterWithTrn(trn: String, sautr: String, regime: String)(response: Future[RegistrationStatus]): OngoingStubbing[_] = {
    when(
      mockRegistrationConnector.registerWithTrn(
        ArgumentMatchers.eq(trn),
        ArgumentMatchers.eq(sautr),
        ArgumentMatchers.eq(regime)
      )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyRegistrationWithTrn(trn: String, sautr: String, regime: String): Unit = {
    verify(mockRegistrationConnector).registerWithTrn(
      ArgumentMatchers.eq(trn),
      ArgumentMatchers.eq(sautr),
      ArgumentMatchers.eq(regime)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
