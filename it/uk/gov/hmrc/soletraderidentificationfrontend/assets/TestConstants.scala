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
import uk.gov.hmrc.soletraderidentificationfrontend.models.BusinessVerificationStatus.{BusinessVerificationFailKey, BusinessVerificationPassKey, BusinessVerificationStatusKey, BusinessVerificationUnchallengedKey}
import uk.gov.hmrc.soletraderidentificationfrontend.models.RegistrationStatus.{RegisteredKey, RegistrationFailedKey, RegistrationNotCalledKey, registeredBusinessPartnerIdKey, registrationStatusKey}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(17)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testFullName: FullName = FullName(testFirstName, testLastName)
  val testFullNameLowerCase: FullName = FullName("john", "smith")
  val testNino: String = "AA111111A"
  val testSautr: String = "1234567890"
  val testContinueUrl = "/test-continue-url"
  val testRegime: String = "VATC"
  val testBusinessVerificationRedirectUrl = "/business-verification-start"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testSafeId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testGGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testTrn: String = "99A99999"
  val testAddress: Address = Address("line1", "line2", Some("line3"), Some("line4"), Some("line5"), Some("AA1 1AA"), "GB")
  val testAddressWrongPostcodeFormat: Address = Address("line1", "line2", Some("line3"), Some("line4"), Some("line5"), Some("AA11AA"), "GB")
  val testNonUKAddress: Address = Address("testLine1", "testLine2", Some("testTown"), None, None, None, "PT")
  val testAddress1: String = "line1"
  val testAddress2: String = "line2"
  val testAddress3: String = "line3"
  val testAddress4: String = "line4"
  val testAddress5: String = "line5"
  val testPostcode: String = "AA1 1AA"
  val testCountry: String = "GB"
  val testCountryName: String = "United Kingdom"
  val testSaPostcode: String = "AA00 0AA"
  val testOverseasTaxIdentifiers: Overseas = Overseas("134124532", "AL")

  val testBusinessVerificationPassJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey)
  val testBusinessVerificationFailJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationFailKey)
  val testBusinessVerificationUnchallengedJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationUnchallengedKey)

  val testSuccessfulRegistrationJson: JsObject = Json.obj(
    registrationStatusKey -> RegisteredKey,
    registeredBusinessPartnerIdKey -> testSafeId)
  val testFailedRegistrationJson: JsObject = Json.obj(registrationStatusKey -> RegistrationFailedKey)
  val testRegistrationNotCalledJson: JsObject = Json.obj(registrationStatusKey -> RegistrationNotCalledKey)

  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"

  val testFullNamePageLabel: String = "What is the name of the nominated partner?"

  val testIndividualPageConfig: PageConfig = PageConfig(
    optServiceName = None,
    deskProServiceId = testDeskProServiceId,
    signOutUrl = testSignOutUrl,
    enableSautrCheck = false,
    accessibilityUrl = testAccessibilityUrl,
    optFullNamePageLabel = None
  )

  val testSoleTraderPageConfig: PageConfig = testIndividualPageConfig.copy(enableSautrCheck = true)

  val testIndividualJourneyConfig: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    businessVerificationCheck = false,
    pageConfig = testIndividualPageConfig,
    testRegime
  )

  val testIndividualJourneyConfigJsonNoRegime: JsObject =
    Json.obj(
      "continueUrl" -> testContinueUrl,
      "pageConfig" -> Json.obj(
        "enableSautrCheck" -> false,
        "deskProServiceId" -> testDeskProServiceId,
        "signOutUrl" -> testSignOutUrl,
        "accessibilityUrl" -> testAccessibilityUrl
      ),
      "businessVerificationCheck" -> false
    )

  val testSoleTraderJourneyConfig: JourneyConfig = testIndividualJourneyConfig
    .copy(businessVerificationCheck = true)
    .copy(pageConfig = testSoleTraderPageConfig)

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = Some(testSaPostcode),
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = Some(BusinessVerificationPass),
      registrationStatus = Some(Registered(testSafeId)),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsMismatch: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = Some(testSaPostcode),
      optSautr = Some(testSautr),
      identifiersMatch = false,
      businessVerification = Some(BusinessVerificationUnchallenged),
      registrationStatus = Some(RegistrationNotCalled),
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsNoBV: SoleTraderDetails = testSoleTraderDetails.copy(businessVerification = None)

  val testSoleTraderDetailsIndividualJourney: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      address = None,
      optSaPostcode = None,
      optSautr = None,
      identifiersMatch = true,
      businessVerification = None,
      registrationStatus = None,
      optTrn = None,
      optOverseas = None
    )

  val testSoleTraderDetailsIndividualJourneyNoNino: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      optNino = None,
      address = None,
      optSaPostcode = None,
      optSautr = None,
      identifiersMatch = false,
      businessVerification = None,
      registrationStatus = None,
      optTrn = None,
      optOverseas = None
    )

  val testIndividualDetails: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = Some(testNino),
      optSautr = Some(testSautr)
    )

  val testIndividualDetailsLowerCaseNino: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino = Some("aa111111a"),
      optSautr = Some(testSautr)
    )

  val testIndividualDetailsLowerCaseFirstName: IndividualDetails = testIndividualDetails.copy(firstName = "john")

  val testIndividualDetailsLowerCaseLastName: IndividualDetails = testIndividualDetails.copy(lastName = "smith")

  val testIndividualDetailsNoSautr: IndividualDetails = testIndividualDetails.copy(optSautr = None)

  val testSoleTraderDetailsJson: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "saPostcode" -> testSaPostcode,
      "sautr" -> testSautr,
      "identifiersMatch" -> true,
      "businessVerification" -> testBusinessVerificationPassJson,
      "registration" -> testSuccessfulRegistrationJson
    )
  }

  val testSoleTraderDetailsJsonMisMatch: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "saPostcode" -> testSaPostcode,
      "sautr" -> testSautr,
      "identifiersMatch" -> false,
      "businessVerification" -> testBusinessVerificationUnchallengedJson,
      "registration" -> testRegistrationNotCalledJson
    )
  }

  val testSoleTraderDetailsJsonIndividual: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "nino" -> testNino,
      "dateOfBirth" -> testDateOfBirth,
      "identifiersMatch" -> true
    )
  }

  val testSoleTraderDetailsJsonIndividualNoNino: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "identifiersMatch" -> false
    )
  }

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

  val testIndividualDetailsJsonNoNino: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "sautr" -> testSautr
    )
  }

  val testIndividualDetailsJsonNoNinoNoSautr: JsObject = {
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth
    )
  }

  val testKnownFactsResponse: JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key" -> "Postcode",
            "value" -> testSaPostcode
          )
        )
      )
    )
  )
  val testKnownFactsResponseNino: JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key" -> "NINO",
            "value" -> testNino
          ),
          Json.obj(
            "key" -> "Postcode",
            "value" -> testSaPostcode
          )
        )
      )
    )
  )
  val testAddressJson: JsObject = Json.obj(
    "line1" -> "line1",
    "line2" -> "line2",
    "line3" -> "line3",
    "line4" -> "line4",
    "line5" -> "line5",
    "postcode" -> "AA1 1AA",
    "countryCode" -> "GB"
  )
  val testOverseasTaxIdentifiersJson: JsObject = Json.obj(
    "taxIdentifier" -> "134124532",
    "country" -> "AL"
  )

  def testKnownFactsResponseIsAbroad(abroad: String = "Y"): JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key" -> "IsAbroad",
            "value" -> abroad
          )
        )
      )
    )
  )

}
