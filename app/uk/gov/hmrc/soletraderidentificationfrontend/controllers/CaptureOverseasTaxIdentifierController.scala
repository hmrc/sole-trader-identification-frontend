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
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureOverseasTaxIdentifierForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_overseas_tax_identifier_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureOverseasTaxIdentifierController @Inject()(mcc: MessagesControllerComponents,
                                                       journeyService: JourneyService,
                                                       view: capture_overseas_tax_identifier_page,
                                                       soleTraderIdentificationService: SoleTraderIdentificationService,
                                                       messagesHelper: MessagesHelper,
                                                       val authConnector: AuthConnector
                                                      )(implicit val config: AppConfig,
                                                        executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              Ok(view(
                journeyId = journeyId,
                pageConfig = journeyConfig.pageConfig,
                formAction = routes.CaptureOverseasTaxIdentifierController.submit(journeyId),
                form = CaptureOverseasTaxIdentifierForm.form
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
          CaptureOverseasTaxIdentifierForm.form.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  BadRequest(view(
                    journeyId = journeyId,
                    pageConfig = journeyConfig.pageConfig,
                    formAction = routes.CaptureOverseasTaxIdentifierController.submit(journeyId),
                    form = formWithErrors
                  ))
              },
            {
              case Some(overseasTaxIdentifier) => for {
                _ <- soleTraderIdentificationService.storeOverseasTaxIdentifier(journeyId, overseasTaxIdentifier)
              } yield {
                Redirect(routes.CaptureOverseasTaxIdentifierCountryController.show(journeyId))
              }
              case None => for {
                _ <- soleTraderIdentificationService.removeOverseasTaxIdentifier(journeyId)
                _ <- soleTraderIdentificationService.removeOverseasTaxIdentifierCountry(journeyId)
              } yield {
                Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
            }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}