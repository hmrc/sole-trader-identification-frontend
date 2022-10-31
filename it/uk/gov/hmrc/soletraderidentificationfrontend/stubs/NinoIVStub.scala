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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testDefaultServiceName, testDefaultWelshServiceName}
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

trait NinoIVStub extends WireMockMethods {

  def stubCreateNinoIdentityVerificationJourney(nino: String,
                                                journeyId: String,
                                                journeyConfig: JourneyConfig
                                               )(status: Int,
                                                 body: JsObject = Json.obj()): StubMapping =
    internalStubCreateNinoIvJourney(
      nino = nino,
      journeyId = journeyId,
      journeyConfig = journeyConfig,
      uriToPostTo = "/nino-identity-verification/journey")(status, body)

  def stubRetrieveNinoIVResult(journeyId: String)
                              (status: Int,
                               body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/nino-identity-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def stubCreateNinoIVJourneyFromStub(nino: String,
                                      journeyId: String,
                                      journeyConfig: JourneyConfig
                                     )(status: Int,
                                       body: JsObject = Json.obj()): StubMapping =
    internalStubCreateNinoIvJourney(
      nino = nino,
      journeyId = journeyId,
      journeyConfig = journeyConfig,
      uriToPostTo = "/identify-your-sole-trader-business/test-only/nino-identity-verification/journey")(status, body)

  def stubRetrieveNinoIVResultFromStub(journeyId: String)
                                      (status: Int,
                                       body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/identify-your-sole-trader-business/test-only/nino-identity-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  private def internalStubCreateNinoIvJourney(nino: String,
                                              journeyId: String,
                                              journeyConfig: JourneyConfig,
                                              uriToPostTo: String
                                             )(status: Int,
                                               body: JsObject): StubMapping = {

    val pageTitle: String = journeyConfig.pageConfig.labels
      .flatMap(_.optEnglishServiceName)
      .getOrElse(journeyConfig.pageConfig.optServiceName
        .getOrElse(testDefaultServiceName)
      )

    val welshPageTitle: String = journeyConfig.pageConfig.labels.flatMap(_.optWelshServiceName).getOrElse(testDefaultWelshServiceName)

    val postBody = Json.obj(
      "origin" -> journeyConfig.regime.toLowerCase,
      "identifiers" -> Json.arr(
        Json.obj(
          "nino" -> nino
        )
      ),
      "continueUrl" -> routes.NinoIVController.retrieveNinoIVResult(journeyId).url,
      "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
      "deskproServiceName" -> journeyConfig.pageConfig.deskProServiceId,
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "pageTitle" -> pageTitle
        ),
        "cy" -> Json.obj(
          "pageTitle" -> welshPageTitle
        )
      )
    )
    when(method = POST, uri = uriToPostTo, postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }


}
