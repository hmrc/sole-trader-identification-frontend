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

package uk.gov.hmrc.soletraderidentificationfrontend.stubs

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

import java.time.format.DateTimeFormatter.ofPattern

trait AuthenticatorStub extends WireMockMethods {
  def stubMatch(authenticatorDetails: IndividualDetails)(status: Int, body: JsObject): Unit = {
    val nino = authenticatorDetails.optNino match {
      case Some(nino) => nino
      case _ => ""
    }
    when(method = POST,
      uri = s"/authenticator/match",
      body = Json.obj(
        "firstName" -> authenticatorDetails.firstName,
        "lastName" -> authenticatorDetails.lastName,
        "dateOfBirth" -> authenticatorDetails.dateOfBirth.format(ofPattern("uuuu-MM-dd")),
        "nino" -> nino
      )
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def stubMatchStub(authenticatorDetails: IndividualDetails)(status: Int, body: JsObject): Unit = {
    val nino = authenticatorDetails.optNino match {
      case Some(nino) => nino
      case _ => ""
    }

    when(method = POST,
      uri = s"/identify-your-sole-trader-business/test-only/authenticator/match",
      body = Json.obj(
        "firstName" -> authenticatorDetails.firstName,
        "lastName" -> authenticatorDetails.lastName,
        "dateOfBirth" -> authenticatorDetails.dateOfBirth.format(ofPattern("uuuu-MM-dd")),
        "nino" -> nino
      )
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def successfulMatchJson(authenticatorDetails: IndividualDetails): JsObject = {
    val nino = authenticatorDetails.optNino match {
      case Some(nino) => nino
      case _ => ""
    }

    Json.obj(
      "firstName" -> authenticatorDetails.firstName,
      "lastName" -> authenticatorDetails.lastName,
      "dateOfBirth" -> authenticatorDetails.dateOfBirth.format(ofPattern("uuuu-MM-dd")),
      "nino" -> nino
    ) ++ {
      authenticatorDetails.optSautr match {
        case Some(sautr) => Json.obj("saUtr" -> sautr)
        case None => Json.obj()
      }

    }
  }

  val mismatchErrorJson: JsObject = Json.obj(
    "errors" -> Json.arr("no_match_dob")
  )

  val notFoundErrorJson: JsObject = Json.obj(
    "errors" -> "CID returned no record"
  )
}
