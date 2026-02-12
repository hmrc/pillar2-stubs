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

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.FinancialDataController.*
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.{FinancialDataResponse, FinancialItem, FinancialTransaction}
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.util.Random
import scala.util.matching.Regex

class FinancialDataController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter, clock: Clock) extends BackendController(cc) {

  def retrieveFinancialData(idNumber: String, dateFrom: String, dateTo: String): Action[AnyContent] =
    (Action andThen authFilter) { _ =>
      val yearsAndTransactionPattern: Regex = "XMPLR0000000(.{3})\\s*$".r
      val processingDate = LocalDateTime.now(clock).toString

      idNumber match {
        case "XEPLR4000000000" => BadRequest(invalidIdNumberJson)
        case "XEPLR4040000000" => NotFound(financialDataNotFoundJson)
        case "XEPLR5000000000" => InternalServerError(financialServerErrorJson)
        case "XEPLR5030000000" => ServiceUnavailable(financialServiceUnavailableJson)
        case "XEPLR2000000000" => Ok(Json.parse(etmpTestDataFull(idNumber, processingDate)))
        case "XEPLR2000000001" => Ok(Json.parse(oneAccountingPeriod(idNumber, processingDate)))
        case "XEPLR2000000002" => Ok(Json.parse(twoAccountingPeriods(idNumber, processingDate)))
        case "XEPLR2000000003" => Ok(Json.parse(overdueUktr(idNumber, processingDate)))
        case "XEPLR2000000004" => Ok(Json.parse(oneAccountingPeriodWithPaidStatus(idNumber, processingDate)))
        case "XEPLR2000000010" => Ok(Json.parse(repaymentInterest(idNumber, processingDate)))
        // Payment overdue w/o interest, no Return
        case "XEPLR2000000101" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment overdue w/ interest, no Return
        case "XEPLR2000000102" => Ok(paymentOverdueWithInterest(idNumber, clock))
        // Payment overdue w/o interest, Return due
        case "XEPLR2000000103" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment overdue w/ interest, Return overdue
        case "XEPLR2000000104" => Ok(paymentOverdueWithInterest(idNumber, clock))
        // Payment overdue w/ interest, Return due
        case "XEPLR2000000105" => Ok(paymentOverdueWithInterest(idNumber, clock))
        // Payment overdue w/o interest, Return overdue
        case "XEPLR2000000106" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment overdue w/o interest, Return received
        case "XEPLR2000000107" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment paid, no Return
        case "XEPLR2000000108" => Ok(Json.parse(oneAccountingPeriodWithPaidStatus(idNumber, processingDate)))
        // No payments, no Return, BTN
        case "XEPLR2000000109" => Ok(Json.parse(baseResponse(idNumber, processingDate = LocalDateTime.now(clock), transactions = Seq.empty)))
        // Payment overdue, Return overdue, BTN
        case "XEPLR2000000110" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment overdue, no Return, BTN
        case "XEPLR2000000111" => Ok(paymentOverdueNoInterest(idNumber, clock))
        // Payment overdue w/ interest, no Return, BTN
        case "XEPLR2000000112" => Ok(paymentOverdueWithInterest(idNumber, clock))
        // Empty financial data response (for testing no outstanding payments - also used by Account Activity API)
        case "XMPLR0000000000" =>
          Ok(Json.parse(baseResponse(idNumber, processingDate = LocalDateTime.now(clock), transactions = Seq.empty)))
        case v @ yearsAndTransactionPattern(numberOfTransactions) =>
          Ok(Json.toJson(generateSuccessfulResponse(v, numberOfTransactions.toInt, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))))
        case _ => Ok(Json.parse(successfulResponse(idNumber, processingDate)))
      }
    }
}

object FinancialDataController {

  val invalidIdNumberJson: JsValue = Json.obj(
    "code"   -> "INVALID_xIDNUMBER",
    "reason" -> "Submission has not passed validation. Invalid parameter idNumber."
  )

  val financialDataNotFoundJson: JsValue = Json.obj(
    "code"   -> "NOT_FOUND",
    "reason" -> "The remote endpoint has indicated that no data can be found"
  )

  val financialServerErrorJson: JsValue = Json.obj(
    "code"   -> "SERVER_ERROR",
    "reason" -> "DES is currently experiencing problems that require live service intervention"
  )

  val financialServiceUnavailableJson: JsValue = Json.obj(
    "code"   -> "SERVICE_UNAVAILABLE",
    "reason" -> "Dependent systems are currently not responding"
  )

