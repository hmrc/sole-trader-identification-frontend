/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CannotConfirmBusinessErrorForm.cannotConfirmBusinessForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{CreateTrnService, JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.cannot_confirm_business_error_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CannotConfirmBusinessErrorController @Inject() (mcc: MessagesControllerComponents,
                                                      view: cannot_confirm_business_error_page,
                                                      val authConnector: AuthConnector,
                                                      soleTraderIdentificationService: SoleTraderIdentificationService,
                                                      journeyService: JourneyService,
                                                      trnService: CreateTrnService,
                                                      messagesHelper: MessagesHelper
                                                     )(implicit val config: AppConfig, executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(internalId) {
      case Some(authInternalId) =>
        journeyService.getJourneyConfig(journeyId, authInternalId).map { journeyConfig =>
          val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
          implicit val messages: Messages = remoteMessagesApi.preferred(request)
          Ok(
            view(
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CannotConfirmBusinessErrorController.submit(journeyId),
              form       = cannotConfirmBusinessForm
            )
          )
        }
      case None =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(internalId) {
      case Some(authInternalId) =>
        cannotConfirmBusinessForm
          .bindFromRequest()
          .fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map { journeyConfig =>
                val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
                implicit val messages: Messages = remoteMessagesApi.preferred(request)
                BadRequest(
                  view(
                    pageConfig = journeyConfig.pageConfig,
                    formAction = routes.CannotConfirmBusinessErrorController.submit(journeyId),
                    form       = formWithErrors
                  )
                )
              },
            continue =>
              if (continue) {
                for {
                  optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
                  _ <- if (optNino.isEmpty) trnService.createTrn(journeyId) // Create TRN at end of journey to avoid duplication
                       else Future.unit
                  journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
                } yield Redirect(journeyConfig.continueUrl + s"?journeyId=$journeyId")
              } else {
                soleTraderIdentificationService.removeAllData(journeyId).map { _ =>
                  Redirect(routes.CaptureFullNameController.show(journeyId))
                }
              }
          )
      case None =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

}
