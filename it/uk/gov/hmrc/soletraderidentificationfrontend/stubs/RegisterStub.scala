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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  def stubRegister(nino: String, optSautr: Option[String], regime: String)(status: Int, body: JsObject): StubMapping = {
    val detailsJson = Json.obj(
      "nino" -> nino.toUpperCase,
      "regime" -> regime
    ) ++ {
      optSautr match {
        case Some(sautr) => Json.obj("sautr" -> sautr)
        case _ => Json.obj()
      }
    }

    val jsonBody = Json.obj(
      "soleTrader" -> detailsJson
    )

    when(method = POST, uri = "/sole-trader-identification/register", jsonBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRegisterWithTrn(trn: String, sautr: String, regime: String)(status: Int, body: JsObject): StubMapping = {
    val jsonBody = Json.obj(
      "trn" -> trn,
      "sautr" -> sautr,
      "regime" -> regime
    )

    when(method = POST, uri = "/sole-trader-identification/register-trn", jsonBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyRegister(nino: String, optSautr: Option[String], regime: String): Unit = {
    val detailsJson = Json.obj(
      "nino" -> nino.toUpperCase,
      "regime" -> regime
    ) ++ {
      optSautr match {
        case Some(sautr) => Json.obj("sautr" -> sautr)
        case _ => Json.obj()
      }
    }

    val jsonBody = Json.obj(
      "soleTrader" -> detailsJson
    )

    WiremockHelper.verifyPost(uri = "/sole-trader-identification/register", optBody = Some(jsonBody.toString()))
  }

  def verifyRegisterWithTrn(trn: String, sautr: String, regime: String): Unit = {
    val jsonBody = Json.obj(
      "trn" -> trn,
      "sautr" -> sautr,
      "regime" -> regime
    )

    WiremockHelper.verifyPost(uri = "/sole-trader-identification/register-trn", optBody = Some(jsonBody.toString()))

  }
}
