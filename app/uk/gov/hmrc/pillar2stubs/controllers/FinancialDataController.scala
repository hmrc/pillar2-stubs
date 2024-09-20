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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.FinancialDataController._
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.{FinancialDataResponse, FinancialItem, FinancialTransaction}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.util.Random
import scala.util.matching.Regex

class FinancialDataController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) {

  def retrieveFinancialData(idType: String, idNumber: String, regimeType: String, dateFrom: String, dateTo: String): Action[AnyContent] =
    (Action andThen authFilter) { _ =>
      val yearsAndTransactionPattern: Regex = "XMPLR0000000(.{3})\\s*$".r

      idNumber match {
        case "XEPLR4000000000" => BadRequest(Json.parse(InvalidIdNumber))
        case "XEPLR4040000000" => NotFound(Json.parse(FinancialDataNotFound))
        case "XEPLR5000000000" => InternalServerError(Json.parse(FinancialServerError))
        case "XEPLR5030000000" => ServiceUnavailable(Json.parse(FinancialServiceUnavailable))
        case v @ yearsAndTransactionPattern(numberOfTransactions) =>
          Ok(Json.toJson(generateSuccessfulResponse(v, numberOfTransactions.toInt, LocalDate.parse(dateFrom), LocalDate.parse(dateTo))))
        case _ => Ok(Json.parse(SuccessfulResponse(idNumber)))
      }
    }
}

object FinancialDataController {

  private val InvalidIdNumber =
    """
      |{
      |   "code": "INVALID_IDNUMBER",
      |   "reason": "Submission has not passed validation. Invalid parameter idNumber."
      |}
      |
      |""".stripMargin

  private val FinancialDataNotFound =
    """
      |{
      |   "code": "NOT_FOUND",
      |   "reason": "The remote endpoint has indicated that no data can be found"
      |}
      |
      |""".stripMargin

  private val FinancialServerError =
    """
      |{
      |   "code": "SERVER_ERROR",
      |   "reason": "DES is currently experiencing problems that require live service intervention"
      |}
      |
      |""".stripMargin

  private val FinancialServiceUnavailable =
    """
      |{
      |   "code": "SERVICE_UNAVAILABLE",
      |   "reason": "Dependent systems are currently not responding"
      |}
      |
      |""".stripMargin

  private def generateSuccessfulResponse(idNumber: String, numberOfTransactions: Int, dateFrom: LocalDate, dateTo: LocalDate) =
    FinancialDataResponse(
      "ZPLR",
      idNumber,
      "PLR",
      LocalDateTime.now,
      financialTransactions = generateFinancialTransactions(numberOfTransactions, dateFrom, dateTo)
    )

