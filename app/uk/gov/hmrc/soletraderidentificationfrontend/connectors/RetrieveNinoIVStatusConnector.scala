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

import play.api.http.Status.OK
import play.api.libs.json.JsonValidationError
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveNinoIVStatusConnector.RetrieveIVStatusHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass, BusinessVerificationStatus}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveNinoIVStatusConnector @Inject()(http: HttpClient,
                                              appConfig: AppConfig
                                             )(implicit ec: ExecutionContext) {

  def retrieveNinoIVStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    http.GET[BusinessVerificationStatus](appConfig.getNinoIVResultUrl(journeyId))(
      RetrieveIVStatusHttpReads,
      hc,
      ec
    )

}

object RetrieveNinoIVStatusConnector {
  val PassKey = "PASS"
  val FailKey = "FAIL"

  implicit object RetrieveIVStatusHttpReads extends HttpReads[BusinessVerificationStatus] {
    override def read(method: String, url: String, response: HttpResponse): BusinessVerificationStatus = {
      response.status match {
        case OK =>
          (response.json \ "verificationStatus")
            .validate[String]
            .collect(JsonValidationError("Invalid verification status returned from identity verification")) {
              case PassKey => BusinessVerificationPass
              case FailKey => BusinessVerificationFail
            }.getOrElse(throw new InternalServerException("Invalid response returned from retrieve Nino Identity Verification result"))
        case _ =>
          throw new InternalServerException("Invalid response returned from retrieve Nino Identity Verification result")
      }
    }
  }

}

