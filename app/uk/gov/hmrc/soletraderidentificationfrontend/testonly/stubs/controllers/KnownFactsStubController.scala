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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class KnownFactsStubController @Inject() (controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  val postCodeIsAbroadUtr: String = "0000000000"

  val noNinoDeclaredButNinoFoundUtr: String = "1234567891"

  val knownFactsNoContentUtr: String = "0000000001"

  def stubKnownFacts: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val knownFacts = (request.body \ "knownFacts").head.validate[JsObject]

    val (identifiersValueJson, verifiersValueJson): (Option[JsArray], Option[JsArray]) = knownFacts match {
      case JsSuccess(sautrBlock, _) =>
        (sautrBlock \ "value").validate[String] match {
          case JsSuccess(`postCodeIsAbroadUtr`, _) =>
            createStubResponseIdentifiersAndVerifiers(postCodeIsAbroadUtr, "IsAbroad", "Y")
          case JsSuccess(`noNinoDeclaredButNinoFoundUtr`, _) =>
            createStubResponseIdentifiersAndVerifiers(noNinoDeclaredButNinoFoundUtr, "NINO", "BB111111B")
          case JsSuccess(`knownFactsNoContentUtr`, _) =>
            createStubResponseIdentifiersAndVerifiers(knownFactsNoContentUtr)
          case JsSuccess(utr, _) =>
            createStubResponseIdentifiersAndVerifiers(utr, "Postcode", "AA1 1AA")
          case err: JsError => throw new InternalServerException(s"KnownFactsStubController: Error parsing SA Utr value - $err")
        }
      case _ => throw new InternalServerException("KnownFactsStubController: Error in parsing data posted to stub")
    }

    (identifiersValueJson, verifiersValueJson) match {
      case (Some(identifiers), Some(verifiers)) =>
        Future.successful(
          Ok(
            Json.obj(
              "service"               -> "IR-SA",
              "           enrolments" -> Json.arr(Json.obj("identifiers" -> identifiers, "verifiers" -> verifiers))
            )
          )
        )
      case (None, None) => Future.successful(NoContent)
      case _            => throw new InternalServerException("Known facts stub controller : Unexpected combination of identifiers and verifiers")
    }

  }

  private def createStubResponseIdentifiersAndVerifiers(utr: String, key: String = "", value: String = ""): (Option[JsArray], Option[JsArray]) = {
    utr match {
      case `knownFactsNoContentUtr` => (None, None)
      case _ =>
        (
          Some(Json.arr(Json.obj("key" -> "UTR", value -> utr))),
          Some(Json.arr(Json.obj("key" -> key, "value" -> value)))
        )
    }
  }

}
