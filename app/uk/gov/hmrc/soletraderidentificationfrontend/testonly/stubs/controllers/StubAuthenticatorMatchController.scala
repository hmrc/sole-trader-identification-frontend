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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class StubAuthenticatorMatchController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  val stubMatch: Action[SoleTraderDetails] = Action.async(parse.json[SoleTraderDetails]) {

    implicit request =>
      request.body.lastName.toLowerCase match {
        case "pass" =>
          Future.successful(Ok(Json.obj("result" -> "match")))
        case "fail" =>
          Future.successful(Unauthorized(Json.obj("errors" -> "DOB does not exist in CID")))
        case "deceased" =>
          Future.successful(FailedDependency)
      }
  }

}
