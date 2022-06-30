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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureDateOfBirthForm.captureDateOfBirthForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_date_of_birth_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureDateOfBirthController @Inject()(mcc: MessagesControllerComponents,
                                             view: capture_date_of_birth_page,
                                             soleTraderIdentificationService: SoleTraderIdentificationService,
                                             val authConnector: AuthConnector,
                                             journeyService: JourneyService,
                                             messagesHelper: MessagesHelper
                                            )(implicit val config: AppConfig,
                                              ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        for {
          journeyConfig <- journeyService.getJourneyConfig(journeyId)
          firstName <- soleTraderIdentificationService
            .retrieveFullName(journeyId)
            .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
        } yield {
          val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
          implicit val messages: Messages = remoteMessagesApi.preferred(request)
          Ok(view(
            firstName,
            pageConfig = journeyConfig.pageConfig,
            formAction = routes.CaptureDateOfBirthController.submit(journeyId),
            form = captureDateOfBirthForm()
          ))
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        captureDateOfBirthForm().bindFromRequest.fold(
          formWithErrors => {
            for {
              journeyConfig <- journeyService.getJourneyConfig(journeyId)
              firstName <- soleTraderIdentificationService
                .retrieveFullName(journeyId)
                .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
            } yield {
              val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
              implicit val messages: Messages = remoteMessagesApi.preferred(request)
              BadRequest(view(
                firstName,
                pageConfig = journeyConfig.pageConfig,
                formAction = routes.CaptureDateOfBirthController.submit(journeyId),
                form = formWithErrors))
            }
          }
          ,
          dateOfBirth =>
            soleTraderIdentificationService.storeDateOfBirth(journeyId, dateOfBirth).map {
              _ => Redirect(routes.CaptureNinoController.show(journeyId))
            }
        )
      }
  }

}

