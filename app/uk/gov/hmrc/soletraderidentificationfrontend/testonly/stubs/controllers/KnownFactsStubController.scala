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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class KnownFactsStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  val postCodeIsAbroadUtr: String = "0000000000"

  val noNinoDeclaredButNinoFoundUtr: String = "1234567891"

  def stubKnownFacts: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      val knownFacts = (request.body \ "knownFacts").head.validate[JsObject]
      val (identifiersValueJson, verifiersValueJson): (JsArray, JsArray) = knownFacts match {
        case JsSuccess(sautrBlock, _) =>
          (sautrBlock \ "value").validate[String] match {
            case JsSuccess(`postCodeIsAbroadUtr`, _) =>
              (
                identifiersJson(utr = postCodeIsAbroadUtr),
                Json.arr(
                  Json.obj(
                    "key" -> "IsAbroad",
                    "value" -> "Y"
                  )
                )
              )
            case JsSuccess(`noNinoDeclaredButNinoFoundUtr`, _) =>
              (
                identifiersJson(utr = noNinoDeclaredButNinoFoundUtr),
                Json.arr(
                  Json.obj(
                    "key" -> "NINO",
                    "value" -> "BB111111B"
                  )
                )
              )
            case JsSuccess(utr, _) =>
              (
                identifiersJson(utr),
                Json.arr(
                  Json.obj(
                    "key" -> "Postcode",
                    "value" -> "AA1 1AA"
                  )
                )
              )
          }
        case _ => throw new InternalServerException("KnownFactsStubController: Error in parsing data posted to stub")
      }

      Future.successful(Ok(Json.obj(
        "service" -> "IR-SA",
        "enrolments" -> Json.arr(
          Json.obj(
            "identifiers" -> identifiersValueJson,
            "verifiers" -> verifiersValueJson
          )
        )
      )))

  }

  private def identifiersJson(utr: String): JsArray =
    Json.arr(
      Json.obj(
        "key" -> "UTR",
        "value" -> utr
      )
    )
}
