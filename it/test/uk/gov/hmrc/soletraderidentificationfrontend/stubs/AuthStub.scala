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
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.test.Helpers.UNAUTHORIZED
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

trait AuthStub extends WireMockMethods {

  val authUrl = "/auth/authorise"
  val IRSaEnrolmentKey = "IR-SA"
  val IRSaReferenceKey = "UTR"

  def stubAuth[T](status: Int, body: T)(implicit writes: Writes[T]): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = status, body = writes.writes(body))
  }

  def stubAuthFailure(): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = UNAUTHORIZED, headers = Map(HeaderNames.WWW_AUTHENTICATE -> s"""MDTP detail="MissingBearerToken""""))
  }

  def successfulAuthResponse(enrolments: JsObject*): JsObject = Json.obj(
    "optionalCredentials" -> Json.obj(
      "providerId"   -> testCredentialId,
      "providerType" -> testGGProviderId
    ),
    "groupIdentifier" -> testGroupId,
    "internalId"      -> Some(testInternalId),
    "allEnrolments"   -> enrolments
  )

  def irSaEnrolment(utr: String): JsObject = Json.obj(
    "key" -> IRSaEnrolmentKey,
    "identifiers" -> Json.arr(
      Json.obj(
        "key"   -> IRSaReferenceKey,
        "value" -> utr
      )
    )
  )

}
