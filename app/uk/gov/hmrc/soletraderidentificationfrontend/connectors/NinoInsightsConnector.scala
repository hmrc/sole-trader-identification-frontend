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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.NinoInsightsHttpParser.NinoInsightsHttpReads
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoInsightsConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def retrieveNinoInsights(nino: String)(implicit hc: HeaderCarrier): Future[JsObject] = {
    val jsonBody = Json.obj(
      "nino" -> nino
    )

    httpClient
      .post(url = url"${appConfig.insightsUrl}")(hc)
      .withBody(jsonBody)
      .execute[JsObject](NinoInsightsHttpReads, ec)

  }

}

object NinoInsightsHttpParser {

  implicit object NinoInsightsHttpReads extends HttpReads[JsObject] {
    override def read(method: String, url: String, response: HttpResponse): JsObject = {
      response.status match {
        case OK =>
          response.json.validate[JsObject] match {
            case JsSuccess(value, _) => value
            case JsError(_) =>
              throw new InternalServerException("Unexpected error reading response from Insights API")
          }
        case _ =>
          throw new InternalServerException(s"Unexpected response from Insights API - status = ${response.status}, body = ${response.body}")
      }
    }
  }
}
