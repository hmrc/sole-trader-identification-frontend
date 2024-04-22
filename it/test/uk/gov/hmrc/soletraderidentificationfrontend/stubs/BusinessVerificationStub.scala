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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testDefaultServiceName
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

trait BusinessVerificationStub extends WireMockMethods {

  def stubCreateBusinessVerificationJourney(sautr: String, journeyId: String, journeyConfig: JourneyConfig)(status: Int,
                                                                                                            body: JsObject = Json.obj()
                                                                                                           ): StubMapping =
    internalStubCreateBusinessVerificationJourney(sautr         = sautr,
                                                  journeyId     = journeyId,
                                                  journeyConfig = journeyConfig,
                                                  uriToPostTo   = "/business-verification/journey"
                                                 )(status, body)

  def stubRetrieveBusinessVerificationResult(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body   = body
      )

  def stubCreateBusinessVerificationJourneyFromStub(sautr: String, journeyId: String, journeyConfig: JourneyConfig)(status: Int,
                                                                                                                    body: JsObject = Json.obj()
                                                                                                                   ): StubMapping =
    internalStubCreateBusinessVerificationJourney(sautr         = sautr,
                                                  journeyId     = journeyId,
                                                  journeyConfig = journeyConfig,
                                                  uriToPostTo   = "/identify-your-sole-trader-business/test-only/business-verification/journey"
                                                 )(status, body)

  def stubRetrieveBusinessVerificationResultFromStub(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/identify-your-sole-trader-business/test-only/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body   = body
      )

  private def internalStubCreateBusinessVerificationJourney(sautr: String, journeyId: String, journeyConfig: JourneyConfig, uriToPostTo: String)(
    status: Int,
    body: JsObject
  ): StubMapping = {

    val pageTitle: String = journeyConfig.pageConfig.optServiceName.getOrElse(testDefaultServiceName)

    val postBody = Json.obj(
      "journeyType" -> "BUSINESS_VERIFICATION",
      "origin"      -> journeyConfig.regime.toLowerCase,
      "identifiers" -> Json.arr(
        Json.obj(
          "saUtr" -> sautr
        )
      ),
      "continueUrl"               -> routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
      "pageTitle"                 -> pageTitle,
      "deskproServiceName"        -> journeyConfig.pageConfig.deskProServiceId
    )
    when(method = POST, uri = uriToPostTo, postBody)
      .thenReturn(
        status = status,
        body   = body
      )
  }

}
