/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.sa_utr_page

import scala.concurrent.Future

@Singleton
class SaUtrController @Inject()(mcc: MessagesControllerComponents,
                                view: sa_utr_page)
                               (implicit val config: AppConfig) extends FrontendController(mcc) {

  val companyName = "John Smith"

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(routes.SaUtrController.submit(), companyName)))
  }

  val submit: Action[AnyContent] = Action { implicit request => NotImplemented }

}
