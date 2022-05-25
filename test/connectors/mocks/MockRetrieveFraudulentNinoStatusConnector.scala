/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveFraudulentNinoStatusConnector

import scala.concurrent.Future

trait MockRetrieveFraudulentNinoStatusConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockRetrieveFraudulentNinoStatusConnector: RetrieveFraudulentNinoStatusConnector = mock[RetrieveFraudulentNinoStatusConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRetrieveFraudulentNinoStatusConnector)
  }

  def mockIsFraudulentNino(ninoToBeChecked: String)(response: Future[Boolean]): OngoingStubbing[_] = {
    when(mockRetrieveFraudulentNinoStatusConnector.isFraudulentNino(
      ArgumentMatchers.eq(ninoToBeChecked)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

}
