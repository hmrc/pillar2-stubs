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

  // Scenario-specific IDs for Outstanding Payments testing (one per scenario)
  val Scenario1UktrCharges:                     String = "XEPLR2697000001"
  val Scenario2UktrInterestCharges:             String = "XEPLR2697000002"
  val Scenario3DeterminationCharges:            String = "XEPLR2697000003"
  val Scenario4DiscoveryAssessmentCharges:      String = "XEPLR2697000004"
  val Scenario5OverpaidClaimAssessment:         String = "XEPLR2697000005"
  val Scenario7UktrLateFilingPenalties:         String = "XEPLR2697000007"
  val Scenario8OrnGirLateFilingPenalties:       String = "XEPLR2697000008"
  val Scenario9PotentialLostRevenue:            String = "XEPLR2697000009"
  val Scenario10Schedule36:                     String = "XEPLR2697000010"
  val Scenario11RecordKeeping:                  String = "XEPLR2697000011"
  val Scenario12DeterminationWithInterest:      String = "XEPLR2697000012"
  val Scenario13DiscAssmtWithInterest:          String = "XEPLR2697000013"
  val Scenario14OverpaidClaimAssmtWithInterest: String = "XEPLR2697000014"

  private val apStart: LocalDate = LocalDate.of(2024, 1, 1)
  private val apEnd:   LocalDate = LocalDate.of(2024, 12, 31)

  private def debitTransaction(
    desc:            String,
    chargeRefNo:     String,
    dueDate:         LocalDate,
    transactionDate: LocalDate,
    originalAmount:  BigDecimal,
    accruedInterest: Option[BigDecimal] = None,
    outstanding:     BigDecimal,
    clearedAmount:   Option[BigDecimal] = None
  ): AccountActivityTransactionDetail =
    AccountActivityTransactionDetail(
      transactionType = "Debit",
      transactionDesc = desc,
      startDate = Some(apStart),
      endDate = Some(apEnd),
      chargeRefNo = Some(chargeRefNo),
      transactionDate = transactionDate,
      dueDate = Some(dueDate),
      accruedInterest = accruedInterest,
      originalAmount = originalAmount,
      outstandingAmount = Some(outstanding),
      clearedAmount = clearedAmount,
      clearingDetails = clearedAmount.map { cleared =>
        Seq(
          ClearingDetail(
            transactionDesc = desc,
            amount = cleared,
            clearingDate = transactionDate,
            chargeRefNo = Some(chargeRefNo),
            dueDate = Some(dueDate),
            clearingReason = Some("Cleared by Payment")
          )
        )
      }
    )

  private def responseWithDebits(transactions: Seq[AccountActivityTransactionDetail]): AccountActivitySuccessResponse =
    AccountActivitySuccessResponse(
      AccountActivitySuccess(
        processingDate = AccountActivityResponse.now,
        transactionDetails = transactions
      )
    )

  def scenario1UktrCharges(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 UK Tax Return Pillar 2 DTT",
          chargeRefNo = "XDT3456789012",
          dueDate = LocalDate.of(2026, 6, 20),
          transactionDate = LocalDate.of(2026, 6, 15),
          originalAmount = 3100,
          outstanding = 3100
        ),
        debitTransaction(
          desc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
          chargeRefNo = "XII3456789012",
          dueDate = LocalDate.of(2026, 6, 25),
          transactionDate = LocalDate.of(2026, 6, 15),
          originalAmount = 4100,
          outstanding = 2100,
          clearedAmount = Some(2000)
        ),
        debitTransaction(
          desc = "Pillar 2 UK Tax Return Pillar 2 MTT UTPR",
          chargeRefNo = "XUT3456789012",
          dueDate = LocalDate.of(2026, 6, 30),
          transactionDate = LocalDate.of(2026, 6, 15),
          originalAmount = 1500,
          outstanding = 1500
        )
      )
    )

  def scenario2UktrInterestCharges(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 UKTR Interest Pillar 2 DTT Int",
          chargeRefNo = "XIN3456789012",
          dueDate = LocalDate.of(2026, 7, 20),
          transactionDate = LocalDate.of(2026, 7, 15),
          originalAmount = 35,
          outstanding = 35
        ),
        debitTransaction(
          desc = "Pillar 2 UKTR Interest Pillar 2 MTT IIR Int",
          chargeRefNo = "XIN3456789013",
          dueDate = LocalDate.of(2026, 7, 20),
          transactionDate = LocalDate.of(2026, 7, 15),
          originalAmount = 50,
          outstanding = 20,
          clearedAmount = Some(30)
        ),
        debitTransaction(
          desc = "Pillar 2 UKTR Interest Pillar 2 MTT UTPR Int",
          chargeRefNo = "XIN3456789014",
          dueDate = LocalDate.of(2026, 7, 20),
          transactionDate = LocalDate.of(2026, 7, 15),
          originalAmount = 25,
          outstanding = 25
        )
      )
    )

  def scenario3DeterminationCharges(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Determination Pillar 2 DTT",
          chargeRefNo = "XDD3456789012",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 3100,
          outstanding = 3100
        ),
        debitTransaction(
          desc = "Pillar 2 Determination Pillar 2 MTT IIR",
          chargeRefNo = "XDT3456789012",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 3100,
          outstanding = 1100,
          clearedAmount = Some(2000)
        ),
        debitTransaction(
          desc = "Pillar 2 Determination Pillar 2 MTT UTPR",
          chargeRefNo = "XDU3456789012",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 1500,
          outstanding = 1500
        )
      )
    )

  def scenario4DiscoveryAssessmentCharges(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
          chargeRefNo = "XDA3456789012",
          dueDate = LocalDate.of(2026, 9, 30),
          transactionDate = LocalDate.of(2026, 9, 15),
          originalAmount = 3000,
          outstanding = 3000
        ),
        debitTransaction(
          desc = "Pillar 2 Discovery Assessment Pillar 2 MTT IIR",
          chargeRefNo = "XDA3456789013",
          dueDate = LocalDate.of(2026, 9, 30),
          transactionDate = LocalDate.of(2026, 9, 15),
          originalAmount = 2500,
          outstanding = 1500,
          clearedAmount = Some(1000)
        ),
        debitTransaction(
          desc = "Pillar 2 Discovery Assessment Pillar 2 MTT UTPR",
          chargeRefNo = "XDA3456789014",
          dueDate = LocalDate.of(2026, 9, 30),
          transactionDate = LocalDate.of(2026, 9, 15),
          originalAmount = 1200,
          outstanding = 1200
        )
      )
    )

  def scenario5OverpaidClaimAssessmentCharges(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Overpaid Claim Assmt Pillar 2 DTT",
          chargeRefNo = "XOC3456789010",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 4100,
          outstanding = 4100
        ),
        debitTransaction(
          desc = "Pillar 2 Overpaid Claim Assmt Pillar 2 MTT IIR",
          chargeRefNo = "XOC3456789011",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 3000,
          outstanding = 1000,
          clearedAmount = Some(2000)
        ),
        debitTransaction(
          desc = "Pillar 2 Overpaid Claim Assmt Pillar 2 MTT UTPR",
          chargeRefNo = "XOC3456789012",
          dueDate = LocalDate.of(2028, 3, 31),
          transactionDate = LocalDate.of(2027, 2, 15),
          originalAmount = 4100,
          outstanding = 4100
        )
      )
    )

  def scenario7UktrLateFilingPenalties(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 UKTR DTT LFP AUTO PEN",
          chargeRefNo = "XPN3456789012",
          dueDate = LocalDate.of(2026, 7, 31),
          transactionDate = LocalDate.of(2026, 7, 1),
          originalAmount = 100,
          outstanding = 100
        ),
        debitTransaction(
          desc = "Pillar 2 UKTR MTT LFP AUTO PEN",
          chargeRefNo = "XPN3456789013",
          dueDate = LocalDate.of(2026, 7, 31),
          transactionDate = LocalDate.of(2026, 7, 1),
          originalAmount = 200,
          outstanding = 50,
          clearedAmount = Some(150)
        )
      )
    )

  def scenario8OrnGirLateFilingPenalties(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 ORN/GIR DTT LFP AUTO PEN",
          chargeRefNo = "XPN3456789020",
          dueDate = LocalDate.of(2026, 8, 31),
          transactionDate = LocalDate.of(2026, 8, 1),
          originalAmount = 300,
          outstanding = 100,
          clearedAmount = Some(200)
        ),
        debitTransaction(
          desc = "Pillar 2 ORN/GIR MTT LFP AUTO PEN",
          chargeRefNo = "XPN3456789021",
          dueDate = LocalDate.of(2026, 8, 31),
          transactionDate = LocalDate.of(2026, 8, 1),
          originalAmount = 150,
          outstanding = 150
        )
      )
    )

  def scenario9PotentialLostRevenuePenalty(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Poten Lost Rev Pen TG PEN",
          chargeRefNo = "XPL3456789012",
          dueDate = LocalDate.of(2026, 11, 30),
          transactionDate = LocalDate.of(2026, 11, 1),
          originalAmount = 500,
          outstanding = 250,
          clearedAmount = Some(250)
        )
      )
    )

  def scenario10Schedule36(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Sch 36 Penalty TG PEN",
          chargeRefNo = "XS363456789012",
          dueDate = LocalDate.of(2026, 10, 31),
          transactionDate = LocalDate.of(2026, 10, 1),
          originalAmount = 1000,
          outstanding = 1000
        )
      )
    )

  def scenario11RecordKeeping(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Record Keeping Pen TG PEN",
          chargeRefNo = "XRK3456789012",
          dueDate = LocalDate.of(2026, 7, 30),
          transactionDate = LocalDate.of(2026, 6, 30),
          originalAmount = 3500,
          outstanding = 3500
        )
      )
    )

  def scenario12DeterminationWithInterest(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Determination Pillar 2 DTT",
          chargeRefNo = "XDD3456789012",
          dueDate = LocalDate.of(2025, 3, 31),
          transactionDate = LocalDate.of(2025, 2, 15),
          originalAmount = 3100,
          accruedInterest = Some(BigDecimal(100)),
          outstanding = 3100
        )
      )
    )

  def scenario13DiscAssmtWithInterest(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Discovery Assessment Pillar 2 DTT",
          chargeRefNo = "XDD3456789012",
          dueDate = LocalDate.of(2025, 3, 31),
          transactionDate = LocalDate.of(2025, 2, 15),
          originalAmount = 44300,
          accruedInterest = Some(BigDecimal(705)),
          outstanding = 44300
        )
      )
    )

  def scenario14OverpaidClaimAssmtWithInterest(): AccountActivitySuccessResponse =
    responseWithDebits(
      Seq(
        debitTransaction(
          desc = "Pillar 2 Overpaid Claim Assmt Pillar 2 DTT",
          chargeRefNo = "XDD3456789012",
          dueDate = LocalDate.of(2025, 3, 31),
          transactionDate = LocalDate.of(2025, 2, 15),
          originalAmount = 887637,
          accruedInterest = Some(BigDecimal(1994)),
          outstanding = 887637
        )
      )
    )

  def overdueOutstandingCharge(): AccountActivitySuccessResponse = AccountActivitySuccessResponse(
    AccountActivitySuccess(
      processingDate = AccountActivityResponse.now,
      transactionDetails = Seq(
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
          startDate = Some(LocalDate.of(2025, 1, 1)),
          endDate = Some(LocalDate.of(2025, 12, 31)),
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2025, 2, 15),
          dueDate = Some(LocalDate.of(2026, 12, 31)),
          originalAmount = BigDecimal(9000),
          outstandingAmount = Some(BigDecimal(9000)),
          clearedAmount = None,
          clearingDetails = None
        ),
        AccountActivityTransactionDetail(
          transactionType = "Debit",
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 MTT IIR",
          startDate = Some(LocalDate.of(2024, 1, 1)),
          endDate = Some(LocalDate.of(2024, 12, 31)),
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2024, 2, 15),
          dueDate = Some(LocalDate.of(2025, 12, 31)),
          accruedInterest = Some(BigDecimal(425)),
          originalAmount = BigDecimal(6500),
          outstandingAmount = Some(BigDecimal(6500)),
          clearedAmount = None,
          clearingDetails = None
        )
      )
    )
  )

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
        AccountActivityTransactionDetail(
          transactionType = "Payment",
          transactionDesc = "On Account Pillar 2 (Payment on Account)",
          transactionDate = LocalDate.of(2025, 11, 1),
          originalAmount = 500,
          outstandingAmount = Some(0),
          clearedAmount = Some(500),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "Pillar 2 Repayment",
                amount = 500,
                clearingDate = LocalDate.of(2025, 11, 1),
                clearingReason = Some("Outgoing payment - Paid")
              )
            )
          )
        ),
        // Repayment interest
        AccountActivityTransactionDetail(
          transactionType = "Credit",
          transactionDesc = "Pillar 2 UKTR RPI Pillar 2 OECD RPI",
          transactionDate = LocalDate.of(2025, 12, 1),
          originalAmount = 5,
          clearedAmount = Some(5),
          clearingDetails = Some(
            Seq(
              ClearingDetail(
                transactionDesc = "Pillar 2 Repayment",
                amount = 5,
                clearingDate = LocalDate.of(2025, 12, 1),
                clearingReason = Some("Outgoing payment - Paid")
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
          dueDate = Some(LocalDate.of(2026, 12, 31)),
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
          dueDate = Some(LocalDate.of(2026, 12, 31)),
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
          dueDate = Some(LocalDate.of(2026, 12, 31)),
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
          dueDate = Some(LocalDate.of(2026, 12, 31)),
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
          transactionDate = LocalDate.of(2026, 1, 2),
          dueDate = Some(LocalDate.of(2026, 1, 31)),
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
          dueDate = Some(LocalDate.of(2026, 10, 15)),
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
