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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.connectors

import play.api.http.Status._
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.soletraderidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.soletraderidentificationfrontend.api.controllers.{routes => apiRoutes}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, JourneyLabels}
import uk.gov.hmrc.soletraderidentificationfrontend.testonly.connectors.TestCreateJourneyConnector.journeyConfigWriter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateJourneyConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def createSoleTraderJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = apiRoutes.JourneyController.createSoleTraderJourney(), journeyConfig = journeyConfig)

  def createIndividualJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = apiRoutes.JourneyController.createIndividualJourney(), journeyConfig = journeyConfig)

  private def postTo(destination: Call, journeyConfig: JourneyConfig)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String] = {
    val destinationUrl = appConfig.selfBaseUrl + destination.url

    httpClient.post(url"$destinationUrl").withBody(Json.toJson(journeyConfig)).execute[HttpResponse].map {
      case response @ HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(
          s"Invalid response from Destination : ${destination.url}  Status : ${response.status} Body : ${response.body}"
        )
    }

  }

}

object TestCreateJourneyConnector {
  implicit val journeyConfigWriter: Writes[JourneyConfig] = (journeyConfig: JourneyConfig) =>
    Json.obj(
      continueUrlKey               -> journeyConfig.continueUrl,
      businessVerificationCheckKey -> journeyConfig.businessVerificationCheck,
      optServiceNameKey            -> journeyConfig.pageConfig.optServiceName,
      deskProServiceIdKey          -> journeyConfig.pageConfig.deskProServiceId,
      signOutUrlKey                -> journeyConfig.pageConfig.signOutUrl,
      enableSautrCheckKey          -> journeyConfig.pageConfig.enableSautrCheck,
      accessibilityUrlKey          -> journeyConfig.pageConfig.accessibilityUrl,
      optFullNamePageLabelKey      -> journeyConfig.pageConfig.optFullNamePageLabel,
      regimeKey                    -> journeyConfig.regime
    ) ++ labelsAsOptJsObject(journeyConfig.pageConfig.labels)

  private def labelsAsOptJsObject(optJourneyLabels: Option[JourneyLabels]): JsObject =
    optJourneyLabels match {
      case Some(journeyLabels) => Json.obj(labelsKey -> Json.toJsObject(journeyLabels))
      case _                   => Json.obj()
    }
}
