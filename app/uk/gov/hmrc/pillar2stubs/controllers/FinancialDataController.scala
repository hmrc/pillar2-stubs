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

  def retrieveFinancialData(idNumber: String, dateFrom: String, dateTo: String): Action[AnyContent] =
    (Action andThen authFilter) { _ =>
      val yearsAndTransactionPattern: Regex = "XMPLR0000000(.{3})\\s*$".r

      idNumber match {
        case "XEPLR4000000000" => BadRequest(Json.parse(InvalidIdNumber))
        case "XEPLR4040000000" => NotFound(Json.parse(FinancialDataNotFound))
        case "XEPLR5000000000" => InternalServerError(Json.parse(FinancialServerError))
        case "XEPLR5030000000" => ServiceUnavailable(Json.parse(FinancialServiceUnavailable))
        case "XEPLR2000000000" => Ok(Json.parse(etmpTestDataFull(idNumber)))
        case "XEPLR2000000001" => Ok(Json.parse(oneAp(idNumber)))
        case "XEPLR2000000002" => Ok(Json.parse(twoAps(idNumber)))
        case "XEPLR2000000003" => Ok(Json.parse(overdueUktr(idNumber)))
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
       |          "dueDate": "2024-03-03",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-03-03",
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
       |          "dueDate": "2023-10-22",
       |          "amount": -9999.99,
       |          "clearingDate": "2023-10-22",
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
       |          "dueDate": "2024-08-15",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-08-15",
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
       |          "dueDate": "2024-06-30",
       |          "amount": -9999.99,
       |          "clearingDate": "2024-06-30",
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
       |          "dueDate": "2023-12-10",
       |          "amount": -9999.99,
       |          "clearingDate": "2023-12-10",
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

  private val etmpTestDataFull = (idNumber: String) => s"""
    |{
    |  "idType":"ZPLR",
    |  "idNumber":"$idNumber",
    |  "regimeType":"PLR",
    |  "processingDate":"${LocalDateTime.now.toString}",
    |  "financialTransactions":[
    |    {
    |      "chargeType":"Pillar 2 DTT",
    |      "mainType":"OECD Pillar 2 UK Tax Return",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"003540024920",
    |      "sapDocumentNumberItem":"0001",
    |      "chargeReference":"XD002610233120",
    |      "mainTransaction":"6500",
    |      "subTransaction":"6233",
    |      "originalAmount":2000.0,
    |      "clearedAmount":2000.0,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-01",
    |          "amount":2000.0,
    |          "clearingDate":"2025-07-14",
    |          "clearingReason":"Incoming Payment",
    |          "paymentReference":"XD002610233120",
    |          "paymentAmount":9000.0,
    |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
    |          "paymentLot":"C00125",
    |          "paymentLotItem":"000001",
    |          "clearingSAPDocument":"294000000145"
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 MTT IIR",
    |      "mainType":"OECD Pillar 2 UK Tax Return",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"003540024920",
    |      "sapDocumentNumberItem":"0002",
    |      "chargeReference":"XD002610233120",
    |      "mainTransaction":"6500",
    |      "subTransaction":"6234",
    |      "originalAmount":2000.0,
    |      "clearedAmount":2000.0,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-01",
    |          "amount":2000.0,
    |          "clearingDate":"2025-07-14",
    |          "clearingReason":"Incoming Payment",
    |          "paymentReference":"XD002610233120",
    |          "paymentAmount":9000.0,
    |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
    |          "paymentLot":"C00125",
    |          "paymentLotItem":"000001",
    |          "clearingSAPDocument":"294000000145"
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 MTT UTPR",
    |      "mainType":"OECD Pillar 2 UK Tax Return",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"003540024920",
    |      "sapDocumentNumberItem":"0003",
    |      "chargeReference":"XD002610233120",
    |      "mainTransaction":"6500",
    |      "subTransaction":"6235",
    |      "originalAmount":4000.0,
    |      "clearedAmount":4000.0,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-01",
    |          "amount":4000.0,
    |          "clearingDate":"2025-07-14",
    |          "clearingReason":"Incoming Payment",
    |          "paymentReference":"XD002610233120",
    |          "paymentAmount":9000.0,
    |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
    |          "paymentLot":"C00125",
    |          "paymentLotItem":"000001",
    |          "clearingSAPDocument":"294000000145"
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 (Payment on Account)",
    |      "mainType":"On Account",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"294000000145",
    |      "sapDocumentNumberItem":"0001",
    |      "mainTransaction":"0060",
    |      "subTransaction":"0400",
    |      "originalAmount":-1000.0,
    |      "outstandingAmount":-1000.0,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-14",
    |          "amount":-1000.0,
    |          "paymentReference":"XD002610233120",
    |          "paymentAmount":9000.0,
    |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
    |          "paymentLot":"C00125",
    |          "paymentLotItem":"000001"
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 DTT Interest",
    |      "mainType":"Pillar 2 UKTR Interest",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"428000000021",
    |      "sapDocumentNumberItem":"0001",
    |      "chargeReference":"XY428000000021",
    |      "mainTransaction":"6503",
    |      "subTransaction":"6236",
    |      "originalAmount":5.4,
    |      "outstandingAmount":5.4,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-14",
    |          "amount":5.4
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 MTT IIR Interest",
    |      "mainType":"Pillar 2 UKTR Interest",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"428000000021",
    |      "sapDocumentNumberItem":"0002",
    |      "chargeReference":"XY428000000021",
    |      "mainTransaction":"6503",
    |      "subTransaction":"6238",
    |      "originalAmount":5.4,
    |      "outstandingAmount":5.4,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-14",
    |          "amount":5.4
    |        }
    |      ]
    |    },
    |    {
    |      "chargeType":"Pillar 2 MTT UTPR Interest",
    |      "mainType":"Pillar 2 UKTR Interest",
    |      "taxPeriodFrom":"2023-01-01",
    |      "taxPeriodTo":"2023-12-31",
    |      "businessPartner":"0100007961",
    |      "contractAccountCategory":"53",
    |      "contractAccount":"002100001302",
    |      "contractObjectType":"PLR",
    |      "contractObject":"00000300000000000682",
    |      "sapDocumentNumber":"428000000021",
    |      "sapDocumentNumberItem":"0003",
    |      "chargeReference":"XY428000000021",
    |      "mainTransaction":"6503",
    |      "subTransaction":"6239",
    |      "originalAmount":10.81,
    |      "outstandingAmount":10.81,
    |      "items":[
    |        {
    |          "subItem":"000",
    |          "dueDate":"2025-07-14",
    |          "amount":10.81
    |        }
    |      ]
    |    }
    |  ]
    |}
    |""".stripMargin

  private val twoAps = (idNumber: String) => s"""
     |{
     |  "idType":"ZPLR",
     |  "idNumber":"$idNumber",
     |  "regimeType":"PLR",
     |  "processingDate":"${LocalDateTime.now.toString}",
     |  "financialTransactions":[
     |    {
     |      "chargeType":"Pillar 2 DTT",
     |      "mainType":"OECD Pillar 2 UK Tax Return",
     |      "taxPeriodFrom":"2025-01-01",
     |      "taxPeriodTo":"2025-12-31",
     |      "businessPartner":"0100007961",
     |      "contractAccountCategory":"53",
     |      "contractAccount":"002100001302",
     |      "contractObjectType":"PLR",
     |      "contractObject":"00000300000000000682",
     |      "sapDocumentNumber":"003540024920",
     |      "sapDocumentNumberItem":"0001",
     |      "chargeReference":"XD002610233120",
     |      "mainTransaction":"6500",
     |      "subTransaction":"6233",
     |      "originalAmount":200000.8,
     |      "outstandingAmount":100000.8,
     |      "clearedAmount":100000.0,
     |      "items":[
     |        {
     |          "subItem":"000",
     |          "dueDate":"2026-10-01",
     |          "amount":100000.8
     |        },
     |        {
     |          "subItem":"001",
     |          "dueDate":"2026-10-01",
     |          "amount":100000.0,
     |          "clearingDate":"2026-10-14",
     |          "clearingReason":"Incoming Payment",
     |          "paymentReference":"XD002610233120",
     |          "paymentAmount":900000.0,
     |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
     |          "paymentLot":"C00125",
     |          "paymentLotItem":"000001",
     |          "clearingSAPDocument":"294000000145"
     |        }
     |      ]
     |    },
     |    {
     |      "chargeType":"Pillar 2 MTT IIR",
     |      "mainType":"OECD Pillar 2 UK Tax Return",
     |      "taxPeriodFrom":"2024-01-01",
     |      "taxPeriodTo":"2024-12-31",
     |      "businessPartner":"0100007961",
     |      "contractAccountCategory":"53",
     |      "contractAccount":"002100001302",
     |      "contractObjectType":"PLR",
     |      "contractObject":"00000300000000000682",
     |      "sapDocumentNumber":"003540024920",
     |      "sapDocumentNumberItem":"0002",
     |      "chargeReference":"XD002610233120",
     |      "mainTransaction":"6500",
     |      "subTransaction":"6234",
     |      "originalAmount":200000.3,
     |      "outstandingAmount":100000.3,
     |      "clearedAmount":100000.0,
     |      "items":[
     |        {
     |          "subItem":"000",
     |          "dueDate":"2025-10-01",
     |          "amount":100000.3
     |        },
     |        {
     |          "subItem":"000",
     |          "dueDate":"2025-10-01",
     |          "amount":100000.0,
     |          "clearingDate":"2025-10-14",
     |          "clearingReason":"Incoming Payment",
     |          "paymentReference":"XD002610233120",
     |          "paymentAmount":900000.0,
     |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
     |          "paymentLot":"C00125",
     |          "paymentLotItem":"000001",
     |          "clearingSAPDocument":"294000000145"
     |        }
     |      ]
     |    }
     |  ]
     |}
      |""".stripMargin

  private val oneAp = (idNumber: String) => s"""
     |{
     |  "idType":"ZPLR",
     |  "idNumber":"$idNumber",
     |  "regimeType":"PLR",
     |  "processingDate":"${LocalDateTime.now.toString}",
     |  "financialTransactions":[
     |    {
     |      "chargeType":"Pillar 2 MTT IIR",
     |      "mainType":"OECD Pillar 2 UK Tax Return",
     |      "taxPeriodFrom":"2024-01-01",
     |      "taxPeriodTo":"2024-12-31",
     |      "businessPartner":"0100007961",
     |      "contractAccountCategory":"53",
     |      "contractAccount":"002100001302",
     |      "contractObjectType":"PLR",
     |      "contractObject":"00000300000000000682",
     |      "sapDocumentNumber":"003540024920",
     |      "sapDocumentNumberItem":"0002",
     |      "chargeReference":"XD002610233120",
     |      "mainTransaction":"6500",
     |      "subTransaction":"6234",
     |      "originalAmount":200000.3,
     |      "outstandingAmount":100000.3,
     |      "clearedAmount":100000.0,
     |      "items":[
     |        {
     |          "subItem":"000",
     |          "dueDate":"2025-10-01",
     |          "amount":100000.3
     |        },
     |        {
     |          "subItem":"000",
     |          "dueDate":"2025-10-01",
     |          "amount":100000.0,
     |          "clearingDate":"2025-10-14",
     |          "clearingReason":"Incoming Payment",
     |          "paymentReference":"XD002610233120",
     |          "paymentAmount":900000.0,
     |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
     |          "paymentLot":"C00125",
     |          "paymentLotItem":"000001",
     |          "clearingSAPDocument":"294000000145"
     |        }
     |      ]
     |    }
     |  ]
     |}
      |""".stripMargin

  private val overdueUktr = (idNumber: String) => s"""
      |{
      |  "idType":"ZPLR",
      |  "idNumber":"$idNumber",
      |  "regimeType":"PLR",
      |  "processingDate":"${LocalDateTime.now.toString}",
      |  "financialTransactions":[
      |    {
      |      "chargeType":"Pillar 2 MTT IIR",
      |      "mainType":"OECD Pillar 2 UK Tax Return",
      |      "taxPeriodFrom":"2024-01-01",
      |      "taxPeriodTo":"2024-12-31",
      |      "businessPartner":"0100007961",
      |      "contractAccountCategory":"53",
      |      "contractAccount":"002100001302",
      |      "contractObjectType":"PLR",
      |      "contractObject":"00000300000000000682",
      |      "sapDocumentNumber":"003540024920",
      |      "sapDocumentNumberItem":"0002",
      |      "chargeReference":"XD002610233120",
      |      "mainTransaction":"6500",
      |      "subTransaction":"6234",
      |      "originalAmount":200000.3,
      |      "outstandingAmount":100000.3,
      |      "clearedAmount":100000.0,
      |      "items":[
      |        {
      |          "subItem":"000",
      |          "dueDate":"2024-10-01",
      |          "amount":100000.3
      |        },
      |        {
      |          "subItem":"000",
      |          "dueDate":"2024-10-01",
      |          "amount":100000.0,
      |          "clearingDate":"2024-10-14",
      |          "clearingReason":"Incoming Payment",
      |          "paymentReference":"XD002610233120",
      |          "paymentAmount":900000.0,
      |          "paymentMethod":"PAYMENTS MADE BY CHEQUE",
      |          "paymentLot":"C00125",
      |          "paymentLotItem":"000001",
      |          "clearingSAPDocument":"294000000145"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

}
