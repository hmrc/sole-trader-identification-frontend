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

package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.OptionValues
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, CheckYourAnswers => messages, Header}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper.checkYourAnswersFormat
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.jdk.CollectionConverters._

trait CheckYourAnswersViewTests extends OptionValues {
  this: ComponentSpecHelper =>

  def testCheckYourAnswersFullView(result: => WSResponse, journeyId: String): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have a back link" in {
      doc.getBackLinkText mustBe Base.back
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the UK Details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>
          val h2Elements: Elements = main.getH2Elements

          h2Elements.size mustBe 1
          h2Elements.first.text mustBe messages.ukDetails
        case None => fail("Element with id main-content cannot be found")
      }

    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 5 rows" in {
        summaryListRows.size mustBe 5
      }

      "have a first name row" in {
        val firstNameRow = summaryListRows.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe testFirstName
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = summaryListRows(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe testLastName
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = summaryListRows(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe testDateOfBirth.format(checkYourAnswersFormat)
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CaptureDateOfBirthController.show(journeyId).url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = summaryListRows(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe testNino.grouped(2).mkString(" ")
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show(journeyId).url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

      "have an sautr row" in {
        val sautrRow = summaryListRows.last

        sautrRow.getSummaryListQuestion mustBe messages.sautr
        sautrRow.getSummaryListAnswer mustBe testSautr
        sautrRow.getSummaryListChangeLink mustBe routes.CaptureSautrController.show(journeyId).url
        sautrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.sautr}"
      }

      "have a continue button" in {
        doc.getSubmitButton.text mustBe Base.continue
      }
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCheckYourAnswersNoSautrView(result: => WSResponse, journeyId: String): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the UK Details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>
          val h2Elements: Elements = main.getH2Elements

          h2Elements.size mustBe 1
          h2Elements.first.text mustBe messages.ukDetails
        case None => fail("Element with id main-content cannot be found")
      }

    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 4 rows" in {
        summaryListRows.size mustBe 4
      }

      "have a first name row" in {
        val firstNameRow = summaryListRows.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe testFirstName
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = summaryListRows(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe testLastName
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = summaryListRows(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe testDateOfBirth.format(checkYourAnswersFormat)
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CaptureDateOfBirthController.show(journeyId).url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = summaryListRows(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe testNino.grouped(2).mkString(" ")
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show(journeyId).url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

      "have a continue button" in {
        doc.getSubmitButton.text mustBe Base.continue
      }
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCheckYourAnswersNoNinoWithOverseasTaxIdentifierView(result: => WSResponse, journeyId: String): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a UK details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>
          val h2Elements: Elements = main.getH2Elements

          h2Elements.size mustBe 2
          h2Elements.first.text mustBe messages.ukDetails
        case None => fail("Element with id main-content cannot be found")
      }

    }

    "have a UK Details summary list which" should {
      lazy val summaryRowLists: List[Element] = doc.getSummaryRowLists.iterator.asScala.toList
      lazy val ukDetailsSummaryListRows: Option[List[Element]] = summaryRowLists.headOption.map(_.getSummaryListRows.iterator().asScala.toList)

        "be one of two summary row lists" in {
          summaryRowLists.size mustBe 2
        }

        "have 7 rows" in {
          ukDetailsSummaryListRows.value.size mustBe 7
        }

      "have a first name row" in {
        val firstNameRow = ukDetailsSummaryListRows.value.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe testFirstName
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = ukDetailsSummaryListRows.value(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe testLastName
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = ukDetailsSummaryListRows.value(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe testDateOfBirth.format(checkYourAnswersFormat)
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CaptureDateOfBirthController.show(journeyId).url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = ukDetailsSummaryListRows.value(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe messages.noNino
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show(journeyId).url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

      "have an address row" in {
        val addressRow = ukDetailsSummaryListRows.value(4)

        addressRow.getSummaryListQuestion mustBe messages.address
        addressRow.getSummaryListAnswer mustBe s"$testAddress1 $testAddress2 $testAddress3 $testAddress4 $testAddress5 $testPostcode $testCountryName"
        addressRow.getSummaryListChangeLink mustBe routes.CaptureAddressController.show(journeyId).url
        addressRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.address}"
      }

      "have an sautr row" in {
        val sautrRow = ukDetailsSummaryListRows.value(5)

        sautrRow.getSummaryListQuestion mustBe messages.sautr
        sautrRow.getSummaryListAnswer mustBe testSautr
        sautrRow.getSummaryListChangeLink mustBe routes.CaptureSautrController.show(journeyId).url
        sautrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.sautr}"
      }

      "have an sa postcode row" in {
        val saPostcodeRow = ukDetailsSummaryListRows.value(6)

        saPostcodeRow.getSummaryListQuestion mustBe messages.saPostcode
        saPostcodeRow.getSummaryListAnswer mustBe testSaPostcode
        saPostcodeRow.getSummaryListChangeLink mustBe routes.CaptureSaPostcodeController.show(journeyId).url
        saPostcodeRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.saPostcode}"
      }

    }

    "have an overseas details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>

          val h2Elements = main.getH2Elements

          h2Elements.size mustBe 2
          h2Elements.last.text mustBe messages.overseasDetails

        case None => fail("Element with id main-content cannot be found")
      }
    }

    "have an overseas details summary list which" should {
      lazy val summaryRowLists: List[Element] = doc.getSummaryRowLists.iterator.asScala.toList
      lazy val overseasDetailsSummaryListRows: Option[List[Element]] = summaryRowLists.lastOption.map(_.getSummaryListRows.iterator.asScala.toList)

      "have 2 rows" in {

        overseasDetailsSummaryListRows.value.size mustBe 2
      }

      "have an Overseas tax identifiers row" in {
        val taxIdentifierRow = overseasDetailsSummaryListRows.value.head

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifier
        taxIdentifierRow.getSummaryListAnswer mustBe s"Yes, $testOverseasTaxIdentifier"
        taxIdentifierRow.getSummaryListChangeLink mustBe routes.CaptureOverseasTaxIdentifierController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
      }

      "have an Overseas tax identifiers Country row" in {
        val taxIdentifierRow = overseasDetailsSummaryListRows.value.last

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifierCountry
        taxIdentifierRow.getSummaryListAnswer mustBe "Albania"
        taxIdentifierRow.getSummaryListChangeLink mustBe routes.CaptureOverseasTaxIdentifierCountryController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifierCountry}"
      }

    }

    "have a continue button" in {
      doc.getSubmitButton.text mustBe Base.continue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCheckYourAnswersNoNinoWithoutOverseasTaxIdentifierView(result: => WSResponse, journeyId: String): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a UK details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>
          val h2Elements: Elements = main.getH2Elements

          h2Elements.size mustBe 2
          h2Elements.first.text mustBe messages.ukDetails
        case None => fail("Element with id main-content cannot be found")
      }

    }

    "have a UK details summary list which" should {
      lazy val summaryRowLists: List[Element] = doc.getSummaryRowLists.iterator.asScala.toList
      lazy val ukDetailsSummaryListRows: Option[List[Element]] = summaryRowLists.headOption.map(_.getSummaryListRows.iterator().asScala.toList)

      "have two summary lists" in {
        summaryRowLists.size mustBe 2
      }

      "have 7 rows" in {
        ukDetailsSummaryListRows.value.size mustBe 7
      }

      "have a first name row" in {
        val firstNameRow = ukDetailsSummaryListRows.value.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe testFirstName
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = ukDetailsSummaryListRows.value(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe testLastName
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = ukDetailsSummaryListRows.value(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe testDateOfBirth.format(checkYourAnswersFormat)
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CaptureDateOfBirthController.show(journeyId).url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = ukDetailsSummaryListRows.value(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe messages.noNino
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show(journeyId).url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

      "have an address row" in {
        val addressRow = ukDetailsSummaryListRows.value(4)

        addressRow.getSummaryListQuestion mustBe messages.address
        addressRow.getSummaryListAnswer mustBe s"$testAddress1 $testAddress2 $testAddress3 $testAddress4 $testAddress5 $testPostcode $testCountryName"
        addressRow.getSummaryListChangeLink mustBe routes.CaptureAddressController.show(journeyId).url
        addressRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.address}"
      }

      "have an sautr row" in {
        val sautrRow = ukDetailsSummaryListRows.value(5)

        sautrRow.getSummaryListQuestion mustBe messages.sautr
        sautrRow.getSummaryListAnswer mustBe testSautr
        sautrRow.getSummaryListChangeLink mustBe routes.CaptureSautrController.show(journeyId).url
        sautrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.sautr}"
      }

      "have an sa postcode row" in {
        val saPostcodeRow = ukDetailsSummaryListRows.value(6)

        saPostcodeRow.getSummaryListQuestion mustBe messages.saPostcode
        saPostcodeRow.getSummaryListAnswer mustBe testSaPostcode
        saPostcodeRow.getSummaryListChangeLink mustBe routes.CaptureSaPostcodeController.show(journeyId).url
        saPostcodeRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.saPostcode}"
      }

    }

    "have an overseas details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>

          val h2Elements = main.getH2Elements

          h2Elements.size mustBe 2
          h2Elements.last.text mustBe messages.overseasDetails

        case None => fail("Element with id main-content cannot be found")
      }
    }

    "have an overseas details summary list which" should {
      lazy val summaryRowLists: List[Element] = doc.getSummaryRowLists.iterator.asScala.toList
      lazy val overseasDetailsSummaryListRows: Option[List[Element]] = summaryRowLists.lastOption.map(_.getSummaryListRows.iterator.asScala.toList)

      "have 1 row" in {

        overseasDetailsSummaryListRows.value.size mustBe 1
      }

      "have an Overseas tax identifers row" in {
        val taxIdentifierRow = overseasDetailsSummaryListRows.value.last

        taxIdentifierRow.getSummaryListQuestion mustBe messages.overseasTaxIdentifier
        taxIdentifierRow.getSummaryListAnswer mustBe Base.no
        taxIdentifierRow.getSummaryListChangeLink mustBe routes.CaptureOverseasTaxIdentifierController.show(journeyId).url
        taxIdentifierRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.overseasTaxIdentifier}"
      }

    }

    "have a continue button" in {
      doc.getSubmitButton.text mustBe Base.continue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }

  def testCheckYourAnswersNoNinoIndividualFlowView(result: => WSResponse, journeyId: String): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a UK details sub-header" in {

      doc.getNullableElementById("main-content") match {
        case Some(main) =>
          val h2Elements: Elements = main.getH2Elements

          h2Elements.size mustBe 1
          h2Elements.first.text mustBe messages.ukDetails
        case None => fail("Element with id main-content cannot be found")
      }

    }

    "have a UK details summary list which" should {
      lazy val summaryRowLists: List[Element] = doc.getSummaryRowLists.iterator.asScala.toList
      lazy val ukDetailsSummaryListRows: Option[List[Element]] = summaryRowLists.headOption.map(_.getSummaryListRows.iterator().asScala.toList)

      "be the only summary list displayed" in {
        summaryRowLists.size mustBe 1
      }

      "have 4 rows" in {
        ukDetailsSummaryListRows.value.size mustBe 4
      }

      "have a first name row" in {
        val firstNameRow = ukDetailsSummaryListRows.value.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe testFirstName
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = ukDetailsSummaryListRows.value(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe testLastName
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureFullNameController.show(journeyId).url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = ukDetailsSummaryListRows.value(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe testDateOfBirth.format(checkYourAnswersFormat)
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CaptureDateOfBirthController.show(journeyId).url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = ukDetailsSummaryListRows.value(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe messages.noNino
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show(journeyId).url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

    }

    "have a continue button" in {
      doc.getSubmitButton.text mustBe Base.continue
    }

    "have the correct technical help link and text" in {
      doc.getTechnicalHelpLinkText mustBe Base.getHelp
      doc.getTechnicalHelpLink mustBe testTechnicalHelpUrl
    }
  }
}
