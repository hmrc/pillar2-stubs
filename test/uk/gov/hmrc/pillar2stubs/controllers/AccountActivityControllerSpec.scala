/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.accountactivity.*

import scala.util.Random

class AccountActivityControllerSpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with OptionValues {

  val validHeaders: List[(String, String)] =
    (ETMPHeaderFilter.mandatoryHeaders ++ List(HeaderNames.authorisation)).map(_ -> Random.nextString(10))

  def request(implicit pillar2Id: String): FakeRequest[AnyContentAsEmpty.type] = {
    val fromDate = "2024-01-01"
    val toDate   = "2024-12-31"
    FakeRequest(GET, routes.AccountActivityController.retrieveAccountActivity(fromDate, toDate).url)
      .withHeaders(Headers(validHeaders*))
      .withHeaders("X-Pillar2-Id" -> pillar2Id, "X-Message-Type" -> "ACCOUNT_ACTIVITY")
  }

  test("Valid Account Activity submission returns 200") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[AccountActivitySuccessResponse].asEither.isRight shouldBe true
  }

  test("Date validation is performed before Pillar2 ID checking") {
    implicit val pillar2Id: String = "XEPLR0000000400" // Special ID for 400 error

    val invalidRequest =
      FakeRequest(GET, routes.AccountActivityController.retrieveAccountActivity("2024-01-31", "2023-01-31").url)
        .withHeaders(Headers(validHeaders*))
        .withHeaders("X-Pillar2-Id" -> pillar2Id, "X-Message-Type" -> "ACCOUNT_ACTIVITY")

    val result = route(app, invalidRequest).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[AccountActivity422ErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.code shouldEqual "003"
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.text shouldEqual "Request could not be processed"
  }

  test("Returns success response with transaction details for default ID") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 200
    val response = contentAsJson(result).as[AccountActivitySuccessResponse]
    response.success.transactionDetails should not be empty
    Set("Payment", "Debit", "Credit")   should contain(response.success.transactionDetails.head.transactionType)
  }

  test("BadRequest - returns 400 error for XEPLR0000000400") {
    implicit val pillar2Id: String = "XEPLR0000000400"
    val result = route(app, request).value

    status(result) shouldEqual 400
    contentAsJson(result).validate[AccountActivityErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivityErrorResponse].error.code shouldEqual "400"
    contentAsJson(result).as[AccountActivityErrorResponse].error.message shouldEqual "Bad Request"
    contentAsJson(result).as[AccountActivityErrorResponse].error.logID shouldEqual "1D43D17801EBCC4C4EAB8974C05448D9"
  }

  test("InternalServerError - returns 500 error for XEPLR0000000500") {
    implicit val pillar2Id: String = "XEPLR0000000500"
    val result = route(app, request).value

    status(result) shouldEqual 500
    contentAsJson(result).validate[AccountActivityErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivityErrorResponse].error.code shouldEqual "500"
    contentAsJson(result).as[AccountActivityErrorResponse].error.message shouldEqual "Internal Server Error"
    contentAsJson(result).as[AccountActivityErrorResponse].error.logID shouldEqual "1D43D17801EBCC4C4EAB8974C05448D9"
  }

  test("UnprocessableEntity - returns 422 with code 001 for XEPLR0000422001") {
    implicit val pillar2Id: String = "XEPLR0000422001"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[AccountActivity422ErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.code shouldEqual "001"
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.text shouldEqual "REGIME missing or invalid"
  }

  test("UnprocessableEntity - returns 422 with code 003 for XEPLR0000422003") {
    implicit val pillar2Id: String = "XEPLR0000422003"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[AccountActivity422ErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.code shouldEqual "003"
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.text shouldEqual "Request could not be processed"
  }

  test("UnprocessableEntity - returns 422 with code 014 for XEPLR0000422014") {
    implicit val pillar2Id: String = "XEPLR0000422014"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[AccountActivity422ErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.code shouldEqual "014"
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.text shouldEqual "No data found"
  }

  test("UnprocessableEntity - returns 422 with code 089 for XEPLR0000422089") {
    implicit val pillar2Id: String = "XEPLR0000422089"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[AccountActivity422ErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.code shouldEqual "089"
    contentAsJson(result).as[AccountActivity422ErrorResponse].errors.text shouldEqual "ID number missing or invalid"
  }

  test("Returns 400 BadRequest for invalid date format") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val invalidRequest =
      FakeRequest(GET, routes.AccountActivityController.retrieveAccountActivity("invalid-date", "2024-12-31").url)
        .withHeaders(Headers(validHeaders*))
        .withHeaders("X-Pillar2-Id" -> pillar2Id, "X-Message-Type" -> "ACCOUNT_ACTIVITY")

    val result = route(app, invalidRequest).value

    status(result) shouldEqual 400
    (contentAsJson(result) \ "error" \ "code").as[String] shouldEqual "400"
  }

  test("Success response contains required fields") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 200
    val response = contentAsJson(result).as[AccountActivitySuccessResponse]
    response.success.transactionDetails should not be empty

    // Check that required fields are present in transaction details
    response.success.transactionDetails.foreach { transaction =>
      transaction.transactionType should not be empty
      transaction.transactionDesc should not be empty
      transaction.originalAmount  should not be BigDecimal(0)
    }
  }

  test("Success response contains clearing details when present") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 200
    val response = contentAsJson(result).as[AccountActivitySuccessResponse]

    // Find transactions with clearing details
    val transactionsWithClearing = response.success.transactionDetails.filter(_.clearingDetails.isDefined)
    transactionsWithClearing should not be empty

    transactionsWithClearing.foreach { transaction =>
      transaction.clearingDetails.get should not be empty
      transaction.clearingDetails.get.foreach { clearing =>
        clearing.transactionDesc should not be empty
        clearing.amount          should not be BigDecimal(0)
      }
    }
  }

  test("BadRequest - returns 400 error when X-Message-Type header is missing or invalid") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val fromDate = "2024-01-01"
    val toDate   = "2024-12-31"

    // Test missing X-Message-Type header
    val headersWithoutMessageType = validHeaders.filterNot(_._1 == "X-Message-Type")
    val missingHeaderRequest      = FakeRequest(GET, routes.AccountActivityController.retrieveAccountActivity(fromDate, toDate).url)
      .withHeaders(Headers(headersWithoutMessageType*))
      .withHeaders("X-Pillar2-Id" -> pillar2Id)

    val resultMissing = route(app, missingHeaderRequest).value
    status(resultMissing) shouldEqual 400
    contentAsJson(resultMissing).as[AccountActivityErrorResponse].error.code shouldEqual "400"
    contentAsJson(resultMissing).as[AccountActivityErrorResponse].error.message shouldEqual "Bad Request"

    // Test invalid X-Message-Type header value
    val invalidHeaderRequest = FakeRequest(GET, routes.AccountActivityController.retrieveAccountActivity(fromDate, toDate).url)
      .withHeaders(Headers(validHeaders*))
      .withHeaders("X-Pillar2-Id" -> pillar2Id, "X-Message-Type" -> "INVALID_VALUE")

    val resultInvalid = route(app, invalidHeaderRequest).value
    status(resultInvalid) shouldEqual 400
    contentAsJson(resultInvalid).as[AccountActivityErrorResponse].error.code shouldEqual "400"
  }

}
