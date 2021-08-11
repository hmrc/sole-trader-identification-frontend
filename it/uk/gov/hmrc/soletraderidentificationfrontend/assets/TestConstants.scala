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
import uk.gov.hmrc.soletraderidentificationfrontend.models._

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
  val testBusinessVerificationRedirectUrl = "/business-verification-start"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testSafeId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testGGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString

  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"

  val testSoleTraderJourneyConfig: JourneyConfig =
    JourneyConfig(
      continueUrl = testContinueUrl,
      pageConfig = PageConfig(
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        enableSautrCheck = true
      )
    )

  val testIndividualJourneyConfig: JourneyConfig =
    JourneyConfig(
      continueUrl = testContinueUrl,
      pageConfig = PageConfig(
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl
      )
    )

  def testSoleTraderDetails(identifiersMatch: Boolean = false): SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = Some(testSautr),
      identifiersMatch = identifiersMatch,
      businessVerification = BusinessVerificationPass,
      registrationStatus = Registered(testSafeId)
    )

  def testSoleTraderDetailsNoSautr(identifiersMatch: Boolean = false): SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = None,
      identifiersMatch = identifiersMatch,
      businessVerification = BusinessVerificationUnchallenged,
      registrationStatus = RegistrationNotCalled
    )

  val testIndividualDetails: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = Some(testSautr)
    )

  val testIndividualDetailsNoSautr: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = None
    )

  def testSoleTraderDetailsJson(identifiersMatch: Boolean = false): JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "sautr" -> testSautr,
      "identifiersMatch" -> identifiersMatch,
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "PASS"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> testSafeId
      )
    )
  }

  def testSoleTraderDetailsJsonNoSautr(identifiersMatch: Boolean = false): JsObject =
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "identifiersMatch" -> identifiersMatch,
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "UNCHALLENGED"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_NOT_CALLED"
      )
    )

  val testIndividualDetailsJson: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "sautr" -> testSautr
    )
  }

  val testIndividualDetailsJsonNoSautr: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino
    )
  }

}
