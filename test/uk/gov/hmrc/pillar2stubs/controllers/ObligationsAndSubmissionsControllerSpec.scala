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

package uk.gov.hmrc.pillar2stubs.controllers

import org.scalatest.OptionValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._

import scala.util.Random

class ObligationsAndSubmissionsControllerSpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with OptionValues {

  val validHeaders: List[(String, String)] =
    (ETMPHeaderFilter.mandatoryHeaders ++ List(HeaderNames.authorisation)).map(_ -> Random.nextString(10))

  def request(implicit pillar2Id: String): FakeRequest[AnyContentAsEmpty.type] = {
    val fromDate = "2024-01-31"
    val toDate   = "2025-01-31"
    FakeRequest(GET, routes.ObligationAndSubmissionsController.retrieveData(fromDate, toDate).url)
      .withHeaders(Headers(validHeaders: _*))
      .withHeaders("X-Pillar2-Id" -> pillar2Id)
  }

  test("Valid ObligationsAndSubmissions submission") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true
  }

  test("UnprocessableEntity - invalid date range") {
    implicit val pillar2Id: String = "XEPLR4220000000"
    val invalidRequest =
      FakeRequest(GET, routes.ObligationAndSubmissionsController.retrieveData("2024-01-31", "2023-01-31").url)
        .withHeaders(Headers(validHeaders: _*))
        .withHeaders("X-Pillar2-Id" -> pillar2Id)

    val result = route(app, invalidRequest).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "001"
    contentAsJson(result)
      .as[ObligationsAndSubmissionsDetailedErrorResponse]
      .errors
      .text shouldEqual "Invalid date range: toDate must be after fromDate"
  }

  test("UnprocessableEntity - missing X-Pillar2-Id") {
    implicit val pillar2Id: String = "XEPLR0200000422"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "002"
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.text shouldEqual "Pillar2 ID is missing or invalid"
  }

  test("UnprocessableEntity - invalid request") {
    implicit val pillar2Id: String = "XEPLR0300000422"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "003"
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.text shouldEqual "Request could not be processed or invalid"
  }

  test("UnprocessableEntity - duplicate submission") {
    implicit val pillar2Id: String = "XEPLR0400000422"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "004"
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.text shouldEqual "Duplicate Submission"
  }

  test("UnprocessableEntity - no data found") {
    implicit val pillar2Id: String = "XEPLR2500000422"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "025"
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.text shouldEqual "No associated data found"
  }

  test("BadRequest ObligationsAndSubmissions submission") {
    implicit val pillar2Id: String = "XEPLR0000000400"
    val result = route(app, request).value

    status(result) shouldEqual 400
    contentAsJson(result).validate[ObligationsAndSubmissionsSimpleErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsSimpleErrorResponse].error.code shouldEqual "400"
  }

  test("InternalServerError ObligationsAndSubmissions submission") {
    implicit val pillar2Id: String = "XEPLR0000000500"
    val result = route(app, request).value

    status(result) shouldEqual 500
    contentAsJson(result).validate[ObligationsAndSubmissionsSimpleErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsSimpleErrorResponse].error.code shouldEqual "500"
  }
}
