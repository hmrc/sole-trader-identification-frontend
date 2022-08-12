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
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{SoleTraderDetailsMatchFailure, SoleTraderDetailsMatchResult}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

import java.time.LocalDate

trait SoleTraderIdentificationStub extends WireMockMethods {

  def stubStoreFullName(journeyId: String, fullName: FullName)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/fullName",
      body = Json.obj(
        "firstName" -> fullName.firstName,
        "lastName" -> fullName.lastName
      )
    ).thenReturn(
      status = status
    )

  def stubStoreNino(journeyId: String, nino: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/nino",
      body = JsString(nino)
    ).thenReturn(
      status = status
    )

  def stubStoreAddress(journeyId: String, address: Address)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/address",
      body = Json.toJson(
        address
      )
    ).thenReturn(
      status = status
    )

  def stubStoreTrn(journeyId: String, trn: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/trn",
      body = JsString(trn)
    ).thenReturn(
      status = status
    )

  def stubStoreSautr(journeyId: String, sautr: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr", body = JsString(sautr)
    ).thenReturn(
      status = status
    )

  def stubStoreSaPostcode(journeyId: String, saPostcode: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/saPostcode",
      body = JsString(saPostcode)
    ).thenReturn(
      status = status
    )

  def stubStoreDob(journeyId: String, dateOfBirth: LocalDate)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/dateOfBirth", body = Json.toJson(dateOfBirth)
    ).thenReturn(
      status = status
    )

  def stubStoreIdentifiersMatch(journeyId: String, identifiersMatch: SoleTraderDetailsMatchResult)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/identifiersMatch", body = JsString(identifiersMatch.toString)
    ).thenReturn(
      status = status
    )

  def stubStoreNinoInsights(journeyId: String, insights: JsObject)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/reputation", body = insights
    ).thenReturn(
      status = status
    )

  def stubStoreAuthenticatorDetails(journeyId: String, authenticatorDetails: IndividualDetails)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorDetails", body = Json.toJson(authenticatorDetails)
    ).thenReturn(
      status = status
    )

  def stubStoreAuthenticatorFailureResponse(journeyId: String, authenticatorFailureResponse: SoleTraderDetailsMatchFailure)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorFailureResponse", body = JsString(authenticatorFailureResponse.toString)
    ).thenReturn(
      status = status
    )

  def stubStoreES20Details(journeyId: String, es20Details: KnownFactsResponse)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/es20Details", body = Json.toJson(es20Details)
    ).thenReturn(
      status = status
    )

  def stubStoreOverseasTaxIdentifiers(journeyId: String, taxIdentifiers: Overseas)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/overseas", body = Json.toJson(taxIdentifiers)
    ).thenReturn(
      status = status
    )

  def stubStoreOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String)(status: Int): Unit =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/overseasTaxIdentifiers",
      body = JsString(overseasTaxIdentifier)
    ).thenReturn(
      status = status
    )

  def stubStoreOverseasTaxIdentifiersCountry(journeyId: String, country: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/country", body = JsString(country)
    ).thenReturn(
      status = status
    )

  def verifyStoreOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/overseasTaxIdentifiers",
      optBody = Some(JsString(overseasTaxIdentifier).toString())
    )


  def stubRetrieveSoleTraderDetails(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveAuthenticatorDetails(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorDetails"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveAuthenticatorFailureResponse(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorFailureResponse"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveIndividualDetails(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveFullName(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/fullName"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveDob(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/dateOfBirth"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveNino(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/nino"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )

  def stubRetrieveSautr(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )

  def stubRetrieveSaPostcode(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/saPostcode"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )

  def stubRetrieveOverseasTaxIdentifiers(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/overseas"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveOverseasTaxIdentifier(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/overseasTaxIdentifiers"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveAddress(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/address"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveTrn(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/trn"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveSautr(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveSaPostcode(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/saPostcode"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveNino(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/nino"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveAddress(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/address"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveInsights(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/reputation"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveOverseasTaxIdentifiers(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/overseas"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveOverseasTaxIdentifier(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/overseasTaxIdentifiers"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveOverseasTaxIdentifiersCountry(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/overseas-country"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveAllData(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubStoreBusinessVerificationStatus(journeyId: String,
                                          businessVerificationStatus: BusinessVerificationStatus
                                         )(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/businessVerification",
      body = Json.toJson(businessVerificationStatus)
    ).thenReturn(
      status = status
    )

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit = {
    val jsonBody = Json.toJson(businessVerificationStatus)
    WiremockHelper.verifyPut(uri = s"/sole-trader-identification/journey/$journeyId/businessVerification", optBody = Some(jsonBody.toString()))
  }

  def stubRetrieveBusinessVerificationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/businessVerification"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveIdentifiersMatch(journeyId: String)(status: Int, identifiersMatch: SoleTraderDetailsMatchResult): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/identifiersMatch"
    ).thenReturn(
      status = status,
      body = JsString(identifiersMatch.toString)
    )

  def stubRetrieveInsights(journeyId: String)(status: Int, insights: JsObject = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/reputation"
    ).thenReturn(
      status = status,
      body = insights
    )

  def stubRetrieveRegistrationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/registration"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveES20Result(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/es20Details"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)(status: Int): StubMapping = {
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/registration",
      body = Json.toJsObject(registrationStatus)
    ).thenReturn(
      status = status
    )
  }

  def verifyStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus): Unit = {
    val jsonBody = Json.toJsObject(registrationStatus)
    WiremockHelper.verifyPut(uri = s"/sole-trader-identification/journey/$journeyId/registration", optBody = Some(jsonBody.toString()))
  }

  def verifyStoreAuthenticatorDetails(journeyId: String, authenticatorDetails: IndividualDetails): Unit = {
    val jsonBody = Json.toJsObject(authenticatorDetails)
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorDetails",
      optBody = Some(jsonBody.toString())
    )
  }

  def verifyStoreAuthenticatorFailureResponse(journeyId: String, authenticatorFailureResponse: SoleTraderDetailsMatchFailure): Unit = {
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/authenticatorFailureResponse",
      optBody = Some(JsString(authenticatorFailureResponse.toString).toString())
    )
  }

  def verifyStoreIdentifiersMatch(journeyId: String, expBody: JsValue): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/identifiersMatch",
      optBody = Some(Json.stringify(expBody))
    )

  def verifyStoreNinoInsights(journeyId: String, expBody: JsObject): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/reputation",
      optBody = Some(Json.stringify(expBody))
    )

  def verifyStoreTrn(journeyId: String, trn: String): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/trn",
      optBody = Some(JsString(trn).toString())
    )

  def verifyStoreES20Details(journeyId: String, es20Details: KnownFactsResponse): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/es20Details",
      optBody = Some(Json.toJsObject(es20Details).toString())
    )

  def verifyRemoveAllData(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/sole-trader-identification/journey/$journeyId")

  def verifyRemoveOverseasTaxIdentifier(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/sole-trader-identification/journey/$journeyId/overseasTaxIdentifiers")

  def verifyStoreCountry(journeyId: String, country: String): Unit =
    WiremockHelper.verifyPut(
      uri = s"/sole-trader-identification/journey/$journeyId/country",
      optBody = Some(JsString(country).toString())
    )

}
