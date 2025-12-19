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

package uk.gov.hmrc.pillar2stubs.models.accountactivity

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}

sealed trait AccountActivityResponse

object AccountActivityResponse {
  def now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)

  given Writes[AccountActivityResponse] = Writes {
    case s: AccountActivitySuccessResponse  => Json.obj("success" -> s.success)
    case e: AccountActivityErrorResponse    => Json.obj("error" -> e.error)
    case d: AccountActivity422ErrorResponse => Json.obj("errors" -> d.errors)
  }
}

case class AccountActivitySuccessResponse(success: AccountActivitySuccess) extends AccountActivityResponse

object AccountActivitySuccessResponse {
  given OFormat[AccountActivitySuccessResponse] = Json.format[AccountActivitySuccessResponse]

  def apply(): AccountActivitySuccessResponse = AccountActivitySuccessResponse(
    AccountActivitySuccess(
      processingDate = AccountActivityResponse.now,
      transactionDetails = Seq(
        // Payment transaction
        AccountActivityTransactionDetail(
          transactionType = "Payment",
          transactionDesc = "On Account Pillar 2 (Payment on Account)",
          transactionDate = LocalDate.of(2025, 10, 15),
          originalAmount = 10000,
          outstandingAmount = Some(1000),
          clearedAmount = Some(9000),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                chargeRefNo = Some("X123456789012"),
                dueDate = Some(LocalDate.of(2025, 12, 31)),
                clearingReason = Some("Allocated to Charge")
              ),
              ClearingDetail(
                transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                chargeRefNo = Some("X123456789012"),
                dueDate = Some(LocalDate.of(2025, 12, 31)),
                clearingReason = Some("Allocated to Charge")
              ),
              ClearingDetail(
                transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT UTPR",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                chargeRefNo = Some("X123456789012"),
                dueDate = Some(LocalDate.of(2025, 12, 31)),
                clearingReason = Some("Allocated to Charge")
              ),
              ClearingDetail(
                transactionDesc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
                amount = 3000,
                clearingDate = LocalDate.of(2025, 10, 15),
                chargeRefNo = Some("X123456789012"),
                dueDate = Some(LocalDate.of(2025, 12, 31)),
                clearingReason = Some("Allocated to Charge")
              )
            )
          )
        ),
        // Debit transactions
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
          startDate = Some(LocalDate.of(2025, 1, 1)),
          endDate = Some(LocalDate.of(2025, 12, 31)),
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2025, 2, 15),
          dueDate = Some(LocalDate.of(2025, 12, 31)),
          originalAmount = 2000,
          clearedAmount = Some(2000),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "On Account Pillar 2 (Payment on Account)",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                clearingReason = Some("Cleared by Payment")
              )
            )
          )
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
          startDate = Some(LocalDate.of(2025, 1, 1)),
          endDate = Some(LocalDate.of(2025, 12, 31)),
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2025, 2, 15),
          dueDate = Some(LocalDate.of(2025, 12, 31)),
          originalAmount = 2000,
          clearedAmount = Some(2000),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "On Account Pillar 2 (Payment on Account)",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                clearingReason = Some("Cleared by Payment")
              )
            )
          )
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT UTPR",
          startDate = Some(LocalDate.of(2025, 1, 1)),
          endDate = Some(LocalDate.of(2025, 12, 31)),
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2025, 2, 15),
          dueDate = Some(LocalDate.of(2025, 12, 31)),
          originalAmount = 2000,
          clearedAmount = Some(2000),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "On Account Pillar 2 (Payment on Account)",
                amount = 2000,
                clearingDate = LocalDate.of(2025, 10, 15),
                clearingReason = Some("Cleared by Payment")
              )
            )
          )
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
          startDate = Some(LocalDate.of(2025, 1, 1)),
          endDate = Some(LocalDate.of(2025, 12, 31)),
          chargeRefNo = Some("XD23456789012"),
          transactionDate = LocalDate.of(2025, 2, 15),
          dueDate = Some(LocalDate.of(2025, 12, 31)),
          originalAmount = 3000,
          clearedAmount = Some(3000),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "On Account Pillar 2 (Payment on Account)",
                amount = 3000,
                clearingDate = LocalDate.of(2025, 10, 15),
                clearingReason = Some("Cleared by Payment")
              )
            )
          )
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 Determination Pillar 2 MTT IIR",
          startDate = Some(LocalDate.of(2026, 1, 1)),
          endDate = Some(LocalDate.of(2026, 12, 31)),
          accruedInterest = Some(35),
          chargeRefNo = Some("XDT3456789012"),
          transactionDate = LocalDate.of(2027, 2, 15),
          dueDate = Some(LocalDate.of(2028, 3, 31)),
          originalAmount = 3100,
          outstandingAmount = Some(3100)
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 Overpaid Claim Assmt Pillar 2 MTT UTPR",
          startDate = Some(LocalDate.of(2026, 1, 1)),
          endDate = Some(LocalDate.of(2026, 12, 31)),
          chargeRefNo = Some("XOC3456789012"),
          transactionDate = LocalDate.of(2027, 2, 15),
          dueDate = Some(LocalDate.of(2028, 3, 31)),
          originalAmount = 4100,
          outstandingAmount = Some(4100),
          standOverAmount = Some(4100),
          appealFlag = Some(true)
        ),
        // Credit transaction
        AccountActivityTransactionDetail(
          transactionType = "Credit",
          transactionDesc = "Pillar 2 UKTR RPI Pillar 2 OECD RPI",
          chargeRefNo = Some("XR23456789012"),
          transactionDate = LocalDate.of(2025, 3, 15),
          originalAmount = -100,
          outstandingAmount = Some(-100)
        ),
        // Penalty and Interest transactions
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UKTR DTT LFP AUTO PEN",
          startDate = Some(LocalDate.of(2024, 1, 1)),
          endDate = Some(LocalDate.of(2024, 12, 31)),
          chargeRefNo = Some("XPN3456789012"),
          transactionDate = LocalDate.of(2026, 7, 1),
          dueDate = Some(LocalDate.of(2026, 7, 31)),
          originalAmount = 100,
          outstandingAmount = Some(100)
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UKTR Interest Pillar 2 DTT Int",
          startDate = Some(LocalDate.of(2024, 1, 1)),
          endDate = Some(LocalDate.of(2024, 12, 31)),
          chargeRefNo = Some("XIN3456789012"),
          transactionDate = LocalDate.of(2025, 10, 15),
          dueDate = Some(LocalDate.of(2025, 10, 15)),
          originalAmount = 35,
          outstandingAmount = Some(35)
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 Record Keeping Pen TG PEN",
          chargeRefNo = Some("XIN3456789012"),
          transactionDate = LocalDate.of(2026, 6, 30),
          dueDate = Some(LocalDate.of(2026, 7, 30)),
          originalAmount = 3500,
          outstandingAmount = Some(3500)
        )
      )
    )
  )
}