  def successfulResponse(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/successfulResponse.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File successfulResponse.json not found"))

  def etmpTestDataFull(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/etmpTestDataFull.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File etmpTestDataFull.json not found"))

  def oneAccountingPeriod(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/oneAccountingPeriod.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File oneAccountingPeriod.json not found"))

  def oneAccountingPeriodWithPaidStatus(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/oneAccountingPeriodWithPaidStatus.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File oneAccountingPeriodWithPaidStatus.json not found"))

  def twoAccountingPeriods(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/twoAccountingPeriods.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File twoAccountingPeriods.json not found"))

  def overdueUktr(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/overdueUktr.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File overdueUktr.json not found"))

  def repaymentInterest(idNumber: String, processingDate: String): String =
    resourceAsString("/resources/financial-data/repaymentInterest.json")
      .map(_.replace("[idNumber]", idNumber).replace("[processingDate]", processingDate))
      .getOrElse(throw new RuntimeException("File repaymentInterest.json not found"))

  def paymentOverdueNoInterest(idNumber: String, clock: Clock): JsObject = {
    val today = LocalDate.now(clock)
    Json
      .parse(
        baseResponse(
          idNumber = idNumber,
          processingDate = LocalDateTime.now(clock),
          transactions = Seq(
            domesticTopUpTaxTransaction(
              paymentDueDate = today.minusDays(10),
              taxPeriodFrom = LocalDate.of(today.getYear, 1, 1),
              taxPeriodTo = LocalDate.of(today.getYear, 12, 31)
            )
          )
        )
      )
      .as[JsObject]
  }

  def paymentOverdueWithInterest(idNumber: String, clock: Clock): JsObject = {
    val today = LocalDate.now(clock)
    Json
      .parse(
        baseResponse(
          idNumber = idNumber,
          processingDate = LocalDateTime.now(clock),
          transactions = Seq(
            domesticTopUpTaxTransaction(
              paymentDueDate = today.minusDays(10),
              taxPeriodFrom = LocalDate.of(today.getYear, 1, 1),
              taxPeriodTo = LocalDate.of(today.getYear, 12, 31)
            ),
            domesticTopUpTaxInterestTransaction(
              paymentDueDate = today.minusDays(10),
              taxPeriodFrom = LocalDate.of(today.getYear, 1, 1),
              taxPeriodTo = LocalDate.of(today.getYear, 12, 31)
            )
          )
        )
      )
      .as[JsObject]
  }

  private def domesticTopUpTaxTransaction(
    paymentDueDate: LocalDate,
    taxPeriodFrom:  LocalDate,
    taxPeriodTo:    LocalDate
  ): JsObject = Json
    .parse(s"""
    |{
    |  "chargeType":"Pillar 2 DTT",
    |  "mainType":"OECD Pillar 2 UK Tax Return",
    |  "taxPeriodFrom":"${taxPeriodFrom.toString}",
    |  "taxPeriodTo":"${taxPeriodTo.toString}",
    |  "businessPartner":"0100007961",
    |  "contractAccountCategory":"53",
    |  "contractAccount":"002100001302",
    |  "contractObjectType":"PLR",
    |  "contractObject":"00000300000000000682",
    |  "sapDocumentNumber":"003540024920",
    |  "sapDocumentNumberItem":"0001",
    |  "chargeReference":"XD002610233120",
    |  "mainTransaction":"6500",
    |  "subTransaction":"6233",
    |  "originalAmount":200000.8,
    |  "outstandingAmount":100000.8,
    |  "clearedAmount":100000.0,
    |  "items":[
    |    {
    |      "subItem":"000",
    |      "dueDate":"${paymentDueDate.toString}",
    |      "amount":100000.8
    |    },
    |    {
    |      "subItem":"001",
    |      "dueDate":"${paymentDueDate.toString}",
    |      "amount":100000.0,
    |      "clearingDate":"2026-10-14",
    |      "clearingReason":"Incoming Payment",
    |      "paymentReference":"XD002610233120",
    |      "paymentAmount":900000.0,
    |      "paymentMethod":"PAYMENTS MADE BY CHEQUE",
    |      "paymentLot":"C00125",
    |      "paymentLotItem":"000001",
    |      "clearingSAPDocument":"294000000145"
    |    }
    |  ]
    |}""".stripMargin)
    .as[JsObject]

  private def domesticTopUpTaxInterestTransaction(
    paymentDueDate: LocalDate,
    taxPeriodFrom:  LocalDate,
    taxPeriodTo:    LocalDate
  ): JsObject = Json
    .parse(s"""
    |{
    |  "chargeType":"Pillar 2 DTT Interest",
    |  "mainType":"Pillar 2 UKTR Interest",
    |  "taxPeriodFrom":"${taxPeriodFrom.toString}",
    |  "taxPeriodTo":"${taxPeriodTo.toString}",
    |  "businessPartner":"0100007961",
    |  "contractAccountCategory":"53",
    |  "contractAccount":"002100001302",
    |  "contractObjectType":"PLR",
    |  "contractObject":"00000300000000000682",
    |  "sapDocumentNumber":"428000000021",
    |  "sapDocumentNumberItem":"0001",
    |  "chargeReference":"XY428000000021",
    |  "mainTransaction":"6503",
    |  "subTransaction":"6236",
    |  "originalAmount":5.4,
    |  "outstandingAmount":5.4,
    |  "items":[
    |    {
    |      "subItem":"000",
    |      "dueDate":"${paymentDueDate.toString}",
    |      "amount":5.4
    |    }
    |  ]
    |}
    |""".stripMargin)
    .as[JsObject]

  def baseResponse(idNumber: String, processingDate: LocalDateTime, transactions: Seq[JsObject]): String = s"""
    |{
    |  "idType":"ZPLR",
    |  "idNumber":"$idNumber",
    |  "regimeType":"PLR",
    |  "processingDate":"${processingDate.toString}",
    |  "financialTransactions":${Json.stringify(JsArray(transactions))}
    |}
    |""".stripMargin

  private def generateFinancialTransactions(numberOfTransactions: Int, dateFrom: LocalDate, dateTo: LocalDate): Seq[FinancialTransaction] = {
    require(numberOfTransactions > 0, "Number of transactions must be positive")

    // Transactions distribution: ensure at least 1 Payment, then split remaining between Repayments and Interest
    val paymentsCount:         Int = math.max(1, math.min(numberOfTransactions, numberOfTransactions / 3)) // at least 1 Payment
    val remainingTransactions: Int = numberOfTransactions - paymentsCount
    val repaymentsCount: Int = if remainingTransactions > 0 then math.max(1, remainingTransactions / 2) else 0 // at least one Repayment if possible
    val interestRepaymentsCount: Int = math.max(0, numberOfTransactions - paymentsCount - repaymentsCount) // Remaining transactions are Interest

    val payments: Seq[FinancialTransaction] = (1 to paymentsCount).map(_ =>
      FinancialTransaction(
        mainTransaction = Some("0060"),
        items = Seq(
          FinancialItem(
            dueDate = Some(generateRandomDate(dateFrom, dateTo)),
            paymentAmount = Some(generateBigDecimal)
          )
        )
      )
    )

    val repayments: Seq[FinancialTransaction] = (1 to repaymentsCount).map(_ =>
      FinancialTransaction(
        mainTransaction = Some("1234"),
        items = Seq(
          FinancialItem(
            clearingDate = Some(generateRandomDate(dateFrom, dateTo)),
            clearingReason = Some("Outgoing payment - Paid"),
            amount = Some(generateBigDecimal)
          )
        )
      )
    )

    val repaymentInterests: Seq[FinancialTransaction] = (1 to interestRepaymentsCount).map(_ =>
      FinancialTransaction(
        mainTransaction = Some("6504"),
        items = Seq(
          FinancialItem(
            clearingDate = Some(generateRandomDate(dateFrom, dateTo)),
            amount = Some(generateBigDecimal)
          )
        )
      )
    )

    payments ++ repayments ++ repaymentInterests
  }

  private def generateRandomDate(dateFrom: LocalDate, dateTo: LocalDate): LocalDate =
    LocalDate.ofEpochDay(Random.between(dateFrom.toEpochDay, dateTo.toEpochDay + 1))

  private def generateBigDecimal: BigDecimal = {
    val start: Double = 10000.00
    val end:   Double = 100000000000.00

    BigDecimal.valueOf(Random.between(start, end)).setScale(2, BigDecimal.RoundingMode.HALF_EVEN)
  }

  private def generateSuccessfulResponse(idNumber: String, numberOfTransactions: Int, dateFrom: LocalDate, dateTo: LocalDate) =
    FinancialDataResponse(
      "ZPLR",
      idNumber,
      "PLR",
      LocalDateTime.now,
      financialTransactions = generateFinancialTransactions(numberOfTransactions, dateFrom, dateTo)
    )

}
