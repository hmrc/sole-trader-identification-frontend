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
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Address, FullName}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

import java.time.LocalDate

trait CreateTrnStub extends WireMockMethods {

  def stubCreateTrn(dateOfBirth: LocalDate, fullName: FullName, address: Address)(status: Int, body: JsObject = Json.obj()): StubMapping = {
    val jsonBody = Json.obj(
      "dateOfBirth" -> dateOfBirth,
      "fullName"    -> fullName,
      "address"     -> address
    )

    when(method = POST, uri = "/sole-trader-identification/get-trn", body = jsonBody)
      .thenReturn(
        status = status,
        body   = body
      )

  }
}
