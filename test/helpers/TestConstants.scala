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

package helpers

import play.api.libs.json.{JsNull, JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.BusinessVerificationStatus.{BusinessVerificationFailKey, BusinessVerificationPassKey, BusinessVerificationStatusKey, BusinessVerificationUnchallengedKey}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.DetailsMismatch
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testSafeId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testSautr: String = "1234567890"
  val testContinueUrl: String = "/test"
  val testBusinessVerificationRedirectUrl: String = "/business-verification-start"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testRegime: String = "VATC"
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(17)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testFullName: FullName = FullName(testFirstName, testLastName)
  val testFullNameLowecase: FullName = FullName("john", "smith")
  val testNino: String = "AA111111A"
  val testTrn: String = "99A99999"
  val testAddress: Address = Address("line1", "line2", Some("line3"), Some("line4"), Some("line5"), Some("AA1 1AA"), "GB")
  val testAddressWrongPostcodeFormat: Address = Address("line1", "line2", Some("line3"), Some("line4"), Some("line5"), Some("AA11AA"), "GB")
  val testOverseasAddress: Address = Address("line1", "line2", Some("line3"), Some("line4"), Some("line5"), None, "US")
  val testSaPostcode: String = "AA1 1AA"
  val testOverseasIdentifiers: Overseas = Overseas("134124532", "AL")
  val testDefaultServiceName: String = "Entity Validation Service"
  val testServiceName: String = "Test Service"

  val testPassedBusinessVerificationStatusJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey)

  val testFailedBusinessVerificationStatusJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationFailKey)

  val testUnchallengedBusinessVerificationStatusJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationUnchallengedKey)

  val testRegistrationSuccess: String = "success"
  val testRegistrationFailed: String  = "fail"
  val testRegistrationNotCalled: String = "not called"

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = Some(BusinessVerificationPass),
      registrationStatus = Some(Registered(testSafeId)),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsNoMatch: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = Some(testSautr),
      identifiersMatch = false,
      businessVerification = Some(BusinessVerificationUnchallenged),
      registrationStatus = Some(RegistrationNotCalled),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsNoSautr: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = None,
      identifiersMatch = true,
      businessVerification = Some(BusinessVerificationUnchallenged),
      registrationStatus = Some(RegistrationNotCalled),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsNoNinoButUtr: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = None,
      address = Some(testAddress),
      optSaPostcode = Some(testSaPostcode),
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = Some(BusinessVerificationUnchallenged),
      registrationStatus = Some(RegistrationNotCalled),
      optTrn = Some(testTrn),
      optOverseas = Some(testOverseasIdentifiers)
    )

  val testSoleTraderWithoutBVCheckDetails: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = None,
      registrationStatus = Some(Registered(testSafeId)),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsRegistrationFailed: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = Some(BusinessVerificationPass),
      registrationStatus = Some(RegistrationFailed),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsNoNinoAndOverseas: SoleTraderDetails = testSoleTraderDetailsNoNinoButUtr.copy(address = Some(testOverseasAddress))

  val testIndividualDetails: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      optSautr = Some(testSautr)
    )

  val testIndividualDetailsNoSautr: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      optSautr = None
    )

  val testIndividualDetailsNoNinoNoSautr: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = None,
      optSautr = None
    )

  val testIndividualDetailsNoNino: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = None,
      optSautr = Some(testSautr)
    )

  val testKnownFactsResponseOverseas: KnownFactsResponse =
    KnownFactsResponse(
      postcode = None,
      isAbroad = Some(true),
      nino = None
    )

  val testKnownFactsResponseUK: KnownFactsResponse =
    KnownFactsResponse(
      postcode = Some("AA1 1AA"),
      isAbroad = Some(false),
      nino = None
    )

  val testSoleTraderJourneyConfig: JourneyConfig = testJourneyConfig(enableSautrCheck = true)

  val testSoleTraderJourneyConfigWithCallingService: JourneyConfig = testJourneyConfig(enableSautrCheck = true,  optServiceName = Some(testServiceName))

  val testSoleTraderJourneyConfigWithBVCheckDisabled: JourneyConfig = testJourneyConfig(enableSautrCheck = true, businessVerificationCheck = false)

  val testIndividualJourneyConfig: JourneyConfig = testJourneyConfig()

  val testIndividualJourneyConfigWithCallingService: JourneyConfig = testJourneyConfig(optServiceName = Some(testServiceName))

  val testIndividualSuccessfulAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "nino" -> testNino,
    "identifiersMatch" -> true,
    "authenticatorResponse" -> Json.toJson(testIndividualDetailsNoSautr)
  )

  val testIndividualSuccessfulWithCallingServiceAuditEventJson: JsObject =
    testIndividualSuccessfulAuditEventJson ++ Json.obj("callingService" -> testServiceName)

  def testJourneyConfig(
                         enableSautrCheck: Boolean = false,
                         optServiceName: Option[String] = None,
                         businessVerificationCheck: Boolean = true): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    businessVerificationCheck = businessVerificationCheck,
    pageConfig = PageConfig(
      optServiceName = optServiceName,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      enableSautrCheck = enableSautrCheck,
      accessibilityUrl = testAccessibilityUrl,
      optFullNamePageLabel = None,
    ),
    testRegime
  )

  def testSoleTraderAuditEventJson(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "nino" -> testNino,
    "dateOfBirth" -> testDateOfBirth,
    "authenticatorResponse" -> Json.toJson(testIndividualDetails),
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testPassedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationSuccess
  )

  def testSoleTraderWithCallingServiceAuditEventJson(identifiersMatch: Boolean = false): JsObject =
    testSoleTraderAuditEventJson(identifiersMatch) ++ Json.obj("callingService" -> testServiceName)

  def testSoleTraderAuditEventJsonNoSautr(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "nino" -> testNino,
    "dateOfBirth" -> testDateOfBirth,
    "authenticatorResponse" -> Json.toJson(testIndividualDetailsNoSautr),
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationNotCalled
  )

  def testSoleTraderAuditEventJsonNoNino(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "address" -> testAddress,
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationNotCalled,
    "TempNI" -> testTrn,
    "ES20Response" -> testKnownFactsResponseUK,
    "SAPostcode" -> testSaPostcode,
    "overseasTaxIdentifier" -> testOverseasIdentifiers.taxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseasIdentifiers.country
  )

  def testSoleTraderAuditEventJsonNoNinoOverseas(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "address" -> testOverseasAddress,
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationNotCalled,
    "TempNI" -> testTrn,
    "ES20Response" -> testKnownFactsResponseOverseas,
    "SAPostcode" -> testSaPostcode,
    "overseasTaxIdentifier" -> testOverseasIdentifiers.taxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseasIdentifiers.country
  )

  def testSoleTraderFailureAuditEventJson(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "nino" -> testNino,
    "dateOfBirth" -> testDateOfBirth,
    "authenticatorResponse" -> DetailsMismatch.toString,
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationNotCalled
  )

  def testSoleTraderWithoutBVCheckAuditEventJson(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "nino" -> testNino,
    "dateOfBirth" -> testDateOfBirth,
    "authenticatorResponse" -> Json.toJson(testIndividualDetails),
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> JsNull,
    "RegisterApiStatus" -> testRegistrationSuccess
  )

  def testSoleTraderRegistrationFailedAuditEventJson(identifiersMatch: Boolean = false): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Sole Trader",
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "nino" -> testNino,
    "dateOfBirth" -> testDateOfBirth,
    "authenticatorResponse" -> Json.toJson(testIndividualDetails),
    "userSAUTR" -> testSautr,
    "sautrMatch" -> identifiersMatch,
    "VerificationStatus" -> testPassedBusinessVerificationStatusJson,
    "RegisterApiStatus" -> testRegistrationFailed
  )

  val testIndividualFailureAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "nino" -> testNino,
    "identifiersMatch" -> false,
    "authenticatorResponse" -> DetailsMismatch.toString
  )


}