  private def generateFinancialTransactions(numberOfExtraTransactions: Int, dateFrom: LocalDate, dateTo: LocalDate): Seq[FinancialTransaction] = {
    val i = numberOfExtraTransactions / 2

    val payments = (0 to i).map(_ =>
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

    val refunds = (0 to i).map(_ =>
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

    payments ++ refunds
  }

  private def generateRandomDate(dateFrom: LocalDate, dateTo: LocalDate): LocalDate =
    LocalDate.ofEpochDay(Random.between(dateFrom.toEpochDay, dateTo.toEpochDay + 1))

  private def generateBigDecimal: BigDecimal = {
    val start = 10000.00
    val end   = 100000000000.00

    BigDecimal.valueOf(Random.between(start, end)).setScale(2, BigDecimal.RoundingMode.HALF_EVEN)

  }

  private val SuccessfulResponse = (idNumber: String) => s"""
       |{
       |  "idType": "ZPLR",
       |  "idNumber": "$idNumber",
       |  "regimeType": "PLR",
       |  "processingDate": "${LocalDateTime.now.toString}",
       |  "financialTransactions": [
       |    {
       |      "chargeType": "Pillar 2 (Payment on Account)",
       |      "mainType": "On Account",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "53",
       |      "contractAccount": "X",
       |      "contractObjectType": "PLR",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "0001",
       |      "mainTransaction": "0060",
       |      "subTransaction": "0400",
       |      "originalAmount": 9999.99,
       |      "outstandingAmount": 9999.99,
       |      "items": [
       |        {
       |          "subItem": "001",
       |          "dueDate": "2024-07-01",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-07-01",
       |          "clearingReason": "Outgoing payment - Paid",
       |          "outgoingPaymentMethod": "BACS Payment out",
       |          "paymentReference": "a",
       |          "paymentAmount": 5000,
       |          "paymentMethod": "FPS Receipts",
       |          "paymentLot": "081203010024",
       |          "paymentLotItem": "000001",
       |          "clearingSAPDocument": "000001"
       |        }
       |      ]
       |    },
       |    {
       |      "chargeType": "Pillar 2 (Payment on Account)",
       |      "mainType": "On Account",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "53",
       |      "contractAccount": "X",
       |      "contractObjectType": "PLR",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "0001",
       |      "mainTransaction": "0060",
       |      "subTransaction": "0400",
       |      "originalAmount": 9999.99,
       |      "outstandingAmount": 9999.99,
       |      "items": [
       |        {
       |          "subItem": "001",
       |          "dueDate": "2024-07-01",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-07-01",
       |          "clearingReason": "Outgoing payment - Paid",
       |          "outgoingPaymentMethod": "BACS Payment out",
       |          "paymentReference": "a",
       |          "paymentAmount": 9999.99,
       |          "paymentMethod": "FPS Receipts",
       |          "paymentLot": "081203010024",
       |          "paymentLotItem": "000001",
       |          "clearingSAPDocument": "000001"
       |        }
       |      ]
       |    },
       |    {
       |      "chargeType": "Pillar 2 (Payment on Account)",
       |      "mainType": "On Account",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "53",
       |      "contractAccount": "X",
       |      "contractObjectType": "PLR",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "0001",
       |      "mainTransaction": "0060",
       |      "subTransaction": "0400",
       |      "originalAmount": 9999.99,
       |      "outstandingAmount": 9999.99,
       |      "items": [
       |        {
       |          "subItem": "001",
       |          "dueDate": "2024-07-01",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-07-01",
       |          "clearingReason": "Outgoing payment - Paid",
       |          "outgoingPaymentMethod": "BACS Payment out",
       |          "paymentReference": "a",
       |          "paymentAmount": 9999.99,
       |          "paymentMethod": "FPS Receipts",
       |          "paymentLot": "081203010024",
       |          "paymentLotItem": "000001",
       |          "clearingSAPDocument": "000001"
       |        }
       |      ]
       |    },
       |    {
       |      "chargeType": "Pillar 2 (Payment on Account)",
       |      "mainType": "On Account",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "53",
       |      "contractAccount": "X",
       |      "contractObjectType": "PLR",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "0001",
       |      "mainTransaction": "0060",
       |      "subTransaction": "0400",
       |      "originalAmount": 9999.99,
       |      "outstandingAmount": 9999.99,
       |      "items": [
       |        {
       |          "subItem": "001",
       |          "dueDate": "2024-07-01",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-07-01",
       |          "clearingReason": "Outgoing payment - Paid",
       |          "outgoingPaymentMethod": "BACS Payment out",
       |          "paymentReference": "a",
       |          "paymentAmount": 9999.99,
       |          "paymentMethod": "FPS Receipts",
       |          "paymentLot": "081203010024",
       |          "paymentLotItem": "000001",
       |          "clearingSAPDocument": "000001"
       |        }
       |      ]
       |    },
       |    {
       |      "chargeType": "Pillar 2 (Payment on Account)",
       |      "mainType": "On Account",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "53",
       |      "contractAccount": "X",
       |      "contractObjectType": "PLR",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "0001",
       |      "mainTransaction": "0060",
       |      "subTransaction": "0400",
       |      "originalAmount": 9999.99,
       |      "outstandingAmount": 9999.99,
       |      "items": [
       |        {
       |          "subItem": "001",
       |          "dueDate": "2024-07-01",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-07-01",
       |          "clearingReason": "Outgoing payment - Paid",
       |          "outgoingPaymentMethod": "BACS Payment out",
       |          "paymentReference": "a",
       |          "paymentAmount": 9999.99,
       |          "paymentMethod": "FPS Receipts",
       |          "paymentLot": "081203010024",
       |          "paymentLotItem": "000001",
       |          "clearingSAPDocument": "000001"
       |        }
       |      ]
       |    }
       |  ]
       |}
       |""".stripMargin

}
