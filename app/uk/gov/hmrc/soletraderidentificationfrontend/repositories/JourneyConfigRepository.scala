/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.repositories

import play.api.libs.json._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.JourneyConfigRepository._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.TimeUnit

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "sole-trader-identification-frontend",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds)),
  extraCodecs = Seq(Codecs.playFormatCodec(journeyConfigMongoFormat))
) {

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[InsertOneResult] = {

    val document: JsObject = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def findJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] = {

    collection.find[JourneyConfig](
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).headOption

  }

  def count: Future[Long] = collection.countDocuments().toFuture()

  def removeJourneyConfig(journeyId: String, authInternalId: String): Future[DeleteResult] = {

    collection.deleteOne(
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).toFuture()

  }

  def drop: Future[Unit] = collection.drop().toFuture.map(_ => Unit)

}


object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"

  implicit val journeyConfigMongoFormat: OFormat[JourneyConfig] = Json.format[JourneyConfig]

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel = {
    IndexModel(
      keys = ascending(CreationTimestampKey),
      indexOptions = IndexOptions()
        .name("SoleTraderInformationExpires")
        .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
    )
  }

}
