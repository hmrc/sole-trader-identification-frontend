/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails

import javax.inject.{Inject, Singleton}

@Singleton
class StubAuthenticatorMatchController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  val stubMatch: Action[JsValue] = Action(parse.json) {
    request =>
      val authenticatorDetails = request.body.as[IndividualDetails]

      authenticatorDetails.lastName.toLowerCase match {
        case "fail" =>
          Unauthorized(Json.obj("errors" -> "DOB does not exist in CID"))
        case "not-found" =>
          Unauthorized(Json.obj("errors" -> "CID returned no record"))
        case "deceased" =>
          FailedDependency
        case "ppt-a" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1875540988"
          ))
        case "ppt-b" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1875540987"
          ))
        case "no-sautr" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino
          ))
        case "bv-test-a" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1188662968"
          ))
        case "bv-test-b" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "3550699947"
          ))
        case "bv-test-c" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "8113878100"
          ))
        case "bv-test-d" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "9083735242"
          ))
        case "bv-test-e" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1021150603"
          ))
        case "end-to-end-test" =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1234567899"
          ))
        case _ =>
          Ok(Json.obj(
            "firstName" -> authenticatorDetails.firstName,
            "lastName" -> authenticatorDetails.lastName,
            "dateOfBirth" -> authenticatorDetails.dateOfBirth,
            "nino" -> authenticatorDetails.optNino,
            "saUtr" -> "1234567890"
          ))
      }

  }

}
