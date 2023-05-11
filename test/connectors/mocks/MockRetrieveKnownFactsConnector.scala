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
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveKnownFactsConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.{KnownFactsResponse, KnownFactsResponseError}

import scala.concurrent.Future

trait MockRetrieveKnownFactsConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockRetrieveKnownFactsConnector: RetrieveKnownFactsConnector = mock[RetrieveKnownFactsConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRetrieveKnownFactsConnector)
  }

  def mockRetrieveKnownFacts(sautr: String)(response: Future[Either[KnownFactsResponseError, KnownFactsResponse]]): OngoingStubbing[_] = {
    when(mockRetrieveKnownFactsConnector.retrieveKnownFacts(
      ArgumentMatchers.eq(sautr)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
  }

  def verifyRetrieveKnownFacts(sautr: String): Unit = {
    verify(mockRetrieveKnownFactsConnector).retrieveKnownFacts(
      ArgumentMatchers.eq(sautr)
    )(ArgumentMatchers.any[HeaderCarrier])
  }

}
