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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RegistrationHttpParser.RegistrationHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.RegistrationStatus
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def registerWithNino(nino: String, optSautr: Option[String], regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {

    val detailsJson = Json.obj(
      "nino"   -> nino.toUpperCase,
      "regime" -> regime
    ) ++ {
      optSautr match {
        case Some(sautr) => Json.obj("sautr" -> sautr)
        case _           => Json.obj()
      }
    }

    val jsonBody = Json.obj(
      "soleTrader" -> detailsJson
    )

    httpClient
      .post(url = url"${appConfig.registerUrl}")(hc)
      .withBody(jsonBody)
      .execute[RegistrationStatus](RegistrationHttpReads, ec)

  }

  def registerWithTrn(temporaryReferenceNumber: String, sautr: String, regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {

    val jsonBody = Json.obj(
      "trn"    -> temporaryReferenceNumber,
      "sautr"  -> sautr,
      "regime" -> regime
    )

    httpClient
      .post(url = url"${appConfig.registerWithTrnUrl}")(hc)
      .withBody(jsonBody)
      .execute[RegistrationStatus](RegistrationHttpReads, ec)

  }

}

object RegistrationHttpParser {
  val registrationKey = "registration"

  implicit object RegistrationHttpReads extends HttpReads[RegistrationStatus] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationStatus = {
      response.status match {
        case OK =>
          (response.json \ registrationKey).as[RegistrationStatus]
        case _ =>
          throw new InternalServerException(s"Unexpected response from Register API - status = ${response.status}, body = ${response.body}")
      }
    }
  }

}
