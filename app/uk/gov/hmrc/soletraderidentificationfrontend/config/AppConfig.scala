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

package uk.gov.hmrc.soletraderidentificationfrontend.config

import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{AuthenticatorStub, BusinessVerificationStub, FeatureSwitching, KnownFactsStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.Country

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig, environment: Environment) extends FeatureSwitching {

  def matchSoleTraderDetailsUrl: String =
    if (isEnabled(AuthenticatorStub)) {
      s"${servicesConfig.baseUrl("self")}/identify-your-sole-trader-business/test-only/authenticator/match"
    } else {
      s"${servicesConfig.baseUrl("authenticator")}/authenticator/match"
    }

  private lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  private val assetsUrl = servicesConfig.getString("assets.url")

  private lazy val backendUrl: String = servicesConfig.baseUrl("sole-trader-identification")

  val assetsPrefix: String = assetsUrl + servicesConfig.getString("assets.version")
  val analyticsToken: String = servicesConfig.getString(s"google-analytics.token")
  val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")

  def reportAProblemPartialUrl(serviceIdentifier: String): String =
    s"$contactHost/contact/problem_reports_ajax?service=$serviceIdentifier"

  def reportAProblemNonJSUrl(serviceIdentifier: String): String =
    s"$contactHost/contact/problem_reports_nonjs?service=$serviceIdentifier"

  lazy val soleTraderIdentificationFrontendBaseUrl: String = servicesConfig.baseUrl("sole-trader-identification-frontend")

  def soleTraderIdentificationUrl(journeyId: String): String = s"$backendUrl/sole-trader-identification/journey/$journeyId"

  lazy val createJourneyUrl: String = s"$backendUrl/sole-trader-identification/journey"

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  lazy val timeToLiveSeconds: Long = servicesConfig.getString("mongodb.timeToLiveSeconds").toLong

  lazy val vatRegExitSurveyOrigin = "vat-registration"
  private lazy val feedbackUrl: String = servicesConfig.getString("feedback.host")
  lazy val vatRegFeedbackUrl = s"$feedbackUrl/feedback/$vatRegExitSurveyOrigin"

  def betaFeedbackUrl(serviceIdentifier: String): String = s"$contactHost/contact/beta-feedback?service=$serviceIdentifier"

  lazy val defaultServiceName: String = servicesConfig.getString("defaultServiceName")

  lazy val businessVerificationFrontendBaseUrl: String = servicesConfig.baseUrl("business-verification-frontend")

  private lazy val businessVerificationUrl = servicesConfig.getString("microservice.services.business-verification.url")

  def createBusinessVerificationJourneyUrl: String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-sole-trader-business/test-only/business-verification/journey"
    else
      s"$businessVerificationUrl/journey"
  }

  def getBusinessVerificationResultUrl(journeyId: String): String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-sole-trader-business/test-only/business-verification/journey/$journeyId/status"
    else
      s"$businessVerificationUrl/journey/$journeyId/status"
  }

  def registerUrl: String = s"$backendUrl/sole-trader-identification/register"

  def registerWithTrnUrl: String = s"$backendUrl/sole-trader-identification/register-trn"

  def ninoTeamUrl: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/national-insurance-enquiries-for-employees-and-individuals"

  def createTrnUrl: String = s"$backendUrl/sole-trader-identification/get-trn"

  lazy val enrolmentStoreProxyUrl: String = servicesConfig.baseUrl("enrolment-store-proxy") + "/enrolment-store-proxy"

  def knownFactsUrl: String = {
    val baseUrl: String = if (isEnabled(KnownFactsStub)) s"$selfBaseUrl/identify-your-sole-trader-business/test-only" else enrolmentStoreProxyUrl
    baseUrl + "/enrolment-store/enrolments"
  }

  lazy val countries: Map[String, Country] = {
    environment.resourceAsStream("/countries.json") match {
      case Some(countriesStream) =>
        Json.parse(countriesStream).as[Map[String, Country]]
      case None =>
        throw new InternalServerException("Country list missing")
    }
  }

  lazy val orderedCountryList: Seq[Country] = countries.values.toSeq.sortBy(_.name)

  def getCountryName(countryCode: String): String = countries.get(countryCode) match {
    case Some(Country(_, name)) =>
      name
    case None =>
      throw new InternalServerException("Invalid country code")

  }

}

