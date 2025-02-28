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
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.SubmissionType.{BTN, GIR, ORN, UKTR}
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

  test("Date validation is performed before Pillar2 ID checking") {
    implicit val pillar2Id: String = "XEPLR1111111111" // Special ID for multiple accounting periods

    val invalidRequest =
      FakeRequest(GET, routes.ObligationAndSubmissionsController.retrieveData("2024-01-31", "2023-01-31").url)
        .withHeaders(Headers(validHeaders: _*))
        .withHeaders("X-Pillar2-Id" -> pillar2Id)

    val result = route(app, invalidRequest).value

    // Should return 422 for invalid date range, not 200 with multiple accounting periods
    status(result) shouldEqual 422
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "001"
  }

  test("Returns multiple accounting periods when Pillar2-Id is XEPLR1111111111") {
    implicit val pillar2Id: String = "XEPLR1111111111"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 4

    // Check first period (most recent)
    response.success.accountingPeriodDetails.head.startDate.getYear shouldEqual 2024
    response.success.accountingPeriodDetails.head.obligations.head.status shouldEqual ObligationStatus.Open

    // Check second period
    response.success.accountingPeriodDetails(1).startDate.getYear shouldEqual 2023
    response.success.accountingPeriodDetails(1).underEnquiry shouldEqual true

    // Check third period
    response.success.accountingPeriodDetails(2).startDate.getYear shouldEqual 2022
    response.success.accountingPeriodDetails(2).obligations.head.obligationType shouldEqual ObligationType.GlobeInformationReturn

    // Check fourth period (earliest)
    response.success.accountingPeriodDetails(3).startDate.getYear shouldEqual 2021
    val submissions = response.success.accountingPeriodDetails(3).obligations.head.submissions
    submissions.size shouldEqual 2
    submissions(1).submissionType shouldEqual SubmissionType.BTN
    submissions(1).country.value shouldEqual "FR"
  }

  test("Returns no accounting periods when Pillar2-Id is XEPLR2222222222") {
    implicit val pillar2Id: String = "XEPLR2222222222"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails shouldBe empty
  }

  test("Returns single accounting period when Pillar2-Id is XEPLR3333333333") {
    implicit val pillar2Id: String = "XEPLR3333333333"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.startDate.getYear shouldEqual 2024
    period.endDate.getYear shouldEqual 2024
    period.obligations.head.obligationType shouldEqual ObligationType.Pillar2TaxReturn
    period.obligations.head.status shouldEqual ObligationStatus.Open
  }

  test("Returns submission history for a single account period when Pillar2-Id is XEPLR5555555555") {
    implicit val pillar2Id: String = "XEPLR5555555555"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 3
    response.success.accountingPeriodDetails.head.obligations.head.submissions.size shouldEqual 3
    response.success.accountingPeriodDetails(1).obligations.head.submissions.size shouldEqual 2
    response.success.accountingPeriodDetails(2).obligations.head.submissions.size shouldEqual 2

    val period1 = response.success.accountingPeriodDetails.head
    period1.startDate.getYear shouldEqual 2023
    period1.endDate.getYear shouldEqual 2024
    period1.obligations.head.submissions.head.submissionType shouldEqual GIR
    period1.obligations.head.submissions.head.receivedDate.getYear shouldEqual 2024
    period1.obligations.head.submissions(1).submissionType shouldEqual UKTR
    period1.obligations.head.submissions(1).receivedDate.getYear shouldEqual 2024
    period1.obligations.head.submissions(2).submissionType shouldEqual BTN
    period1.obligations.head.submissions(2).receivedDate.getYear shouldEqual 2025

    val period2 = response.success.accountingPeriodDetails(1)
    period2.startDate.getYear shouldEqual 2022
    period2.endDate.getYear shouldEqual 2023
    period2.obligations.head.submissions.head.submissionType shouldEqual UKTR
    period2.obligations.head.submissions.head.receivedDate.getYear shouldEqual 2023
    period2.obligations.head.submissions(1).submissionType shouldEqual ORN
    period2.obligations.head.submissions(1).receivedDate.getYear shouldEqual 2023

    val period3 = response.success.accountingPeriodDetails(2)
    period3.startDate.getYear shouldEqual 2021
    period3.endDate.getYear shouldEqual 2022
    period3.obligations.head.submissions.head.submissionType shouldEqual UKTR
    period3.obligations.head.submissions.head.receivedDate.getYear shouldEqual 2022
    period3.obligations.head.submissions(1).submissionType shouldEqual GIR
    period3.obligations.head.submissions(1).receivedDate.getYear shouldEqual 2022
  }

  test("Returns submission history for a single account period when Pillar2-Id is XEPLR6666666666") {
    implicit val pillar2Id: String = "XEPLR6666666666"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1
    response.success.accountingPeriodDetails.flatMap(_.obligations.flatMap(_.submissions)).size shouldEqual 2

    val period = response.success.accountingPeriodDetails.head
    period.startDate.getYear shouldEqual 2023
    period.endDate.getYear shouldEqual 2024
    period.obligations.head.submissions.head.submissionType shouldEqual GIR
    period.obligations.head.submissions.head.receivedDate.getYear shouldEqual 2025
    period.obligations.head.submissions(1).submissionType shouldEqual UKTR
    period.obligations.head.submissions(1).receivedDate.getYear shouldEqual 2025
  }

  test("Returns no submission history when Pillar2-Id is XEPLR7777777777") {
    implicit val pillar2Id: String = "XEPLR7777777777"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.forall(_.obligations.forall(_.submissions.isEmpty)) shouldBe true
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
