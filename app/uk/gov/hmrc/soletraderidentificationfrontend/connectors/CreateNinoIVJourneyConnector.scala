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

import play.api.http.Status.{CREATED, FORBIDDEN, NOT_FOUND}
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateNinoIVJourneyConnector.{NinoIVHttpReads, NinoIVJourneyCreationResponse}
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateNinoIVJourneyConnector @Inject() (http: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def createNinoIdentityVerificationJourney(journeyId: String, nino: String, journeyConfig: JourneyConfig)(implicit
    hc: HeaderCarrier
  ): Future[NinoIVJourneyCreationResponse] = {

    val pageTitle: String = journeyConfig.pageConfig.labels
      .flatMap(_.optEnglishServiceName)
      .getOrElse(
        journeyConfig.pageConfig.optServiceName
          .getOrElse(appConfig.defaultServiceName)
      )

    val welshPageTitle: String = journeyConfig.pageConfig.labels.flatMap(_.optWelshServiceName).getOrElse(appConfig.defaultWelshServiceName)

    val jsonBody: JsObject =
      Json.obj(
        "origin" -> journeyConfig.regime.toLowerCase,
        "identifiers" -> Json.arr(
          Json.obj(
            "nino" -> nino
          )
        ),
        "continueUrl"               -> routes.NinoIVController.retrieveNinoIVResult(journeyId).url,
        "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
        "deskproServiceName"        -> journeyConfig.pageConfig.deskProServiceId,
        "labels" -> Json.obj(
          "en" -> Json.obj(
            "pageTitle" -> pageTitle
          ),
          "cy" -> Json.obj(
            "pageTitle" -> welshPageTitle
          )
        )
      )

    http.POST[JsObject, NinoIVJourneyCreationResponse](appConfig.createNinoIVJourneyUrl, jsonBody)(
      implicitly[Writes[JsObject]],
      NinoIVHttpReads,
      hc,
      ec
    )
  }

}

object CreateNinoIVJourneyConnector {

  type NinoIVJourneyCreationResponse = Either[JourneyCreationFailure, JourneyCreated]

  case class JourneyCreated(redirectUri: String)

  sealed trait JourneyCreationFailure

  case object NotEnoughEvidence extends JourneyCreationFailure

  case object UserLockedOut extends JourneyCreationFailure

  implicit object NinoIVHttpReads extends HttpReads[NinoIVJourneyCreationResponse] {
    override def read(method: String, url: String, response: HttpResponse): NinoIVJourneyCreationResponse = {
      response.status match {
        case CREATED =>
          (response.json \ "redirectUri").asOpt[String] match {
            case Some(redirectUri) =>
              Right(JourneyCreated(redirectUri))
            case _ =>
              throw new InternalServerException(s"Nino Identity Verification API returned malformed JSON")
          }
        case NOT_FOUND =>
          Left(NotEnoughEvidence)
        case FORBIDDEN =>
          Left(UserLockedOut)
        case status =>
          throw new InternalServerException(s"Nino Identity Verification API failed with status: $status")
      }
    }
  }

}
