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

import play.api.http.Status.{CREATED, FORBIDDEN, NOT_FOUND}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector._
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateBusinessVerificationJourneyConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def createBusinessVerificationJourney(journeyId: String, sautr: String, journeyConfig: JourneyConfig)(implicit
    hc: HeaderCarrier
  ): Future[BusinessVerificationJourneyCreationResponse] = {

    val pageTitle: String = journeyConfig.pageConfig.labels
      .flatMap(_.optEnglishServiceName)
      .getOrElse(
        journeyConfig.pageConfig.optServiceName
          .getOrElse(appConfig.defaultServiceName)
      )

    val jsonBody: JsObject =
      Json.obj(
        "journeyType" -> "BUSINESS_VERIFICATION",
        "origin"      -> journeyConfig.regime.toLowerCase,
        "identifiers" -> Json.arr(
          Json.obj(
            "saUtr" -> sautr
          )
        ),
        "continueUrl"               -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
        "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
        "pageTitle"                 -> pageTitle,
        "deskproServiceName"        -> journeyConfig.pageConfig.deskProServiceId
      )

    httpClient
      .post(url = url"${appConfig.createBusinessVerificationJourneyUrl}")(hc)
      .withBody(jsonBody)
      .execute[BusinessVerificationJourneyCreationResponse](BusinessVerificationHttpReads, ec)

  }

}

object CreateBusinessVerificationJourneyConnector {

  type BusinessVerificationJourneyCreationResponse = Either[BusinessVerificationJourneyCreationFailure, BusinessVerificationJourneyCreated]

  case class BusinessVerificationJourneyCreated(redirectUri: String)

  sealed trait BusinessVerificationJourneyCreationFailure

  case object NotEnoughEvidence extends BusinessVerificationJourneyCreationFailure

  case object UserLockedOut extends BusinessVerificationJourneyCreationFailure

  implicit object BusinessVerificationHttpReads extends HttpReads[BusinessVerificationJourneyCreationResponse] {
    override def read(method: String, url: String, response: HttpResponse): BusinessVerificationJourneyCreationResponse = {
      response.status match {
        case CREATED =>
          (response.json \ "redirectUri").asOpt[String] match {
            case Some(redirectUri) =>
              Right(BusinessVerificationJourneyCreated(redirectUri))
            case _ =>
              throw new InternalServerException(s"Business Verification API returned malformed JSON")
          }
        case NOT_FOUND =>
          Left(NotEnoughEvidence)
        case FORBIDDEN =>
          Left(UserLockedOut)
        case status =>
          throw new InternalServerException(s"Business Verification API failed with status: $status")
      }
    }
  }

}
