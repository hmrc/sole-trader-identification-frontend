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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureAddressForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_address_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureAddressController @Inject() (mcc: MessagesControllerComponents,
                                          view: capture_address_page,
                                          soleTraderIdentificationService: SoleTraderIdentificationService,
                                          val authConnector: AuthConnector,
                                          journeyService: JourneyService,
                                          messagesHelper: MessagesHelper
                                         )(implicit val config: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
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
          Ok(
            view(
              firstName,
              journeyId  = journeyId,
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CaptureAddressController.submit(journeyId),
              form       = CaptureAddressForm.apply(),
              countries  = config.getOrderedCountryListByLanguage(request.messages.lang.code)
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
        CaptureAddressForm
          .apply()
          .bindFromRequest()
          .fold(
            formWithErrors => {
              for {
                journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
                firstName <- soleTraderIdentificationService
                               .retrieveFullName(journeyId)
                               .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
              } yield {
                val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
                implicit val messages: Messages = remoteMessagesApi.preferred(request)
                BadRequest(
                  view(
                    firstName,
                    journeyId  = journeyId,
                    pageConfig = journeyConfig.pageConfig,
                    formAction = routes.CaptureAddressController.submit(journeyId),
                    form       = formWithErrors,
                    countries  = config.getOrderedCountryListByLanguage(request.messages.lang.code)
                  )
                )
              }
            },
            address =>
              soleTraderIdentificationService.storeAddress(journeyId, address).map { _ =>
                Redirect(routes.CaptureSautrController.show(journeyId))
              }
          )
      case None =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

}
