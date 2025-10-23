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
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
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

class ObligationsAndSubmissionsControllerSpec
    extends AnyFunSuite
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues
    with TableDrivenPropertyChecks {

  val validHeaders: List[(String, String)] =
    (ETMPHeaderFilter.mandatoryHeaders ++ List(HeaderNames.authorisation)).map(_ -> Random.nextString(10))

  def request(implicit pillar2Id: String): FakeRequest[AnyContentAsEmpty.type] = {
    val fromDate = "2024-01-31"
    val toDate   = "2025-01-31"
    FakeRequest(GET, routes.ObligationAndSubmissionsController.retrieveData(fromDate, toDate).url)
      .withHeaders(Headers(validHeaders*))
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
        .withHeaders(Headers(validHeaders*))
        .withHeaders("X-Pillar2-Id" -> pillar2Id)

    val result = route(app, invalidRequest).value

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

    response.success.accountingPeriodDetails.head.startDate.getYear shouldEqual currentYear
    response.success.accountingPeriodDetails.head.obligations.head.status shouldEqual ObligationStatus.Open

    response.success.accountingPeriodDetails(1).startDate.getYear shouldEqual currentYear - 1
    response.success.accountingPeriodDetails(1).underEnquiry shouldEqual true

    response.success.accountingPeriodDetails(2).startDate.getYear shouldEqual currentYear - 2
    response.success.accountingPeriodDetails(2).obligations.head.obligationType shouldEqual ObligationType.GIR

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

  test("Returns all fulfilled obligations and received flag when Pillar2-Id is XEPLR4444444445") {
    implicit val pillar2Id: String = "XEPLR4444444445"
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

    val hasSubmissions = response.success.accountingPeriodDetails.exists { period =>
      period.obligations.exists(_.submissions.nonEmpty)
    }
    hasSubmissions shouldBe true

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
        .withHeaders(Headers(validHeaders*))
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

  test("NotFound - record not found") {
    implicit val pillar2Id: String = "XEPLR0300000404"
    val result = route(app, request).value
    status(result) shouldEqual 404
    contentAsJson(result).validate[ObligationsAndSubmissionsDetailedErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.code shouldEqual "004"
    contentAsJson(result).as[ObligationsAndSubmissionsDetailedErrorResponse].errors.text shouldEqual "Record not found"
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

  test("Returns expected responses for Pillar2-Ids") {
    forAll(
      Table(
        "id"              -> "expected response",
        "XEPLR2000000101" -> ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods(),
        "XEPLR2000000102" -> ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods(),
        "XEPLR2000000103" -> ObligationsAndSubmissionsSuccessResponse.uktrDueScenario(),
        "XEPLR2000000104" -> ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario(),
        "XEPLR2000000105" -> ObligationsAndSubmissionsSuccessResponse(),
        "XEPLR2000000106" -> ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario(),
        "XEPLR2000000107" -> ObligationsAndSubmissionsSuccessResponse.withAllFulfilledAndReceived(),
        "XEPLR2000000108" -> ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods(),
        "XEPLR2000000109" -> ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods(),
        "XEPLR2000000110" -> ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario(),
        "XEPLR2000000111" -> ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()
      )
    ) { (id, expectedResponse) =>
      val result = route(app, request(id)).value

      status(result).shouldEqual(200)
      contentAsJson(result).shouldBe(Json.toJson(expectedResponse))
    }
  }

  test("Returns single active accounting period with no submission when Pillar2-Id is XEPLR9999999991") {
    implicit val pillar2Id: String = "XEPLR9999999991"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.startDate.getYear shouldEqual LocalDate.now().getYear()
    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Open
    period.obligations.head.submissions shouldBe empty
  }

  test("Returns two active accounting periods with no submissions when Pillar2-Id is XEPLR9999999992") {
    implicit val pillar2Id: String = "XEPLR9999999992"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 2

    val periods = response.success.accountingPeriodDetails
    periods.foreach { period =>
      period.obligations.head.obligationType shouldEqual ObligationType.UKTR
      period.obligations.head.status shouldEqual ObligationStatus.Open
      period.obligations.head.submissions shouldBe empty
    }
  }

  test("Returns three active accounting periods with different scenarios when Pillar2-Id is XEPLR9999999993") {
    implicit val pillar2Id: String = "XEPLR9999999993"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 3

    val periods = response.success.accountingPeriodDetails
    periods.head.obligations.head.status shouldEqual ObligationStatus.Fulfilled
    periods.head.obligations.head.submissions.nonEmpty shouldBe true

    periods(1).obligations.head.status shouldEqual ObligationStatus.Open
    periods(1).obligations.head.submissions shouldBe empty
    periods(2).obligations.head.status shouldEqual ObligationStatus.Open
    periods(2).obligations.head.submissions shouldBe empty
  }

  test("Returns four active accounting periods with different scenarios when Pillar2-Id is XEPLR9999999994") {
    implicit val pillar2Id: String = "XEPLR9999999994"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 4

    val periods = response.success.accountingPeriodDetails
    periods.head.obligations.head.status shouldEqual ObligationStatus.Fulfilled
    periods.head.obligations.head.submissions.nonEmpty shouldBe true
    periods.head.obligations.head.submissions.head.submissionType shouldEqual SubmissionType.UKTR_CREATE

    periods(1).obligations.head.status shouldEqual ObligationStatus.Fulfilled
    periods(1).obligations.head.submissions.nonEmpty shouldBe true
    periods(1).obligations.head.submissions.head.submissionType shouldEqual SubmissionType.BTN

    periods(2).obligations.head.status shouldEqual ObligationStatus.Open
    periods(2).obligations.head.submissions shouldBe empty
    periods(3).obligations.head.status shouldEqual ObligationStatus.Open
    periods(3).obligations.head.submissions shouldBe empty
  }

  test("Returns BTN under enquiry scenario when Pillar2-Id is XEPLR9999999995") {
    implicit val pillar2Id: String = "XEPLR9999999995"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 3

    val periods = response.success.accountingPeriodDetails
    periods(1).underEnquiry              shouldBe true
    periods(1).obligations.head.canAmend shouldBe false

    periods.head.underEnquiry shouldBe false
    periods(2).underEnquiry   shouldBe false
  }

  test("Returns UKTR incomplete scenario when Pillar2-Id is XMPLR0012345677") {
    implicit val pillar2Id: String = "XMPLR0012345677"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.obligations.size shouldEqual 2

    period.obligations.head.obligationType shouldEqual ObligationType.UKTR
    period.obligations.head.status shouldEqual ObligationStatus.Fulfilled
    period.obligations.head.submissions.nonEmpty shouldBe true

    period.obligations(1).obligationType shouldEqual ObligationType.GIR
    period.obligations(1).status shouldEqual ObligationStatus.Open
    period.obligations(1).submissions shouldBe empty
  }

  test("Returns two active accounting periods with no submissions when Pillar2-Id is XEPLR1066196602") {
    implicit val pillar2Id: String = "XEPLR1066196602"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 2

    val periods = response.success.accountingPeriodDetails
    periods.foreach { period =>
      period.obligations.head.obligationType shouldEqual ObligationType.UKTR
      period.obligations.head.status shouldEqual ObligationStatus.Open
      period.obligations.head.submissions shouldBe empty
    }
  }

  test("JSON Writes for ObligationsAndSubmissionsResponse covers all response types") {
    import play.api.libs.json.Json
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._

    val successResponse = ObligationsAndSubmissionsSuccessResponse()
    val successJson     = Json.toJson(successResponse)
    successJson.as[JsObject].keys should contain("success")

    val simpleErrorResponse = ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError("Test error")
    val simpleErrorJson     = Json.toJson(simpleErrorResponse)
    simpleErrorJson.as[JsObject].keys should contain("error")

    val detailedErrorResponse = ObligationsAndSubmissionsDetailedErrorResponse.invalidDateRange
    val detailedErrorJson     = Json.toJson(detailedErrorResponse)
    detailedErrorJson.as[JsObject].keys should contain("errors")
  }

  test("JSON serialization and deserialization for ObligationsAndSubmissionsSuccessResponse") {
    import play.api.libs.json.Json
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._

    val response = ObligationsAndSubmissionsSuccessResponse()

    val json = Json.toJson(response)
    json shouldBe a[JsObject]

    val parsedResponse = Json.fromJson[ObligationsAndSubmissionsSuccessResponse](json)
    parsedResponse.isSuccess shouldBe true
    parsedResponse.get shouldEqual response
  }

  test("JSON serialization covers all response builder methods") {
    import play.api.libs.json.Json
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._

    val responses = Seq(
      ObligationsAndSubmissionsSuccessResponse(),
      ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods(),
      ObligationsAndSubmissionsSuccessResponse.withMultipleAccountingPeriods(),
      ObligationsAndSubmissionsSuccessResponse.withMultipleAccountingPeriodsWithSubmissions(),
      ObligationsAndSubmissionsSuccessResponse.withAllFulfilled(),
      ObligationsAndSubmissionsSuccessResponse.withAllFulfilledAndReceived(),
      ObligationsAndSubmissionsSuccessResponse.singleActiveAccountingPeriodWithNoSubmission(),
      ObligationsAndSubmissionsSuccessResponse.twoActiveAccountingPeriodsWithNoSubmissions(),
      ObligationsAndSubmissionsSuccessResponse.threeActiveAccountingPeriodsWithDifferentScenarios(),
      ObligationsAndSubmissionsSuccessResponse.fourActiveAccountingPeriodsWithDifferentScenarios(),
      ObligationsAndSubmissionsSuccessResponse.uktrDueScenario(),
      ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario(),
      ObligationsAndSubmissionsSuccessResponse.uktrIncompleteScenario(),
      ObligationsAndSubmissionsSuccessResponse.btnUnderEnquiryScenario(),
      ObligationsAndSubmissionsSuccessResponse.withEmptyObligations()
    )

    responses.foreach { response =>
      val json = Json.toJson(response)
      json                 shouldBe a[JsObject]
      json.as[JsObject].keys should contain("success")
    }
  }

  test("Returns empty obligations scenario when Pillar2-Id is XEPLR9999999996") {
    implicit val pillar2Id: String = "XEPLR9999999996"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[ObligationsAndSubmissionsSuccessResponse].asEither.isRight shouldBe true

    val response = contentAsJson(result).as[ObligationsAndSubmissionsSuccessResponse]
    response.success.accountingPeriodDetails.size shouldEqual 1

    val period = response.success.accountingPeriodDetails.head
    period.obligations shouldBe empty
  }

  test("JSON serialization covers all case classes and their OFormat instances") {
    import play.api.libs.json.Json
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._
    import java.time.LocalDate
    import java.time.ZonedDateTime

    val success = ObligationsAndSubmissionsSuccess(
      processingDate = ZonedDateTime.now(),
      accountingPeriodDetails = Seq.empty
    )
    val successJson   = Json.toJson(success)
    val parsedSuccess = Json.fromJson[ObligationsAndSubmissionsSuccess](successJson)
    parsedSuccess.isSuccess shouldBe true

    val accountingPeriod = AccountingPeriodDetails(
      startDate = LocalDate.of(2024, 1, 1),
      endDate = LocalDate.of(2024, 12, 31),
      dueDate = LocalDate.of(2025, 1, 31),
      underEnquiry = false,
      obligations = Seq.empty
    )
    val accountingPeriodJson   = Json.toJson(accountingPeriod)
    val parsedAccountingPeriod = Json.fromJson[AccountingPeriodDetails](accountingPeriodJson)
    parsedAccountingPeriod.isSuccess shouldBe true

    val simpleErrorResponse = ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError("Test error")
    val simpleErrorJson     = Json.toJson(simpleErrorResponse)
    val parsedSimpleError   = Json.fromJson[ObligationsAndSubmissionsSimpleErrorResponse](simpleErrorJson)
    parsedSimpleError.isSuccess shouldBe true

    val simpleError          = ObligationsAndSubmissionsSimpleError("400", "Test error", "C123")
    val simpleErrorObjJson   = Json.toJson(simpleError)
    val parsedSimpleErrorObj = Json.fromJson[ObligationsAndSubmissionsSimpleError](simpleErrorObjJson)
    parsedSimpleErrorObj.isSuccess shouldBe true

    val detailedErrorResponse = ObligationsAndSubmissionsDetailedErrorResponse.invalidDateRange
    val detailedErrorJson     = Json.toJson(detailedErrorResponse)
    val parsedDetailedError   = Json.fromJson[ObligationsAndSubmissionsDetailedErrorResponse](detailedErrorJson)
    parsedDetailedError.isSuccess shouldBe true

    val detailedError = ObligationsAndSubmissionsDetailedError(
      processingDate = ZonedDateTime.now(),
      code = "001",
      text = "Test error"
    )
    val detailedErrorObjJson   = Json.toJson(detailedError)
    val parsedDetailedErrorObj = Json.fromJson[ObligationsAndSubmissionsDetailedError](detailedErrorObjJson)
    parsedDetailedErrorObj.isSuccess shouldBe true
  }

  test("Error code constants are accessible and used") {
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.ObligationsAndSubmissionsErrorCodes._

    PILLAR_2_ID_MISSING_OR_INVALID_002 shouldEqual ("002", "Pillar2 ID is missing or invalid")
    REQUEST_COULD_NOT_BE_PROCESSED_003 shouldEqual ("003", "Request could not be processed or invalid")
    DUPLICATE_SUBMISSION_004 shouldEqual ("004", "Duplicate Submission")
    NO_DATA_FOUND_025 shouldEqual ("025", "No associated data found")
    RECORD_NOT_FOUND_004 shouldEqual ("004", "Record not found")
    TIMEOUT_ERROR_499 shouldEqual ("499", "Request timeout")
    BAD_REQUEST_400 shouldEqual "400"
    INTERNAL_SERVER_ERROR_500 shouldEqual "500"
  }

  test("ObligationsAndSubmissionsDetailedErrorResponse apply method works correctly") {
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._
    import play.api.libs.json.Json

    val errorCode = ("999", "Test error message")
    val response  = ObligationsAndSubmissionsDetailedErrorResponse(errorCode)

    response.errors.code shouldEqual "999"
    response.errors.text shouldEqual "Test error message"

    val json = Json.toJson(response)
    json.as[JsObject].keys should contain("errors")
  }

  test("SAPError lazy val is accessible and has correct structure") {
    import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._
    import play.api.libs.json.Json

    val sapError = ObligationsAndSubmissionsSimpleErrorResponse.SAPError
    sapError.error.code shouldEqual "500"
    sapError.error.message shouldEqual "Internal server error"

    val json = Json.toJson(sapError)
    json.as[JsObject].keys should contain("error")
  }

}
