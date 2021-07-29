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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SoleTraderVerificationResult
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.services.AuthenticatorService

import scala.concurrent.Future

trait MockAuthenticatorService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockAuthenticatorService: AuthenticatorService = mock[AuthenticatorService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthenticatorService)
  }

  def mockMatchSoleTraderDetails(individualDetails: IndividualDetails,
                                 journeyConfig: JourneyConfig
                                )(response: Future[SoleTraderVerificationResult]): OngoingStubbing[_] =
    when(mockAuthenticatorService.matchSoleTraderDetails(
      ArgumentMatchers.eq(individualDetails),
      ArgumentMatchers.eq(journeyConfig),
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

}
