
package uk.gov.hmrc.soletraderidentificationfrontend.assets

import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsModel

import java.time.LocalDate

case object TestConstants {

  val testJourneyId: String = "testJourneyId"
  val testDateOfBirth: LocalDate = LocalDate.now()
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testNino: String = "AA111111A"
  val testSautr: String = "1234567890"

  val testSoleTraderDetails: SoleTraderDetailsModel =
    SoleTraderDetailsModel(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      Some(testSautr)
    )

}
