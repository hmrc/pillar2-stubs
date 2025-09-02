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
import uk.gov.hmrc.pillar2stubs.models.error.Origin.HIP
import uk.gov.hmrc.pillar2stubs.models.error.{HIPErrorWrapper, HIPFailure}
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.ObligationsAndSubmissionsResponse.now
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.SubmissionType.{GIR, UKTR_CREATE}
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._

import java.time.LocalDate
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
    val result      = route(app, request).value
    val currentYear = LocalDate.now().getYear()

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 4

    // Check first period (most recent)
    response.success.accountingPeriodDetails.head.startDate.getYear shouldEqual currentYear
    response.success.accountingPeriodDetails.head.obligations.head.status shouldEqual ObligationStatus.Open

    // Check second period
    response.success.accountingPeriodDetails(1).startDate.getYear shouldEqual currentYear - 1
    response.success.accountingPeriodDetails(1).underEnquiry shouldEqual true

    // Check third period
    response.success.accountingPeriodDetails(2).startDate.getYear shouldEqual currentYear - 2
    response.success.accountingPeriodDetails(2).obligations.head.obligationType shouldEqual ObligationType.GIR

    // Check fourth period (earliest)
    response.success.accountingPeriodDetails(3).startDate.getYear shouldEqual currentYear - 3
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
    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Open
    period.obligations.head.submissions.size shouldEqual 0
  }

  test("Returns all fulfilled obligations when Pillar2-Id is XEPLR4444444444") {
    implicit val pillar2Id: String = "XEPLR4444444444"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.foreach { period =>
      period.obligations.foreach { obligation =>
        obligation.status shouldEqual ObligationStatus.Fulfilled
      }
    }
  }

  test("Returns all fulfilled obligations when Pillar2-Id is XEPLR9999999995") {
    implicit val pillar2Id: String = "XEPLR9999999995"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.foreach { period =>
      period.obligations.foreach { obligation =>
        obligation.status shouldEqual ObligationStatus.Fulfilled
      }
    }

    val receivedPeriodStart = now.minusDays(60)
    val hasRecentUktrOrGir: Boolean =
      response.success.accountingPeriodDetails.exists { period =>
        period.obligations
          .flatMap(_.submissions)
          .exists(s =>
            (s.submissionType == UKTR_CREATE || s.submissionType == GIR) &&
              receivedPeriodStart.isBefore(s.receivedDate)
          )
      }
    hasRecentUktrOrGir shouldBe true
  }

  test("Returns multiple accounting periods with submissions when Pillar2-Id is XEPLR5555555555") {
    implicit val pillar2Id: String = "XEPLR5555555555"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size should be > 1

    // Check that at least one obligation has submissions
    val hasSubmissions = response.success.accountingPeriodDetails.exists { period =>
      period.obligations.exists(_.submissions.nonEmpty)
    }
    hasSubmissions shouldBe true

    // Check first period has a fulfilled obligation with a submission
    val firstPeriod = response.success.accountingPeriodDetails.head
    firstPeriod.obligations.head.status shouldEqual ObligationStatus.Fulfilled
    firstPeriod.obligations.head.submissions.nonEmpty shouldBe true
    firstPeriod.obligations.head.submissions.head.submissionType shouldEqual SubmissionType.UKTR_CREATE
  }

  test("Returns single accounting period when Pillar2-Id is XEPLR7777777777") {
    implicit val pillar2Id: String = "XEPLR7777777777"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Fulfilled
    period.obligations.head.submissions.size shouldEqual 1
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
    val response = contentAsJson(result).as[HIPErrorWrapper[HIPFailure]]
    response.response.failures should have size 1
    response.response.failures.head.reason shouldEqual "invalid json"
    response.origin shouldEqual HIP
  }

  test("InternalServerError ObligationsAndSubmissions submission") {
    implicit val pillar2Id: String = "XEPLR0000000500"
    val result = route(app, request).value

    status(result) shouldEqual 500
    contentAsJson(result).validate[ObligationsAndSubmissionsSimpleErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsSimpleErrorResponse].error.code shouldEqual "500"
  }

  test("Returns default single accounting period for any other valid Pillar2 ID") {
    implicit val pillar2Id: String = "XTEST0012345999"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.startDate.getYear shouldEqual LocalDate.now().getYear() - 1
    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Open
    period.obligations.head.submissions shouldBe empty
  }

  test("Returns fulfilled scenario when Pillar2-Id is XEPLR0000000504") {
    implicit val pillar2Id: String = "XEPLR0000000504"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 2

    val periods = response.success.accountingPeriodDetails
    periods.foreach { period =>
      period.obligations.head.status shouldEqual ObligationStatus.Fulfilled
      period.obligations.head.submissions should not be empty
    }
  }

  test("Returns UKTR due scenario when Pillar2-Id is XMPLR0012345675") {
    implicit val pillar2Id: String = "XMPLR0012345675"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails should not be empty

    val period = response.success.accountingPeriodDetails.head
    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Open
  }

}
