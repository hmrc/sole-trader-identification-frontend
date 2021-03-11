
package uk.gov.hmrc.soletraderidentificationfrontend.connectors


import java.time.LocalDate

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.{NOT_FOUND, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.SoleTraderIdentificationStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class SoleTraderIdentificationConnectorISpec extends ComponentSpecHelper with SoleTraderIdentificationStub {

  private val soleTraderIdentificationConnector = app.injector.instanceOf[SoleTraderIdentificationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val dateOfBirthKey = "date-of-birth"
  val fullNameKey = "full-name"
  val ninoKey = "national-insurance-number"
  val sautrKey = "sa-utr"


  s"retrieveSoleTraderIdentification($testJourneyId)" should {
    "return Sole Trader Identification" when {
      "there is Sole Trader Identification stored against the journeyId" in {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            SoleTraderDetailsModel(
              firstName = testFirstName,
              lastName = testLastName,
              dateOfBirth = testDateOfBirth,
              nino = testNino,
              optSautr = Some(testSautr)
            )
          )
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification(testJourneyId))

        result mustBe Some(
          SoleTraderDetailsModel(
            testFirstName,
            testLastName,
            testDateOfBirth,
            testNino,
            Some(testSautr)
          ))
      }
    }
    "return None" when {
      "there is no Sole Trader Identification stored against the journeyId" in {
        stubRetrieveSoleTraderDetails(testJourneyId)(
          status = NOT_FOUND
        )

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification(testJourneyId))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $fullNameKey)" should {
    "return full name" when {
      "the full name key is given and a full name is stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(OK, Json.toJsObject(FullNameModel(testFirstName, testLastName)))
        val testJson = Json.obj(
          "firstName" -> testFirstName,
          "lastName" -> testLastName
        )
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsObject](testJourneyId, fullNameKey))

        result mustBe Some(testJson)
      }
    }

    "return None" when {
      "the firstName key is given but there is no first name stored against the journeyId" in {
        stubRetrieveFullName(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, fullNameKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $dateOfBirthKey)" should {
    "return date of birth" when {
      "the date of birth key is given and date of birth is stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(OK, testDateOfBirth)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[LocalDate](testJourneyId, dateOfBirthKey))

        result mustBe Some(testDateOfBirth)
      }
    }

    "return None" when {
      "the date of birth key is given but there is no date of birth stored against the journeyId" in {
        stubRetrieveDob(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[LocalDate](testJourneyId, dateOfBirthKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $ninoKey)" should {
    "return nino" when {
      "the nino key is given and nino is stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(OK, testNino)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, ninoKey))

        result mustBe Some(JsString(testNino))
      }
    }

    "return None" when {
      "the nino key is given but there is no nino stored against the journeyId" in {
        stubRetrieveNino(testJourneyId)(NOT_FOUND)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, ninoKey))

        result mustBe None
      }
    }
  }

  s"retrieveSoleTraderIdentification($testJourneyId, $sautrKey)" should {
    "return sautr" when {
      "the sautr key is given and sautr is stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, sautrKey))

        result mustBe Some(JsString(testSautr))
      }
    }

    "return None" when {
      "the sautr key is given but there is no sautr stored against the journeyId" in {
        stubRetrieveSautr(testJourneyId)(NOT_FOUND)

        val result = await(soleTraderIdentificationConnector.retrieveSoleTraderIdentification[JsString](testJourneyId, sautrKey))

        result mustBe None
      }
    }
  }

  s"storeData($testJourneyId, $fullNameKey)" should {
    "return SuccessfullyStored" in {
      stubStoreFullName(testJourneyId, FullNameModel(testFirstName, testLastName))(status = OK)
      val result = await(soleTraderIdentificationConnector.storeData[FullNameModel](
        testJourneyId, fullNameKey, FullNameModel(testFirstName, testLastName)))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $dateOfBirthKey)" should {
    "return SuccessfullyStored" in {
      stubStoreDob(testJourneyId, testDateOfBirth)(status = OK)

      val result = await(soleTraderIdentificationConnector.storeData[LocalDate](testJourneyId, dateOfBirthKey, testDateOfBirth))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $ninoKey)" should {
    "return SuccessfullyStored" in {
      stubStoreNino(testJourneyId, testNino)(status = OK)

      val result = await(soleTraderIdentificationConnector.storeData[String](testJourneyId, ninoKey, testNino))

      result mustBe SuccessfullyStored
    }
  }

  s"storeData($testJourneyId, $sautrKey)" should {
    "return SuccessfullyStored" in {
      stubStoreSautr(testJourneyId, testSautr)(status = OK)
      val result = await(soleTraderIdentificationConnector.storeData[String](testJourneyId, sautrKey, testSautr))

      result mustBe SuccessfullyStored
    }
  }
}