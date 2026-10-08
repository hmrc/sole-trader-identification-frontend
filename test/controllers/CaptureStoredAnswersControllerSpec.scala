/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import helpers.TestConstants._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.mocks.{MockJourneyService, MockSoleTraderIdentificationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.soletraderidentificationfrontend.controllers._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, PageConfig}

import scala.concurrent.Future

class CaptureStoredAnswersControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with MockJourneyService
    with MockSoleTraderIdentificationService {

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private val pageConfig = PageConfig(
    optServiceName       = Some(testServiceName),
    deskProServiceId     = "test-service-id",
    signOutUrl           = testSignOutUrl,
    enableSautrCheck     = true,
    accessibilityUrl     = testAccessibilityUrl,
    optFullNamePageLabel = None,
    labels               = None
  )

  private val journeyConfig = JourneyConfig(
    continueUrl               = testContinueUrl,
    businessVerificationCheck = false,
    pageConfig                = pageConfig,
    regime                    = testRegime
  )

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.enabled"                       -> false,
        "metrics.jvm"                           -> false,
        "microservice.metrics.graphite.enabled" -> false
      )
      .overrides(
        bind[AuthConnector].toInstance(mockAuthConnector),
        bind[uk.gov.hmrc.soletraderidentificationfrontend.services.JourneyService].toInstance(mockJourneyService),
        bind[uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService].toInstance(mockSoleTraderIdentificationService)
      )
      .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
    when(
      mockAuthConnector.authorise[Option[String]](any(), any[Retrieval[Option[String]]]())(any(), any())
    ).thenReturn(Future.successful(Some(testInternalId)))
  }

  private def documentOf(result: Future[play.api.mvc.Result]) =
    Jsoup.parse(contentAsString(result))

  "CaptureFullNameController.show" should {
    "pre-populate the stored full name" in {
      mockGetJourneyConfig(testJourneyId, testInternalId)(Future.successful(journeyConfig))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))

      val controller = app.injector.instanceOf[CaptureFullNameController]
      val result = controller.show(testJourneyId)(FakeRequest())
      val doc = documentOf(result)

      status(result) mustBe OK
      doc.getElementById("first-name").`val`() mustBe testFirstName
      doc.getElementById("last-name").`val`() mustBe testLastName
    }
  }

  "CaptureDateOfBirthController.show" should {
    "pre-populate the stored date of birth" in {
      mockGetJourneyConfig(testJourneyId, testInternalId)(Future.successful(journeyConfig))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))

      val controller = app.injector.instanceOf[CaptureDateOfBirthController]
      val result = controller.show(testJourneyId)(FakeRequest())
      val doc = documentOf(result)

      status(result) mustBe OK
      doc.getElementById("date-of-birth-day").`val`() mustBe testDateOfBirth.getDayOfMonth.toString
      doc.getElementById("date-of-birth-month").`val`() mustBe testDateOfBirth.getMonth.toString
      doc.getElementById("date-of-birth-year").`val`() mustBe testDateOfBirth.getYear.toString
    }
  }

  "CaptureNinoController.show" should {
    "pre-populate the stored nino" in {
      mockGetJourneyConfig(testJourneyId, testInternalId)(Future.successful(journeyConfig))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))

      val controller = app.injector.instanceOf[CaptureNinoController]
      val result = controller.show(testJourneyId)(FakeRequest())
      val doc = documentOf(result)

      status(result) mustBe OK
      doc.getElementById("nino").`val`() mustBe testNino
    }
  }

  "CaptureAddressController.show" should {
    "pre-populate the stored address" in {
      mockGetJourneyConfig(testJourneyId, testInternalId)(Future.successful(journeyConfig))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveAddress(testJourneyId)(Future.successful(Some(testAddress)))

      val controller = app.injector.instanceOf[CaptureAddressController]
      val result = controller.show(testJourneyId)(FakeRequest())
      val doc = documentOf(result)

      status(result) mustBe OK
      doc.getElementById("address1").`val`() mustBe testAddress.line1
      doc.getElementById("address2").`val`() mustBe testAddress.line2
      doc.getElementById("postcode").`val`() mustBe testAddress.postcode.get
      doc.select("option[selected]").eachAttr("value") must contain(testAddress.countryCode)
    }
  }

  "CaptureSautrNewController.show" should {
    "pre-populate the stored sautr selection and value" in {
      mockGetJourneyConfig(testJourneyId, testInternalId)(Future.successful(journeyConfig))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))

      val controller = app.injector.instanceOf[CaptureSautrNewController]
      val result = controller.show(testJourneyId)(FakeRequest())
      val doc = documentOf(result)

      status(result) mustBe OK
      doc.getElementById("optSautr").hasAttr("checked") mustBe true
      doc.getElementById("sa-utr").`val`() mustBe testSautr
    }
  }
}
