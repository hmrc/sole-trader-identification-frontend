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
import uk.gov.hmrc.soletraderidentificationfrontend.services.JourneyService
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.could_not_confirm_business_error_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

/**
 * The could not confirm business error page is displayed when a sole trader asserts they do not have
 * a Universal Taxpayer Reference, but one is found in their known facts.
 */
@Singleton
class CouldNotConfirmBusinessErrorController @Inject() (mcc: MessagesControllerComponents,
                                                        val authConnector: AuthConnector,
                                                        journeyService: JourneyService,
                                                        view: could_not_confirm_business_error_page,
                                                        messagesHelper: MessagesHelper
                                                       )(implicit appConfig: AppConfig, executionContext: ExecutionContext)
                                                       extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(){
        journeyService.getJourneyConfig(journeyId).map {
          journeyConfig =>
            val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
            implicit val messages: Messages = remoteMessagesApi.preferred(request)
            Ok(view(pageConfig = journeyConfig.pageConfig, redirectLocation = routes.RetryJourneyController.tryAgain(journeyId)))
        }
      }
  }

}
