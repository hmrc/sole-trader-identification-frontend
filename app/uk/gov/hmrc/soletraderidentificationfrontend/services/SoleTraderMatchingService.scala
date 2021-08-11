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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.AuthenticatorConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{AuthenticatorResponse, DetailsMismatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderMatchingService @Inject()(authenticatorConnector: AuthenticatorConnector,
                                          soleTraderIdentificationService: SoleTraderIdentificationService) {

  def matchSoleTraderDetails(journeyId: String,
                             individualDetails: IndividualDetails,
                             journeyConfig: JourneyConfig
                            )(implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuthenticatorResponse] =
    for {
      authenticatorResponse <- authenticatorConnector.matchSoleTraderDetails(individualDetails).map {
        case Right(authenticatorDetails) if journeyConfig.pageConfig.enableSautrCheck =>
          if (authenticatorDetails.optSautr == individualDetails.optSautr)
            Right(authenticatorDetails)
          else {
            Left(DetailsMismatch)
          }
        case authenticatorResponse =>
          authenticatorResponse
      }
      _ <- authenticatorResponse match {
        case Right(details) =>
          soleTraderIdentificationService.storeAuthenticatorDetails(journeyId, details)
        case Left(failureResponse) =>
          soleTraderIdentificationService.storeAuthenticatorFailureResponse(journeyId, failureResponse)
      }
    } yield
      authenticatorResponse

}
