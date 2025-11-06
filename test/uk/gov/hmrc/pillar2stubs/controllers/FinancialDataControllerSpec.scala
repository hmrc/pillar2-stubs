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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.controllers.FinancialDataController.*

import java.time.*
import scala.concurrent.Future

class FinancialDataControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues with TableDrivenPropertyChecks {

  private val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().overrides(bind[Clock].to(fixedClock)).build()

  private val authHeader: (String, String) = HeaderNames.authorisation -> "token"

  lazy val dateFrom: String = "2024-01-31"
  lazy val dateTo:   String = "2025-01-31"

  def buildFakeRequest(idNumber: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.FinancialDataController.retrieveFinancialData(idNumber, dateFrom, dateTo).url)
      .withHeaders(authHeader)

  "FinancialDataController" - {
    "must return BAD_REQUEST for an invalid ID number" in {
      val invalidIdNumber: String                              = "XEPLR4000000000"
      val request:         FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(invalidIdNumber)
      val result:          Future[Result]                      = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe invalidIdNumberJson
    }

    "must return NOT_FOUND when no data are found" in {
      val validIdNumber: String                              = "XEPLR4040000000"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe financialDataNotFoundJson
    }

    "must return INTERNAL_SERVER_ERROR when DES is experiencing an error" in {
      val validIdNumber: String                              = "XEPLR5000000000"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe financialServerErrorJson
    }

    "must return SERVICE_UNAVAILABLE when dependent systems are not responding" in {
      val validIdNumber: String                              = "XEPLR5030000000"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      status(result) mustBe SERVICE_UNAVAILABLE
      contentAsJson(result) mustBe financialServiceUnavailableJson
    }

    "must return OK when financial data exists for the given ID" in {
      val validIdNumber: String                              = "XEPLR2000000000"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(etmpTestDataFull(validIdNumber, processingDateTime))
    }

    "must return OK with one Accounting Period data when financial data exists for the given ID" in {
      val validIdNumber: String                              = "XEPLR2000000001"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(oneAccountingPeriod(validIdNumber, processingDateTime))
    }

    "must return OK with two Accounting Periods data when financial data exists for the given ID" in {
      val validIdNumber: String                              = "XEPLR2000000002"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(twoAccountingPeriods(validIdNumber, processingDateTime))
    }

    "must return OK with two Late Payment Interest (Overdue) data when financial data exists for the given ID" in {
      val validIdNumber: String                              = "XEPLR2000000003"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(overdueUktr(validIdNumber, processingDateTime))
    }

    "must return OK with Repayment Interest (RPI) data when financial data exists for the given ID" in {
      val validIdNumber: String                              = "XEPLR2000000010"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(repaymentInterest(validIdNumber, processingDateTime))
    }

    "must return OK with overdue payment data for the given IDs" in forAll(
      Table(
        "id"              -> "interest due",
        "XEPLR2000000101" -> false,
        "XEPLR2000000102" -> true,
        "XEPLR2000000103" -> false,
        "XEPLR2000000104" -> true,
        "XEPLR2000000105" -> true,
        "XEPLR2000000106" -> false,
        "XEPLR2000000107" -> false,
        "XEPLR2000000110" -> false,
        "XEPLR2000000111" -> false,
        "XEPLR2000000112" -> true
      )
    ) { (id, interestDue) =>
      val request: FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(id)
      val result:  Future[Result]                      = route(app, request).value

      status(result) mustBe OK
      if (interestDue) {
        contentAsJson(result) mustBe paymentOverdueWithInterest(id, fixedClock)
      } else {
        contentAsJson(result) mustBe paymentOverdueNoInterest(id, fixedClock)
      }

    }

    "must return OK with a single accounting period and paid status" in {
      val validIdNumber: String                              = "XEPLR2000000108"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(oneAccountingPeriodWithPaidStatus(validIdNumber, processingDateTime))
    }

    "must return OK with no transactions" in {
      val validIdNumber: String                              = "XEPLR2000000109"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(baseResponse(validIdNumber, processingDate = LocalDateTime.now(fixedClock), transactions = Seq.empty))
    }

    "must return OK with default response when the ID does not match a pattern" in {
      val validIdNumber: String                              = "SOME-ID-123"
      val request:       FakeRequest[AnyContentAsEmpty.type] = buildFakeRequest(validIdNumber)
      val result:        Future[Result]                      = route(app, request).value

      val processingDateTime: String = (contentAsJson(result) \ "processingDate").as[String]

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse(successfulResponse(validIdNumber, processingDateTime))
    }
  }

}
