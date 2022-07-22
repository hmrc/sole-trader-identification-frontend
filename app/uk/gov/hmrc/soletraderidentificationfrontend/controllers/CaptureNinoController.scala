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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{EnableNoNinoJourney, FeatureSwitching}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureNinoForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_nino_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureNinoController @Inject()(mcc: MessagesControllerComponents,
                                      view: capture_nino_page,
                                      soleTraderIdentificationService: SoleTraderIdentificationService,
                                      val authConnector: AuthConnector,
                                      journeyService: JourneyService,
                                      messagesHelper: MessagesHelper
                                     )(implicit val config: AppConfig,
                                       executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          for {
            journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
            firstName <- soleTraderIdentificationService
              .retrieveFullName(journeyId)
              .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
          } yield {
            val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
            implicit val messages: Messages = remoteMessagesApi.preferred(request)
            Ok(view(
              firstName,
              journeyId = journeyId,
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CaptureNinoController.submit(journeyId),
              form = CaptureNinoForm.form,
              noNinoJourneyEnabled = isEnabled(EnableNoNinoJourney)
            ))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureNinoForm.form.bindFromRequest().fold(
            formWithErrors => {
              for {
                journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
                firstName <- soleTraderIdentificationService
                  .retrieveFullName(journeyId)
                  .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
              } yield {
                val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
                implicit val messages: Messages = remoteMessagesApi.preferred(request)
                BadRequest(view(
                  firstName,
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureNinoController.submit(journeyId),
                  form = formWithErrors,
                  noNinoJourneyEnabled = isEnabled(EnableNoNinoJourney)
                ))
              }
            }
            ,
            nino =>
              for {
                _ <- soleTraderIdentificationService.storeNino(journeyId, nino)
                _ <- soleTraderIdentificationService.removeAddress(journeyId)
                _ <- soleTraderIdentificationService.removeOverseasTaxIdentifiers(journeyId)
                journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
              } yield
                if (journeyConfig.pageConfig.enableSautrCheck) {
                  Redirect(routes.CaptureSautrController.show(journeyId))
                } else {
                  Redirect(routes.CheckYourAnswersController.show(journeyId))
                }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }


  def noNino(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>
              soleTraderIdentificationService.removeNino(journeyId).map {
                if (journeyConfig.pageConfig.enableSautrCheck)
                  _ => Redirect(routes.CaptureAddressController.show(journeyId))
                else
                  _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}
