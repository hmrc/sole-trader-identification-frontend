/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.{Individual, SoleTrader}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{AuthenticatorDetails, BusinessVerificationPass, BusinessVerificationUnchallenged, FullName, JourneyConfig, PageConfig, Registered, RegistrationNotCalled, SoleTraderDetails}

import java.time.LocalDate
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(17)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testFullName: FullName = FullName(testFirstName, testLastName)
  val testNino: String = "AA111111A"
  val testSautr: String = "1234567890"
  val testContinueUrl = "/test-continue-url"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testSafeId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testGGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString

  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"

  val testSoleTraderJourneyConfig: JourneyConfig = JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl), SoleTrader)
  val testSoleTraderJourneyConfigSautrEnabled: JourneyConfig = JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl, true), SoleTrader)
  val testIndividualJourneyConfig: JourneyConfig = JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl), Individual)

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      testFullName,
      testDateOfBirth,
      testNino,
      Some(testSautr),
      BusinessVerificationPass,
      Registered(testSafeId)
    )

  val testSoleTraderDetailsNoSautr: SoleTraderDetails =
    SoleTraderDetails(
      testFullName,
      testDateOfBirth,
      testNino,
      None,
      BusinessVerificationUnchallenged,
      RegistrationNotCalled
    )

  val testAuthenticatorDetails: AuthenticatorDetails =
    AuthenticatorDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      Some(testSautr)
    )

  val testAuthenticatorDetailsNoSatur: AuthenticatorDetails =
    AuthenticatorDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      None
    )

  val testSoleTraderDetailsJson: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "sautr" -> testSautr,
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "PASS"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> testSafeId
      )
    )
  }

  val testSoleTraderDetailsNoSautrJson: JsObject =
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "UNCHALLENGED"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_NOT_CALLED"
      )
    )

  val testAuthenticatorDetailsJson: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "sautr" -> testSautr
    )
  }

  val testAuthenticatorDetailsJsonNoSautr: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino
    )
  }

}
