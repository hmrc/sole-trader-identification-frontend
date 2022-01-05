/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.soletraderidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyController @Inject()(controllerComponents: ControllerComponents,
                                  journeyService: JourneyService,
                                  val authConnector: AuthConnector,
                                  soleTraderIdentificationService: SoleTraderIdentificationService
                                 )(implicit ec: ExecutionContext,
                                   appConfig: AppConfig) extends BackendController(controllerComponents) with AuthorisedFunctions {

  def createSoleTraderJourney: Action[JourneyConfig] = createJourney(enableSautrCheck = true)

  def createIndividualJourney: Action[JourneyConfig] = createJourney(enableSautrCheck = false)

  def createJourney(enableSautrCheck: Boolean): Action[JourneyConfig] = Action.async(parse.json[JourneyConfig] {
    json =>
      for {
        continueUrl <- (json \ continueUrlKey).validate[String]
        businessVerificationCheck <- (json \ businessVerificationCheckKey).validateOpt[Boolean]
        optServiceName <- (json \ optServiceNameKey).validateOpt[String]
        deskProServiceId <- (json \ deskProServiceIdKey).validate[String]
        signOutUrl <- (json \ signOutUrlKey).validate[String]
        accessibilityUrl <- (json \ accessibilityUrlKey).validate[String]
      } yield JourneyConfig(
        continueUrl,
        businessVerificationCheck.getOrElse(true),
        PageConfig(
          optServiceName,
          deskProServiceId,
          signOutUrl,
          enableSautrCheck,
          accessibilityUrl
        )
      )
  }) {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.createJourney(req.body, authInternalId).map(
            journeyId =>
              Created(Json.obj(
                "journeyStartUrl" -> s"${appConfig.selfUrl}${controllerRoutes.CaptureFullNameController.show(journeyId).url}"
              ))
          )
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def createJourney(): Action[JourneyConfig] = Action.async(parse.json[JourneyConfig] {
    json =>
      for {
        continueUrl <- (json \ continueUrlKey).validate[String]
        businessVerificationCheck <- (json \ businessVerificationCheckKey).validateOpt[Boolean]
        optServiceName <- (json \ optServiceNameKey).validateOpt[String]
        deskProServiceId <- (json \ deskProServiceIdKey).validate[String]
        signOutUrl <- (json \ signOutUrlKey).validate[String]
        enableSautrCheck <- (json \ enableSautrCheckKey).validateOpt[Boolean]
        accessibilityUrl <- (json \ accessibilityUrlKey).validate[String]
      } yield JourneyConfig(
        continueUrl,
        businessVerificationCheck.getOrElse(true),
        PageConfig(
          optServiceName,
          deskProServiceId,
          signOutUrl,
          enableSautrCheck.getOrElse(false),
          accessibilityUrl
        )
      )
  }) {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.createJourney(req.body, authInternalId).map(
            journeyId =>
              Created(Json.obj(
                "journeyStartUrl" -> s"${appConfig.selfUrl}${controllerRoutes.CaptureFullNameController.show(journeyId).url}"
              ))
          )
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def retrieveJourneyData(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId).map {
          case Some(journeyData) => Ok(Json.toJson(journeyData.copy(optNino = journeyData.optNino.map(_.toUpperCase))))
          case None => NotFound
        }
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
}