case class AccountActivitySuccess(
  processingDate:     ZonedDateTime,
  transactionDetails: Seq[AccountActivityTransactionDetail]
)

object AccountActivitySuccess {
  given OFormat[AccountActivitySuccess] = Json.format[AccountActivitySuccess]
}

case class AccountActivityTransactionDetail(
  transactionType:   String,
  transactionDesc:   String,
  transactionDate:   LocalDate,
  originalAmount:    BigDecimal,
  startDate:         Option[LocalDate] = None,
  endDate:           Option[LocalDate] = None,
  accruedInterest:   Option[BigDecimal] = None,
  chargeRefNo:       Option[String] = None,
  dueDate:           Option[LocalDate] = None,
  outstandingAmount: Option[BigDecimal] = None,
  clearedAmount:     Option[BigDecimal] = None,
  standOverAmount:   Option[BigDecimal] = None,
  appealFlag:        Option[Boolean] = None,
  clearingDetails:   Option[Seq[ClearingDetail]] = None
)

object AccountActivityTransactionDetail {
  given OFormat[AccountActivityTransactionDetail] = Json.format[AccountActivityTransactionDetail]
}

case class ClearingDetail(
  transactionDesc: String,
  amount:          BigDecimal,
  clearingDate:    LocalDate,
  chargeRefNo:     Option[String] = None,
  dueDate:         Option[LocalDate] = None,
  clearingReason:  Option[String] = None
)

object ClearingDetail {
  given OFormat[ClearingDetail] = Json.format[ClearingDetail]
}

case class AccountActivityErrorResponse(error: AccountActivityError) extends AccountActivityResponse

object AccountActivityErrorResponse {
  given OFormat[AccountActivityErrorResponse] = Json.format[AccountActivityErrorResponse]

  val badRequest: AccountActivityErrorResponse = AccountActivityErrorResponse(
    AccountActivityError(
      code = "400",
      message = "Bad Request",
      logID = "1D43D17801EBCC4C4EAB8974C05448D9"
    )
  )

  val internalServerError: AccountActivityErrorResponse = AccountActivityErrorResponse(
    AccountActivityError(
      code = "500",
      message = "Internal Server Error",
      logID = "1D43D17801EBCC4C4EAB8974C05448D9"
    )
  )
}

case class AccountActivityError(code: String, message: String, logID: String)

object AccountActivityError {
  given OFormat[AccountActivityError] = Json.format[AccountActivityError]
}

case class AccountActivity422ErrorResponse(errors: AccountActivity422Error) extends AccountActivityResponse

object AccountActivity422ErrorResponse {
  given OFormat[AccountActivity422ErrorResponse] = Json.format[AccountActivity422ErrorResponse]

  def apply(errorCode: (String, String)): AccountActivity422ErrorResponse = AccountActivity422ErrorResponse(
    AccountActivity422Error(
      processingDate = AccountActivityResponse.now,
      code = errorCode._1,
      text = errorCode._2
    )
  )
}

case class AccountActivity422Error(processingDate: ZonedDateTime, code: String, text: String)

object AccountActivity422Error {
  given OFormat[AccountActivity422Error] = Json.format[AccountActivity422Error]
}

object AccountActivityErrorCodes {
  val REGIME_MISSING_OR_INVALID_001:      (String, String) = ("001", "REGIME missing or invalid")
  val REQUEST_COULD_NOT_BE_PROCESSED_003: (String, String) = ("003", "Request could not be processed")
  val NO_DATA_FOUND_014:                  (String, String) = ("014", "No data found")
  val ID_NUMBER_MISSING_OR_INVALID_089:   (String, String) = ("089", "ID number missing or invalid")
}
