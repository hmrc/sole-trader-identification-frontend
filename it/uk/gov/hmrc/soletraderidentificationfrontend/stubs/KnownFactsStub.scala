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

trait KnownFactsStub extends WireMockMethods {

  def stubGetEacdKnownFacts(sautr: String)(status: Int, body: JsObject = Json.obj()): StubMapping = {

    val json: JsObject = Json.obj(
      "service" -> "IR-SA",
      "knownFacts" -> Json.arr(
        Json.obj(
          "key"   -> "UTR",
          "value" -> sautr
        )
      )
    )

    when(method = POST, uri = "/enrolment-store-proxy/enrolment-store/enrolments", body = json)
      .thenReturn(
        status = status,
        body   = body
      )
  }

  def stubGetEacdKnownFactsFromStub(sautr: String)(status: Int, body: JsObject = Json.obj()): StubMapping = {

    val json: JsObject = Json.obj(
      "service" -> "IR-SA",
      "knownFacts" -> Json.arr(
        Json.obj(
          "key"   -> "UTR",
          "value" -> sautr
        )
      )
    )

    when(method = POST, uri = "/identify-your-sole-trader-business/test-only/enrolment-store/enrolments", body = json)
      .thenReturn(
        status = status,
        body   = body
      )
  }

  def verifyGetEacdKnownFacts(sautr: String): Unit = {
    val json: JsObject = Json.obj(
      "service" -> "IR-SA",
      "knownFacts" -> Json.arr(
        Json.obj(
          "key"   -> "UTR",
          "value" -> sautr
        )
      )
    )

    WiremockHelper.verifyPost(uri = "/enrolment-store-proxy/enrolment-store/enrolments", optBody = Some(json.toString()))

  }

  def verifyGetEacdKnownFactsFromStub(sautr: String): Unit = {

    val json: JsObject = Json.obj(
      "service" -> "IR-SA",
      "knownFacts" -> Json.arr(
        Json.obj(
          "key"   -> "UTR",
          "value" -> sautr
        )
      )
    )

    WiremockHelper.verifyPost(uri = "/identify-your-sole-trader-business/test-only/enrolment-store/enrolments", optBody = Some(json.toString()))

  }

}
