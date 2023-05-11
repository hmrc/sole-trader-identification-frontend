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
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.SoleTraderIdentificationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored

import scala.concurrent.Future

trait MockSoleTraderIdentificationConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockSoleTraderIdentificationConnector: SoleTraderIdentificationConnector = mock[SoleTraderIdentificationConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSoleTraderIdentificationConnector)
  }

  def mockRetrieveSoleTraderInformation[T](journeyId: String,
                                           dataKey: String
                                          )(response: Future[Option[T]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationConnector.retrieveSoleTraderDetails[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[Reads[T]],
      ArgumentMatchers.any[Manifest[T]],
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyRetrieveSoleTraderInformation[T](journeyId: String, dataKey: String): Unit =
    verify(mockSoleTraderIdentificationConnector).retrieveSoleTraderDetails[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[Reads[T]],
      ArgumentMatchers.any[Manifest[T]],
      ArgumentMatchers.any[HeaderCarrier])

  def mockStoreData[T](journeyId: String,
                       dataKey: String,
                       data: T
                      )(response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationConnector.storeData[T](
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data)
    )(ArgumentMatchers.any[Writes[T]],
      ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyStoreData[T](journeyId: String,
                         dataKey: String,
                         data: T): Unit =
    verify(mockSoleTraderIdentificationConnector).storeData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data)
    )(ArgumentMatchers.any[Writes[T]],
      ArgumentMatchers.any[HeaderCarrier])

  def mockRemoveSoleTraderDetails[T](journeyId: String,
                                     dataKey: String
                                    )(response: Future[SuccessfullyRemoved.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationConnector.removeSoleTraderDetails(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[HeaderCarrier]
    )).thenReturn(response)

  def verifyRemoveSoleTraderDetails[T](journeyId: String, dataKey: String): Unit =
    verify(mockSoleTraderIdentificationConnector).removeSoleTraderDetails(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey)
    )(ArgumentMatchers.any[HeaderCarrier])

}
