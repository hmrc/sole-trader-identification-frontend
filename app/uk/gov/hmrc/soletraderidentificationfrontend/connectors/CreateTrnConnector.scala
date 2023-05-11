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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateTrnHttpParser.CreateTrnHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Address, FullName}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateTrnConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def createTrn(dateOfBirth: LocalDate, fullName: FullName, address: Address)(implicit hc: HeaderCarrier): Future[String] = {

    val jsonBody = Json.obj(
      "dateOfBirth" -> dateOfBirth,
      "fullName"    -> fullName,
      "address"     -> address
    )

    httpClient.POST[JsObject, String](appConfig.createTrnUrl, jsonBody)(
      implicitly[Writes[JsObject]],
      CreateTrnHttpReads,
      hc,
      ec
    )
  }
}

object CreateTrnHttpParser {

  implicit object CreateTrnHttpReads extends HttpReads[String] {
    override def read(method: String, url: String, response: HttpResponse): String = {
      response.status match {
        case CREATED =>
          (response.json \ "temporaryReferenceNumber").as[String]
        case status =>
          throw new InternalServerException(s"Creation of TRN failed with status: $status")
      }
    }
  }
}
