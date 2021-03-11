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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.services.{CheckYourAnswersService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.check_your_answers_page

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           soleTraderIdentificationService: SoleTraderIdentificationService)
                                          (implicit val config: AppConfig, executionContext: ExecutionContext) extends FrontendController(mcc) {


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId).map {
        case Some(details) => Ok(view(routes.CheckYourAnswersController.submit(), journeyId, details))
        case None => throw new InternalServerException("Fail to retrieve data from database")
      }
  }

  val submit: Action[AnyContent] = Action {
    implicit request =>
      NotImplemented
  }
}
