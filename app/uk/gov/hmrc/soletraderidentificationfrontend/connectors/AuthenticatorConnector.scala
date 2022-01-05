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

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderVerificationResultHttpParser.SoleTraderVerificationResultReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.AuthenticatorResponse

import java.time.format.DateTimeFormatter.ofPattern
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatorConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig)(implicit executionContext: ExecutionContext) {

  def matchSoleTraderDetails(authenticatorDetails: IndividualDetails)(implicit hc: HeaderCarrier): Future[AuthenticatorResponse] = {
    val nino = authenticatorDetails.optNino match {
      case Some(nino) => nino.toUpperCase()
      case _ => throw new InternalServerException("Missing required NINO for Authenticator API")
    }

    val jsonBody = Json.obj(
      "firstName" -> authenticatorDetails.firstName.capitalize,
      "lastName" -> authenticatorDetails.lastName.capitalize,
      "dateOfBirth" -> authenticatorDetails.dateOfBirth.format(ofPattern("uuuu-MM-dd")),
      "nino" -> nino
    )

    httpClient.POST(appConfig.matchSoleTraderDetailsUrl, jsonBody)
  }
}
