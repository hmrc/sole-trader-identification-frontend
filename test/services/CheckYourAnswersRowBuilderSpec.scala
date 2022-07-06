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

package services

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.services.CheckYourAnswersRowBuilder
import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper.checkYourAnswersFormat

class CheckYourAnswersRowBuilderSpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {

  object TestService extends CheckYourAnswersRowBuilder()

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit val mockMessages: Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val testFirstNameRow = SummaryListRow(
    key = Key(content = Text("First name")),
    value = Value(content = HtmlContent(testFirstName)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureFullNameController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("First name")
      )
    )))
  )

  val testLastNameRow = SummaryListRow(
    key = Key(content = Text("Last name")),
    value = Value(content = HtmlContent(testLastName)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureFullNameController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Last name")
      )
    )))
  )

  val testDateOfBirthRow = SummaryListRow(
    key = Key(content = Text("Date of birth")),
    value = Value(content = HtmlContent(testDateOfBirth.format(checkYourAnswersFormat))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureDateOfBirthController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Date of birth")
      )
    )))
  )

  val testNinoRow = SummaryListRow(
    key = Key(content = Text("National Insurance number")),
    value = Value(content = HtmlContent(testNino.grouped(2).mkString(" "))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureNinoController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("National Insurance number")
      )
    )))
  )

  val testNoNinoRow = SummaryListRow(
    key = Key(content = Text("National Insurance number")),
    value = Value(content = HtmlContent("There is no National Insurance number")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureNinoController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("National Insurance number")
      )
    )))
  )

  val testSautrRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(content = HtmlContent(testSautr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSautrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testNoSautrRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(content = HtmlContent("There is no UTR")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSautrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testSaPostcodeRow = SummaryListRow(
    key = Key(content = Text("Self Assessment postcode")),
    value = Value(content = HtmlContent(testSaPostcode)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSaPostcodeController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Self Assessment postcode")
      )
    )))
  )

  val testNoSaPostcodeRow = SummaryListRow(
    key = Key(content = Text("Self Assessment postcode")),
    value = Value(content = HtmlContent("The business does not have a Self Assessment postcode")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSaPostcodeController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Self Assessment postcode")
      )
    )))
  )
  val formatOverseasTaxIdentifiers: String = Seq(
    Some(testOverseasIdentifiers.taxIdentifier),
    Some(mockAppConfig.getCountryName(testOverseasIdentifiers.country))
  ).flatten.mkString("<br>")

  val testOverseasTaxIdentifiersRow = SummaryListRow(
    key = Key(content = Text("Overseas tax identifier")),
    value = Value(content = HtmlContent(formatOverseasTaxIdentifiers)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureOverseasTaxIdentifiersController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Overseas tax identifier")
      )
    )))
  )

  val formattedAddress: String = Seq(
    Some(testAddress.line1),
    Some(testAddress.line2),
    testAddress.line3,
    testAddress.line4,
    testAddress.line5,
    testAddress.postcode,
    Some(mockAppConfig.getCountryName(testAddress.countryCode))
  ).flatten.mkString("<br>")

  val testAddressRow = SummaryListRow(
    key = Key(content = Text("Home Address")),
    value = Value(content = HtmlContent(formattedAddress)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureAddressController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Home Address")
      )
    )))
  )

  "buildSummaryListRowSeq" should {
    "build a summary list sequence" when {
      "the user is on the individual journey" when {
        "there is a nino" in {
          val result = TestService.buildSummaryListRows(testJourneyId, testIndividualDetailsNoSautr, optAddress = None, optSaPostcode = None, optOverseasTaxId = None, enableSautrCheck = false)

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNinoRow)
        }
      }

      "the user is on the sole trader journey" when {
        "there is a nino and sautr but no address provided" in {
          val result = TestService.buildSummaryListRows(testJourneyId, testIndividualDetails, optAddress = None, optSaPostcode = None, optOverseasTaxId = None, enableSautrCheck = true)

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNinoRow, testSautrRow)
        }

        "the nino has not been provided but sautr, sa postcode and address have" in {
          val result = TestService.buildSummaryListRows(testJourneyId, testIndividualDetailsNoNino, Some(testAddress), optSaPostcode = Some(testSaPostcode), optOverseasTaxId = Some(testOverseasIdentifiers), enableSautrCheck = true)

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNoNinoRow, testAddressRow, testSautrRow, testSaPostcodeRow, testOverseasTaxIdentifiersRow)
        }

        "the nino and sa postcode have not been provided but sautr, and address have" in {
          val result = TestService.buildSummaryListRows(testJourneyId, testIndividualDetailsNoNino, Some(testAddress), optSaPostcode = None, optOverseasTaxId = Some(testOverseasIdentifiers), enableSautrCheck = true)

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNoNinoRow, testAddressRow, testSautrRow, testNoSaPostcodeRow, testOverseasTaxIdentifiersRow)
        }

        "the nino and sautr have not been provided but an address has" in {
          val result = TestService.buildSummaryListRows(testJourneyId, testIndividualDetailsNoNinoNoSautr, Some(testAddress), optSaPostcode = None, optOverseasTaxId = Some(testOverseasIdentifiers), enableSautrCheck = true)

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNoNinoRow, testAddressRow, testNoSautrRow, testOverseasTaxIdentifiersRow)
        }
      }
    }
  }

}
