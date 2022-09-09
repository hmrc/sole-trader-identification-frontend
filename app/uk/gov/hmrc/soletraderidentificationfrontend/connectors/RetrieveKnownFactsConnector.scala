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

import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.KnownFactsHttpParser.KnownFactsHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.{KnownFactsResponse, KnownFactsResponseError, KnownFactsNoContentError}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveKnownFactsConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def retrieveKnownFacts(sautr: String)(implicit hc: HeaderCarrier): Future[Either[KnownFactsResponseError, KnownFactsResponse]] = {

    val jsonBody = Json.obj(
      "service" -> "IR-SA",
      "knownFacts" -> Json.arr(
        Json.obj(
          "key" -> "UTR",
          "value" -> sautr
        )
      )
    )

    httpClient.POST(appConfig.knownFactsUrl, jsonBody)(implicitly[Writes[JsObject]],
      KnownFactsHttpReads,
      hc,
      ec)

  }
}

object KnownFactsHttpParser {

  case class Verifier(key: String, value: String)

  implicit val VerifiersFormat: OFormat[Verifier] = Json.format[Verifier]

  implicit object KnownFactsHttpReads extends HttpReads[Either[KnownFactsResponseError, KnownFactsResponse]] {
    override def read(method: String, url: String, response: HttpResponse): Either[KnownFactsResponseError, KnownFactsResponse] = {
      response.status match {
        case OK =>
          val knownFacts = for {
            verifiers <- (response.json \\ "verifiers").head.validate[List[Verifier]]
          } yield verifiers
          knownFacts match {
            case JsSuccess(verifiersList, _) =>
              val optPostcode: Option[String] = verifiersList.find(verifier => verifier.key == "Postcode").map(verifier => verifier.value)
              val optIsAbroadFlag: Option[Boolean] = verifiersList.find(verifier => verifier.key == "IsAbroad").map(verifier => if (verifier.value == "Y") true else false)
              val optNino: Option[String] = verifiersList.find(verifier => verifier.key == "NINO").map(verifier => verifier.value)
              Right(KnownFactsResponse(optPostcode, optIsAbroadFlag, optNino))
            case JsError(errors) => throw new InternalServerException(s"`Failed to read Known Facts API response with the following error/s: $errors")
          }
        case NO_CONTENT => Left(KnownFactsNoContentError)
        case status =>
          throw new InternalServerException(s"Unexpected status from known facts call. Status returned - $status")
      }
    }
  }


}
