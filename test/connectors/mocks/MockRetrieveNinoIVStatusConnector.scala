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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveNinoIVStatusConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.BusinessVerificationStatus

import scala.concurrent.Future

trait MockRetrieveNinoIVStatusConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockRetrieveNinoIVStatusConnector: RetrieveNinoIVStatusConnector = mock[RetrieveNinoIVStatusConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRetrieveNinoIVStatusConnector)
  }

  def mockRetrieveNinoIVStatus(journeyId: String)(response: Future[BusinessVerificationStatus]): OngoingStubbing[_] = {
    when(
      mockRetrieveNinoIVStatusConnector.retrieveNinoIVStatus(
        ArgumentMatchers.eq(journeyId)
      )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyRetrieveNinoIVStatus(journeyId: String): Unit = {
    verify(mockRetrieveNinoIVStatusConnector).retrieveNinoIVStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
