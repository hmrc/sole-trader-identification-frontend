/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import play.api.libs.json.JsObject
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.SoleTraderIdentificationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{SoleTraderDetailsMatchFailure, SoleTraderDetailsMatchResult}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SoleTraderIdentificationService @Inject() (connector: SoleTraderIdentificationConnector) {

  def storeFullName(journeyId: String, fullName: FullName)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[FullName](journeyId, FullNameKey, fullName)

  def storeDateOfBirth(journeyId: String, dateOfBirth: LocalDate)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[LocalDate](journeyId, DateOfBirthKey, dateOfBirth)

  def storeNino(journeyId: String, nino: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, NinoKey, nino)

  def storeAddress(journeyId: String, address: Address)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[Address](journeyId, AddressKey, address)

  def storeSautr(journeyId: String, sautr: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, SautrKey, sautr)

  def storeSaPostcode(journeyId: String, saPostcode: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, SaPostcodeKey, saPostcode)

  def storeIdentifiersMatch(journeyId: String, identifiersMatch: SoleTraderDetailsMatchResult)(implicit
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] =
    connector.storeData[SoleTraderDetailsMatchResult](journeyId, IdentifiersMatchKey, identifiersMatch)

  def storeAuthenticatorFailureResponse(journeyId: String, authenticatorFailureResponse: SoleTraderDetailsMatchFailure)(implicit
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, AuthenticatorFailureResponseKey, authenticatorFailureResponse.toString)

  def storeAuthenticatorDetails(journeyId: String, authenticatorDetails: IndividualDetails)(implicit
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] =
    connector.storeData[IndividualDetails](journeyId, AuthenticatorDetailsKey, authenticatorDetails)

  def storeES20Details(journeyId: String, es20Details: KnownFactsResponse)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[KnownFactsResponse](journeyId, Es20DetailsKey, es20Details)

  def storeBusinessVerificationStatus(journeyId: String, businessVerification: BusinessVerificationStatus)(implicit
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] =
    connector.storeData[BusinessVerificationStatus](journeyId, VerificationStatusKey, businessVerification)

  def storeRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)(implicit
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] =
    connector.storeData[RegistrationStatus](journeyId, RegistrationKey, registrationStatus)

  def storeTrn(journeyId: String, trn: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, TrnKey, trn)

  def storeOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData(journeyId, OverseasTaxIdentifierKey, overseasTaxIdentifier)

  def storeOverseasTaxIdentifiersCountry(journeyId: String, country: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, OverseasCountryKey, country)

  def storeInsights(journeyId: String, insights: JsObject)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[JsObject](journeyId, InsightsKey, insights)

  def retrieveInsights(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[JsObject]] =
    connector.retrieveSoleTraderDetails[JsObject](journeyId, InsightsKey)

  def retrieveFullName(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[FullName]] =
    connector.retrieveSoleTraderDetails[FullName](journeyId, FullNameKey)

  def retrieveDateOfBirth(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    connector.retrieveSoleTraderDetails[LocalDate](journeyId, DateOfBirthKey)

  def retrieveNino(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, NinoKey)

  def retrieveSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, SautrKey)

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetails]] =
    connector.retrieveSoleTraderDetails(journeyId)

  def retrieveIndividualDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IndividualDetails]] =
    connector.retrieveIndividualDetails(journeyId)

  def retrieveBusinessVerificationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrieveSoleTraderDetails[BusinessVerificationStatus](journeyId, VerificationStatusKey)

  def retrieveIdentifiersMatch(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetailsMatchResult]] =
    connector.retrieveSoleTraderDetails[SoleTraderDetailsMatchResult](journeyId, IdentifiersMatchKey)

  def retrieveAuthenticatorDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IndividualDetails]] =
    connector.retrieveSoleTraderDetails[IndividualDetails](journeyId, AuthenticatorDetailsKey)

  def retrieveRegistrationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[RegistrationStatus]] =
    connector.retrieveSoleTraderDetails[RegistrationStatus](journeyId, RegistrationKey)

  def retrieveAuthenticatorFailureResponse(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, AuthenticatorFailureResponseKey)

  def retrieveAddress(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Address]] =
    connector.retrieveSoleTraderDetails[Address](journeyId, AddressKey)

  def retrieveSaPostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, SaPostcodeKey)

  def retrieveOverseasTaxIdentifier(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, OverseasTaxIdentifierKey)

  def retrieveOverseasTaxIdentifierCountry(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, OverseasCountryKey)

  def retrieveES20Details(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[KnownFactsResponse]] =
    connector.retrieveSoleTraderDetails[KnownFactsResponse](journeyId, Es20DetailsKey)

  def retrieveTrn(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderDetails[String](journeyId, TrnKey)

  def removeSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, SautrKey)

  def removeNino(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, NinoKey)

  def removeSaPostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, SaPostcodeKey)

  def removeAddress(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, AddressKey)

  def removeOverseasTaxIdentifier(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, OverseasTaxIdentifierKey)

  def removeInsights(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, InsightsKey)

  def removeOverseasTaxIdentifierCountry(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderDetails(journeyId, OverseasCountryKey)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeAllData(journeyId)
}

object SoleTraderIdentificationService {
  val FullNameKey = "fullName"
  val NinoKey = "nino"
  val SautrKey = "sautr"
  val DateOfBirthKey = "dateOfBirth"
  val IdentifiersMatchKey: String = "identifiersMatch"
  val AuthenticatorDetailsKey: String = "authenticatorDetails"
  val AuthenticatorFailureResponseKey: String = "authenticatorFailureResponse"
  val VerificationStatusKey = "businessVerification"
  val RegistrationKey: String = "registration"
  val AddressKey: String = "address"
  val TrnKey: String = "trn"
  val SaPostcodeKey: String = "saPostcode"
  val Es20DetailsKey: String = "es20Details"
  val InsightsKey: String = "reputation"
  val OverseasTaxIdentifierKey: String = "overseasTaxIdentifiers"
  val OverseasCountryKey: String = "country"
}
