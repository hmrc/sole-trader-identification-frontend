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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

import play.api.libs.json.{JsArray, JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DetailsMismatch, KnownFactsNoContentKey, SuccessfulMatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testMinAge: Int = 17
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(testMinAge)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testFullName: FullName = FullName(testFirstName, testLastName)
  val testFullNameLowerCase: FullName = FullName("john", "smith")
  val testNino: String = "AA111111A"
  val testNinoRecordedByKnownFacts: String = "BB111111B"
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
  val testCorrelationId: String = UUID.randomUUID().toString
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
  val testOverseasTaxIdentifier: String = "123456234"
  val testOverseasTaxIdentifierCountry: String = "AL"

  val testBusinessVerificationPassJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey)
  val testBusinessVerificationFailJson: JsObject = Json.obj(BusinessVerificationStatusKey -> BusinessVerificationFailKey)
  val testBusinessVerificationNotEnoughInfoToCallJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallBVKey)
  val testBusinessVerificationNotEnoughInfoToChallengeJson: JsObject =
    Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToChallengeKey)
  val testBusinessVerificationUnchallengedJson: JsObject = Json.obj(BusinessVerificationStatusKey -> "UNCHALLENGED")

  val testSuccessfulRegistrationJson: JsObject = Json.obj("registrationStatus" -> "REGISTERED", "registeredBusinessPartnerId" -> testSafeId)

  def testFailedRegistrationJson(failures: JsArray): JsObject = Json.obj("registrationStatus" -> "REGISTRATION_FAILED", "failures" -> failures)

  val testRegistrationNotCalledJson: JsObject = Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")

  val testRegistrationFailure: List[Failure] = List(Failure("PARTY_TYPE_MISMATCH", "The remote endpoint has indicated there is Party Type mismatch"))

  val testMultipleRegistrationFailure: List[Failure] = List(
    Failure("INVALID_REGIME", "Request has not passed validation.  Invalid regime"),
    Failure("INVALID_PAYLOAD", "Request has not passed validation. Invalid payload.")
  )

  val testBackendSuccessfulRegistrationJson: JsObject = Json.obj("registration" -> testSuccessfulRegistrationJson)
  def testBackendFailedRegistrationJson(failures: JsArray): JsObject = Json.obj("registration" -> testFailedRegistrationJson(failures))

  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testTechnicalHelpUrl: String = "http://localhost:9250/contact/report-technical-problem?service=grs"
  val testDefaultServiceName: String = "Entity Validation Service"
  val testDefaultWelshServiceName: String = "Gwasanaeth Dilysu Endid"
  val testServiceName: String = "Test Service"
  val testFullNamePageLabel: String = "What is the name of the nominated partner?"
  val welshFullNamePageLabel: String = "Welsh " + testFullNamePageLabel
  val welshTestServiceName: String = "Welsh " + testServiceName

  val testInsightsReturnBody: JsObject = Json.obj(
    "ninoInsightsCorrelationId" -> testCorrelationId,
    "code"                      -> 0,
    "reason"                    -> "0 code"
  )

  val testIndividualPageConfig: PageConfig = PageConfig(
    optServiceName       = None,
    deskProServiceId     = testDeskProServiceId,
    signOutUrl           = testSignOutUrl,
    enableSautrCheck     = false,
    accessibilityUrl     = testAccessibilityUrl,
    optFullNamePageLabel = None,
    labels               = None
  )

  val testSoleTraderPageConfig: PageConfig = testIndividualPageConfig.copy(enableSautrCheck = true)

  val testIndividualJourneyConfig: JourneyConfig = JourneyConfig(
    continueUrl               = testContinueUrl,
    businessVerificationCheck = false,
    pageConfig                = testIndividualPageConfig,
    testRegime
  )

  val testIndividualJourneyConfigWithCallingService: JourneyConfig =
    testIndividualJourneyConfig
      .copy(pageConfig = testIndividualPageConfig.copy(optServiceName = Some(testServiceName)))

  val testSoleTraderJourneyConfig: JourneyConfig = testIndividualJourneyConfig
    .copy(businessVerificationCheck = true)
    .copy(pageConfig = testSoleTraderPageConfig)

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      fullName                        = testFullName,
      dateOfBirth                     = testDateOfBirth,
      optNino                         = Some(testNino),
      address                         = None,
      optSaPostcode                   = Some(testSaPostcode),
      optSautr                        = Some(testSautr),
      identifiersMatch                = SuccessfulMatch,
      businessVerification            = Some(BusinessVerificationPass),
      registrationStatus              = Some(Registered(testSafeId)),
      optTrn                          = None,
      optOverseasTaxIdentifier        = None,
      optOverseasTaxIdentifierCountry = None,
      optNinoInsights                 = Some(testInsightsReturnBody)
    )

  val testSoleTraderDetailsMismatch: SoleTraderDetails =
    SoleTraderDetails(
      fullName                        = testFullName,
      dateOfBirth                     = testDateOfBirth,
      optNino                         = Some(testNino),
      address                         = None,
      optSaPostcode                   = Some(testSaPostcode),
      optSautr                        = Some(testSautr),
      identifiersMatch                = DetailsMismatch,
      businessVerification            = Some(BusinessVerificationNotEnoughInformationToCallBV),
      registrationStatus              = Some(RegistrationNotCalled),
      optTrn                          = None,
      optOverseasTaxIdentifier        = None,
      optOverseasTaxIdentifierCountry = None,
      optNinoInsights                 = Some(testInsightsReturnBody)
    )

  val testSoleTraderDetailsNoBV: SoleTraderDetails = testSoleTraderDetails.copy(businessVerification = None)

  val testSoleTraderDetailsIndividualJourney: SoleTraderDetails =
    SoleTraderDetails(
      fullName                        = testFullName,
      dateOfBirth                     = testDateOfBirth,
      optNino                         = Some(testNino),
      address                         = None,
      optSaPostcode                   = None,
      optSautr                        = None,
      identifiersMatch                = SuccessfulMatch,
      businessVerification            = None,
      registrationStatus              = None,
      optTrn                          = None,
      optOverseasTaxIdentifier        = None,
      optOverseasTaxIdentifierCountry = None,
      optNinoInsights                 = Some(testInsightsReturnBody)
    )

  val testSoleTraderDetailsIndividualJourneyNoNino: SoleTraderDetails =
    SoleTraderDetails(
      fullName                        = testFullName,
      dateOfBirth                     = testDateOfBirth,
      optNino                         = None,
      address                         = None,
      optSaPostcode                   = None,
      optSautr                        = None,
      identifiersMatch                = DetailsMismatch,
      businessVerification            = None,
      registrationStatus              = None,
      optTrn                          = None,
      optOverseasTaxIdentifier        = None,
      optOverseasTaxIdentifierCountry = None,
      optNinoInsights                 = None
    )

  val testIndividualDetails: IndividualDetails =
    IndividualDetails(
      firstName   = testFirstName,
      lastName    = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino     = Some(testNino),
      optSautr    = Some(testSautr)
    )

  val testIndividualDetailsLowerCaseNino: IndividualDetails =
    IndividualDetails(
      firstName   = testFirstName,
      lastName    = testLastName,
      dateOfBirth = testDateOfBirth,
      optNino     = Some("aa111111a"),
      optSautr    = Some(testSautr)
    )

  val testIndividualDetailsLowerCaseFirstName: IndividualDetails = testIndividualDetails.copy(firstName = "john")

  val testIndividualDetailsLowerCaseLastName: IndividualDetails = testIndividualDetails.copy(lastName = "smith")

  val testIndividualDetailsNoSautr: IndividualDetails = testIndividualDetails.copy(optSautr = None)

  val testSoleTraderDetailsJson: JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> testFirstName,
        "lastName"  -> testLastName
      ),
      "dateOfBirth"          -> testDateOfBirth,
      "nino"                 -> testNino,
      "saPostcode"           -> testSaPostcode,
      "sautr"                -> testSautr,
      "identifiersMatch"     -> true,
      "businessVerification" -> testBusinessVerificationPassJson,
      "registration"         -> testSuccessfulRegistrationJson,
      "reputation"           -> testInsightsReturnBody
    )
  }

  def testSoleTraderDetailsJsonMisMatch(bvStatus: JsObject): JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> testFirstName,
        "lastName"  -> testLastName
      ),
      "dateOfBirth"          -> testDateOfBirth,
      "nino"                 -> testNino,
      "saPostcode"           -> testSaPostcode,
      "sautr"                -> testSautr,
      "identifiersMatch"     -> "DetailsMismatch",
      "businessVerification" -> bvStatus,
      "registration"         -> testRegistrationNotCalledJson
    )
  }

  def testSoleTraderDetailsJsonNoNinoKnownFactsNoContent(bvStatus: JsObject): JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> testFirstName,
        "lastName"  -> testLastName
      ),
      "dateOfBirth"            -> testDateOfBirth,
      "address"                -> testAddressJson,
      "sautr"                  -> testSautr,
      "saPostcode"             -> testSaPostcode,
      "overseasTaxIdentifiers" -> testOverseasTaxIdentifier,
      "country"                -> testOverseasTaxIdentifierCountry,
      "identifiersMatch"       -> KnownFactsNoContentKey,
      "businessVerification"   -> bvStatus,
      "registration"           -> testRegistrationNotCalledJson,
      "trn"                    -> testTrn
    )
  }

  val testSoleTraderDetailsJsonIndividualNoNino: JsObject = {
    Json.obj("fullName" -> Json.obj(
               "firstName" -> testFirstName,
               "lastName"  -> testLastName
             ),
             "dateOfBirth"      -> testDateOfBirth,
             "identifiersMatch" -> false
            )
  }

  val testIndividualDetailsJson: JsObject = {
    Json.obj("fullName" -> Json.obj(
               "firstName" -> testFirstName,
               "lastName"  -> testLastName
             ),
             "dateOfBirth" -> testDateOfBirth,
             "nino"        -> testNino,
             "sautr"       -> testSautr
            )
  }

  val testIndividualDetailsJsonNoSautr: JsObject = {
    Json.obj("fullName" -> Json.obj(
               "firstName" -> testFirstName,
               "lastName"  -> testLastName
             ),
             "dateOfBirth" -> testDateOfBirth,
             "nino"        -> testNino
            )
  }

  val testIndividualDetailsJsonNoNino: JsObject = {
    Json.obj("fullName" -> Json.obj(
               "firstName" -> testFirstName,
               "lastName"  -> testLastName
             ),
             "dateOfBirth" -> testDateOfBirth,
             "sautr"       -> testSautr
            )
  }

  val testIndividualDetailsJsonNoNinoNoSautr: JsObject = {
    Json.obj("fullName" -> Json.obj(
               "firstName" -> testFirstName,
               "lastName"  -> testLastName
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
            "key"   -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key"   -> "Postcode",
            "value" -> testSaPostcode
          )
        )
      )
    )
  )
  val testKnownFactsResponseNino: JsObject = testKnownFactsResponseNino(nino = testNino)

  def testKnownFactsResponseNino(nino: String): JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key"   -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key"   -> "NINO",
            "value" -> nino
          ),
          Json.obj(
            "key"   -> "Postcode",
            "value" -> testSaPostcode
          )
        )
      )
    )
  )

  def testKnownFactsResponseWithoutNino: JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key"   -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key"   -> "Postcode",
            "value" -> testSaPostcode
          )
        )
      )
    )
  )

  val testAddressJson: JsObject = Json.obj(
    "line1"       -> "line1",
    "line2"       -> "line2",
    "line3"       -> "line3",
    "line4"       -> "line4",
    "line5"       -> "line5",
    "postcode"    -> "AA1 1AA",
    "countryCode" -> "GB"
  )
  val testOverseasTaxIdentifiersJson: JsObject = Json.obj(
    "taxIdentifier" -> "134124532",
    "country"       -> "AL"
  )

  def testKnownFactsResponseIsAbroad(abroad: String = "Y"): JsObject = Json.obj(
    "service" -> "IR-SA",
    "enrolments" -> Json.arr(
      Json.obj(
        "identifiers" -> Json.arr(
          Json.obj(
            "key"   -> "UTR",
            "value" -> testSautr
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key"   -> "IsAbroad",
            "value" -> abroad
          )
        )
      )
    )
  )

  val signInRedirectUrl: (String, String) => String = (journeyId, currentPageUrl) =>
    "/bas-gateway/sign-in" +
      s"?continue_url=%2Fidentify-your-sole-trader-business%2F$journeyId%2F$currentPageUrl" +
      "&origin=sole-trader-identification-frontend"

}
