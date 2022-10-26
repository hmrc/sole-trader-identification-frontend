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

import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, InjectedController}

import java.util.UUID
import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class NinoIVStubController extends InjectedController {

  private val origin = "vat"
  private val businessVerificationJourneyId = UUID.randomUUID.toString

  def createNinoIVJourney: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      val jsonBody = for {
        origin <- (request.body \ "origin").validate[String]
        _ <-  ((request.body \ "identifiers").head \ "nino").validate[String]
        continueUrl <- (request.body \ "continueUrl").validate[String]
        _ <- (request.body \ "accessibilityStatementUrl").validate[String]
        _ <- (request.body \ "pageTitle").validate[String]
        _ <- (request.body \ "deskproServiceName").validate[String]
      } yield (origin, continueUrl)
      jsonBody match {
        case JsSuccess((origin, _), _) if !origin.equals(origin.toLowerCase) =>
          Future.failed(new IllegalArgumentException(s"origin value $origin must be lower case"))
        case JsSuccess((_, continueUrl), _) =>
          Future.successful {
            Created(Json.obj(
              "redirectUri" -> (continueUrl + s"?journeyId=$businessVerificationJourneyId")
            ))
          }
        case _ =>
          Future.failed(new IllegalArgumentException(s"Request body for createNinoIVJourney stub failed verification"))
      }
  }

  def retrieveVerificationResult(businessVerificationJourneyId: String): Action[AnyContent] = Action.async {
    Future.successful {
      Ok(Json.obj(
          "journeyType" -> "NINO_IDENTITY_VERIFICATION",
          "origin" -> origin,
          "identifiers" -> Json.arr(
            { "nino" -> "AA111111A" }
          ),
          "verificationStatus" -> "PASS"
      ))
    }
  }

}

