
package uk.gov.hmrc.soletraderidentificationfrontend.stubs

import java.time.LocalDate

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsString, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullNameModel
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

trait SoleTraderIdentificationStub extends WireMockMethods {

  def stubStoreFullName(journeyId: String, fullName: FullNameModel)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/$journeyId/full-name",
      body = Json.obj(
        "firstName" -> fullName.firstName,
        "lastName" -> fullName.lastName
      )
    ).thenReturn(
      status = status
    )

  def stubStoreNino(journeyId: String, nino: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/$journeyId/national-insurance-number",
      body = JsString(nino)
    ).thenReturn(
      status = status
    )

  def stubStoreSautr(journeyId: String, sautr: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/$journeyId/sa-utr", body = JsString(sautr)
    ).thenReturn(
      status = status
    )

  def stubStoreDob(journeyId: String, dateOfBirth: LocalDate)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/$journeyId/date-of-birth", body = dateOfBirth
    ).thenReturn(
      status = status
    )


  def stubRetrieveSoleTraderDetails(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveFullName(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/$journeyId/full-name"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveDob(journeyId: String)(status: Int, body: LocalDate = LocalDate.now()): StubMapping = {
    when(method = GET,
      uri = s"/sole-trader-identification/$journeyId/date-of-birth"
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def stubRetrieveNino(journeyId: String)(status: Int, body: String = ""): StubMapping = {
    when(method = GET,
      uri = s"/sole-trader-identification/$journeyId/national-insurance-number"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )
  }

  def stubRetrieveSautr(journeyId: String)(status: Int, body: String = ""): StubMapping = {
    when(method = GET,
      uri = s"/sole-trader-identification/$journeyId/sa-utr"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )
  }
}