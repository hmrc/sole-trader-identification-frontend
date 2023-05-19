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

package uk.gov.hmrc.soletraderidentificationfrontend.api.controllers

import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.soletraderidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, JourneyLabels, PageConfig, SoleTraderDetails}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfigUrlAllowed, JourneyConfigUrlInvalid, JourneyConfigUrlNotAllowed}
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.UrlHelper

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyController @Inject() (controllerComponents: ControllerComponents,
                                   journeyService: JourneyService,
                                   val authConnector: AuthConnector,
                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                   urlHelper: UrlHelper
                                  )(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends BackendController(controllerComponents)
    with AuthorisedFunctions {

  val relativeUrlReads: String => Reads[String] = relativeUrlReadsHelper(urlHelper)
  val continueUrlReads: Reads[String] = relativeUrlReads(continueUrlKey)
  val signOutUrlReads: Reads[String] = relativeUrlReads(signOutUrlKey)
  val accessibilityUrlReads: Reads[String] = relativeUrlReads(accessibilityUrlKey)

  def createSoleTraderJourney: Action[JourneyConfig] = createJourney(sautrCheckPolicy = SautrCheckEnabled)

  def createIndividualJourney: Action[JourneyConfig] = createJourney(sautrCheckPolicy = SautrCheckDisabled)

  def createJourney(): Action[JourneyConfig] = createJourney(sautrCheckPolicy = SautrCheckReadFromIncomingJson)

  def retrieveJourneyData(journeyId: String): Action[AnyContent] = Action.async { implicit req =>
    authorised() {
      soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId).map {
        case Some(journeyData) =>
          Ok(SoleTraderDetails.jsonWriterForCallingServices(journeyData.copy(optNino = journeyData.optNino.map(_.toUpperCase))))
        case None => NotFound
      }
    }
  }

  private def createJourney(sautrCheckPolicy: SautrCheckPolicy): Action[JourneyConfig] = Action.async(parse.json[JourneyConfig] { json =>
    for {
      continueUrl                <- relativeUrlReads(continueUrlKey).reads(json)
      businessVerificationCheck  <- (json \ businessVerificationCheckKey).validateOpt[Boolean]
      optServiceName             <- (json \ optServiceNameKey).validateOpt[String]
      deskProServiceId           <- (json \ deskProServiceIdKey).validate[String]
      signOutUrl                 <- signOutUrlReads.reads(json)
      sautrCheckFromIncomingJson <- (json \ enableSautrCheckKey).validateOpt[Boolean]
      accessibilityUrl           <- accessibilityUrlReads.reads(json)
      optFullNamePageLabel       <- (json \ optFullNamePageLabelKey).validateOpt[String]
      regime                     <- (json \ regimeKey).validate[String]
      labels                     <- (json \ labelsKey).validateOpt[JourneyLabels]
    } yield JourneyConfig(
      continueUrl,
      businessVerificationCheck.getOrElse(true),
      PageConfig(
        optServiceName,
        deskProServiceId,
        signOutUrl,
        enableSautrCheck(sautrCheckPolicy, sautrCheckFromIncomingJson),
        accessibilityUrl,
        optFullNamePageLabel,
        labels
      ),
      regime
    )
  }) { implicit req =>
    authorised().retrieve(internalId) {
      case Some(authInternalId) =>
        journeyService
          .createJourney(req.body, authInternalId)
          .map(journeyId =>
            Created(
              Json.obj(
                "journeyStartUrl" -> s"${appConfig.selfUrl}${controllerRoutes.CaptureFullNameController.show(journeyId).url}"
              )
            )
          )
      case _ =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

}

object JourneyController {
  val continueUrlKey = "continueUrl"
  val businessVerificationCheckKey = "businessVerificationCheck"
  val optServiceNameKey = "optServiceName"
  val deskProServiceIdKey = "deskProServiceId"
  val signOutUrlKey = "signOutUrl"
  val enableSautrCheckKey = "enableSautrCheck"
  val accessibilityUrlKey = "accessibilityUrl"
  val optFullNamePageLabelKey = "optFullNamePageLabel"
  val regimeKey = "regime"
  val labelsKey = "labels"

  private def relativeUrlReadsHelper(urlHelper: UrlHelper)(jsPathKeyToBeRead: String): Reads[String] = (JsPath \ jsPathKeyToBeRead)
    .read[String]
    .flatMap { someIncomingUrlToBeValidated => (_: JsValue) =>
      urlHelper.isAValidUrl(urlToBeValidated = someIncomingUrlToBeValidated) match {
        case JourneyConfigUrlAllowed => JsSuccess(someIncomingUrlToBeValidated)
        case JourneyConfigUrlNotAllowed =>
          JsError(s"$someIncomingUrlToBeValidated value for $jsPathKeyToBeRead json key is not relative or accepted urls")
        case JourneyConfigUrlInvalid =>
          JsError(s"An unexpected error occurred validating $someIncomingUrlToBeValidated for $jsPathKeyToBeRead json key")
      }
    }

  private def enableSautrCheck(sautrCheckPolicy: SautrCheckPolicy, sautrCheckFromIncomingJson: Option[Boolean]): Boolean = sautrCheckPolicy match {
    case SautrCheckDisabled             => false
    case SautrCheckEnabled              => true
    case SautrCheckReadFromIncomingJson => sautrCheckFromIncomingJson.getOrElse(false)
  }

  sealed trait SautrCheckPolicy

  case object SautrCheckEnabled extends SautrCheckPolicy

  case object SautrCheckDisabled extends SautrCheckPolicy

  case object SautrCheckReadFromIncomingJson extends SautrCheckPolicy
}
