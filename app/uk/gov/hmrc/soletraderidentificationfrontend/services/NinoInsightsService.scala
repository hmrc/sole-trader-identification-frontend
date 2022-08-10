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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.NinoInsightsConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoInsightsService @Inject()(ninoInsightsConnector: NinoInsightsConnector,
                                    soleTraderIdentificationService: SoleTraderIdentificationService
                                   )(implicit ec: ExecutionContext) {

  def ninoInsights(journeyId: String, nino: String)(implicit hc: HeaderCarrier): Future[JsObject] = {
    for {
      insights <- ninoInsightsConnector.retrieveNinoInsights(nino)
      _ <- soleTraderIdentificationService.storeInsights(journeyId, insights)
    } yield insights
  }

}