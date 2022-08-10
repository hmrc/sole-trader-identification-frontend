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

import play.api.{Configuration, Environment}
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{AuthenticatorStub, BusinessVerificationStub, FeatureSwitching, KnownFactsStub}
import uk.gov.hmrc.soletraderidentificationfrontend.models.Country

import java.io.IOException
import javax.inject.{Inject, Singleton}
import org.apache.commons.io.IOUtils
import scala.collection.JavaConverters.asScalaBufferConverter

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig, environment: Environment) extends FeatureSwitching {

  val allowedHosts: Set[String] = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet

  def matchSoleTraderDetailsUrl: String =
    if (isEnabled(AuthenticatorStub)) {
      s"${servicesConfig.baseUrl("self")}/identify-your-sole-trader-business/test-only/authenticator/match"
    } else {
      s"${servicesConfig.baseUrl("authenticator")}/authenticator/match"
    }

  lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  private val assetsUrl = servicesConfig.getString("assets.url")

  private lazy val backendUrl: String = servicesConfig.baseUrl("sole-trader-identification")

  val assetsPrefix: String = assetsUrl + servicesConfig.getString("assets.version")
  val analyticsToken: String = servicesConfig.getString(s"google-analytics.token")
  val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")

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

  private lazy val countriesListInEnglish: Map[String, Country] = getCountryList("/countries.json")

  private lazy val countriesListInWelsh: Map[String, Country] = getCountryList("/countries_cy.json")

  def getCountryListByLanguage(code: String = "en"): Map[String, Country] = if(code == "cy") countriesListInWelsh else countriesListInEnglish

  private lazy val orderedCountryListInEnglish: Seq[Country] = countriesListInEnglish.values.toSeq.sortBy(_.name)

  private lazy val orderedCountryListInWelsh: Seq[Country] =  countriesListInWelsh.values.toSeq.sortBy(_.name)

  def getOrderedCountryListByLanguage(code: String = "en"): Seq[Country] = if(code == "cy") orderedCountryListInWelsh else orderedCountryListInEnglish

  def getCountryName(countryCode: String, langCode: String = "en"): String = getCountryListByLanguage(langCode).get(countryCode) match {
    case Some(Country(_, name)) =>
      name
    case None =>
      throw new InternalServerException("Invalid country code")
  }

  def getCountryList(fileName: String) : Map[String, Country] = {

    environment.resourceAsStream(fileName) match {
      case Some(countriesStream) =>
        try {
          Json.parse(countriesStream).as[Map[String, Country]]
        } finally {
          try {
            IOUtils.close(countriesStream)
          } catch {
            case ex : IOException =>
              throw new InternalServerException(s"I/O exception raised on closing file $fileName : ${ex.getMessage}")
          }
        }
      case None => throw new InternalServerException(s"Country list file $fileName cannot be found")
    }
  }

  lazy val insightsUrl: String = s"$backendUrl/sole-trader-identification/nino-insights"

}

