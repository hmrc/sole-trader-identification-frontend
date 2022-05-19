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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResult, JsSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveFraudulentNinoStatusStatusParser.RetrieveFraudulentNinoStatusHttpReads

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveFraudulentNinoStatusConnector @Inject()(http: HttpClient,
                                                      appConfig: AppConfig
                                                     )(implicit ec: ExecutionContext) {

  def isFraudulentNino(ninoToBeChecked: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http.GET[Boolean](appConfig.fraudulentNinoUrl(ninoToBeChecked))(
      RetrieveFraudulentNinoStatusHttpReads,
      hc,
      ec
    )

}

object RetrieveFraudulentNinoStatusStatusParser {

  implicit object RetrieveFraudulentNinoStatusHttpReads extends HttpReads[Boolean] {
    override def read(method: String, url: String, response: HttpResponse): Boolean = {
      response.status match {
        case OK =>
          (response.json \ "isAFraudulentNino").validate[Boolean] match {
            case JsSuccess(trueOrFalse, _) => trueOrFalse
            case JsError(_) => throw new InternalServerException("Expecting json { isAFraudulentNino : true/false }")
          }
        case status =>
          throw new InternalServerException(s"Invalid response status returned from backend. Expecting $OK got $status")
      }
    }
  }

}