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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateTrnConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Address, FullName}

import java.time.LocalDate
import scala.concurrent.Future

trait MockCreateTrnConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockCreateTrnConnector: CreateTrnConnector = mock[CreateTrnConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCreateTrnConnector)
  }

  def mockCreateTrn(dateOfBirth: LocalDate, fullName: FullName, address: Address)(response: Future[String]): OngoingStubbing[_] = {
    when(
      mockCreateTrnConnector.createTrn(
        ArgumentMatchers.eq(dateOfBirth),
        ArgumentMatchers.eq(fullName),
        ArgumentMatchers.eq(address)
      )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyCreateTrn(dateOfBirth: LocalDate, fullName: FullName, address: Address): Unit = {
    verify(mockCreateTrnConnector).createTrn(
      ArgumentMatchers.eq(dateOfBirth),
      ArgumentMatchers.eq(fullName),
      ArgumentMatchers.eq(address)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
