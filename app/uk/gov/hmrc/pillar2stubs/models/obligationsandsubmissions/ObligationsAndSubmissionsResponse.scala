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

package uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions

import play.api.libs.json.{Json, OFormat, Writes}
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.ObligationsAndSubmissionsErrorCodes.{BAD_REQUEST_400, INTERNAL_SERVER_ERROR_500}
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.ObligationsAndSubmissionsResponse.now

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}

sealed trait ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsResponse {
  def now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)

  implicit val writes: Writes[ObligationsAndSubmissionsResponse] = Writes {
    case s: ObligationsAndSubmissionsSuccessResponse       => Json.obj("success" -> s.success)
    case e: ObligationsAndSubmissionsSimpleErrorResponse   => Json.obj("errors" -> e.error)
    case d: ObligationsAndSubmissionsDetailedErrorResponse => Json.obj("errors" -> d.errors)
  }
}

case class ObligationsAndSubmissionsSuccessResponse(success: ObligationsAndSubmissionsSuccess) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsSuccessResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsSuccessResponse] = Json.format[ObligationsAndSubmissionsSuccessResponse]

  // Default single accounting period
  def apply(): ObligationsAndSubmissionsSuccessResponse = ObligationsAndSubmissionsSuccessResponse(
    ObligationsAndSubmissionsSuccess(
      processingDate = now,
      accountingPeriodDetails = Seq(
        AccountingPeriodDetails(
          startDate = LocalDate.of(2024, 1, 1),
          endDate = LocalDate.of(2024, 12, 31),
          dueDate = LocalDate.of(2025, 1, 31),
          underEnquiry = false,
          obligations = Seq(
            Obligation(
              obligationType = ObligationType.Pillar2TaxReturn,
              status = ObligationStatus.Open,
              canAmend = true,
              submissions = Seq(Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None))
            )
          )
        )
      )
    )
  )

  // Multiple accounting periods (4 periods with different characteristics)
  def withMultipleAccountingPeriods(): ObligationsAndSubmissionsSuccessResponse = {
    val now = ObligationsAndSubmissionsResponse.now

    ObligationsAndSubmissionsSuccessResponse(
      ObligationsAndSubmissionsSuccess(
        processingDate = now,
        accountingPeriodDetails = Seq(
          AccountingPeriodDetails(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31),
            dueDate = LocalDate.of(2026, 1, 31),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Open,
                canAmend = true,
                submissions = Seq(
                  Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None),
                  Submission(submissionType = SubmissionType.ORN, receivedDate = now, country = None)
                )
              ),
              Obligation(
                obligationType = ObligationType.GlobeInformationReturn,
                status = ObligationStatus.Open,
                canAmend = true,
                submissions = Seq(
                  Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None),
                  Submission(submissionType = SubmissionType.ORN, receivedDate = now, country = None)
                )
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2023, 1, 1),
            endDate = LocalDate.of(2023, 12, 31),
            dueDate = LocalDate.of(2026, 1, 31),
            underEnquiry = true,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Open,
                canAmend = false,
                submissions = Seq(Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None))
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2022, 1, 1),
            endDate = LocalDate.of(2022, 12, 31),
            dueDate = LocalDate.of(2023, 1, 31),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.GlobeInformationReturn,
                status = ObligationStatus.Open,
                canAmend = false,
                submissions = Seq(Submission(submissionType = SubmissionType.GIR, receivedDate = now, country = None))
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2021, 1, 1),
            endDate = LocalDate.of(2021, 12, 31),
            dueDate = LocalDate.of(2022, 1, 31),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Open,
                canAmend = true,
                submissions = Seq(
                  Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None),
                  Submission(submissionType = SubmissionType.BTN, receivedDate = now, country = Some("FR"))
                )
              )
            )
          )
        )
      )
    )
  }

  // No accounting periods
  def withNoAccountingPeriods(): ObligationsAndSubmissionsSuccessResponse =
    ObligationsAndSubmissionsSuccessResponse(
      ObligationsAndSubmissionsSuccess(
        processingDate = ObligationsAndSubmissionsResponse.now,
        accountingPeriodDetails = Seq.empty
      )
    )

  // All obligations fulfilled
  def withAllFuffilled(): ObligationsAndSubmissionsSuccessResponse =
    ObligationsAndSubmissionsSuccessResponse(
      ObligationsAndSubmissionsSuccess(
        processingDate = ObligationsAndSubmissionsResponse.now,
        accountingPeriodDetails = Seq(
          AccountingPeriodDetails(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 12, 31),
            dueDate = LocalDate.of(2025, 1, 31),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Fulfilled,
                canAmend = false,
                submissions = Seq(Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None))
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2023, 1, 1),
            endDate = LocalDate.of(2023, 12, 31),
            dueDate = LocalDate.of(2024, 1, 31),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Fulfilled,
                canAmend = false,
                submissions = Seq(Submission(submissionType = SubmissionType.UKTR, receivedDate = now, country = None))
              )
            )
          )
        )
      )
    )

  //submission history containing multiple submission periods
  def submissionHistoryMultiplePeriods(): ObligationsAndSubmissionsSuccessResponse =
    ObligationsAndSubmissionsSuccessResponse(
      ObligationsAndSubmissionsSuccess(
        processingDate = ObligationsAndSubmissionsResponse.now,
        accountingPeriodDetails = Seq(
          AccountingPeriodDetails(
            startDate = LocalDate.of(2023, 2, 15),
            endDate = LocalDate.of(2024, 2, 16),
            dueDate = LocalDate.of(2025, 2, 16),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Fulfilled,
                canAmend = false,
                submissions = Seq(
                  Submission(submissionType = SubmissionType.GIR, receivedDate = now.withYear(2024).withMonth(11).withDayOfMonth(21), country = None),
                  Submission(
                    submissionType = SubmissionType.UKTR,
                    receivedDate = now.withYear(2024).withMonth(11).withDayOfMonth(21),
                    country = None
                  ),
                  Submission(submissionType = SubmissionType.BTN, receivedDate = now.withYear(2025).withMonth(1).withDayOfMonth(10), country = None)
                )
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2022, 2, 15),
            endDate = LocalDate.of(2023, 2, 16),
            dueDate = LocalDate.of(2024, 2, 16),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Fulfilled,
                canAmend = false,
                submissions = Seq(
                  Submission(
                    submissionType = SubmissionType.UKTR,
                    receivedDate = now.withYear(2023).withMonth(11).withDayOfMonth(21),
                    country = None
                  ),
                  Submission(submissionType = SubmissionType.ORN, receivedDate = now.withYear(2023).withMonth(11).withDayOfMonth(19), country = None)
                )
              )
            )
          ),
          AccountingPeriodDetails(
            startDate = LocalDate.of(2021, 2, 15),
            endDate = LocalDate.of(2022, 2, 16),
            dueDate = LocalDate.of(2023, 2, 16),
            underEnquiry = false,
            obligations = Seq(
              Obligation(
                obligationType = ObligationType.Pillar2TaxReturn,
                status = ObligationStatus.Fulfilled,
                canAmend = false,
                submissions = Seq(
                  Submission(
                    submissionType = SubmissionType.UKTR,
                    receivedDate = now.withYear(2022).withMonth(11).withDayOfMonth(21),
                    country = None
                  ),
                  Submission(submissionType = SubmissionType.GIR, receivedDate = now.withYear(2022).withMonth(11).withDayOfMonth(21), country = None)
                )
              )
            )
          )
        )
      )
    )

  //submission history containing one submission period
  def submissionHistorySingularPeriod(): ObligationsAndSubmissionsSuccessResponse = ObligationsAndSubmissionsSuccessResponse(
    ObligationsAndSubmissionsSuccess(
      processingDate = now,
      accountingPeriodDetails = Seq(
        AccountingPeriodDetails(
          startDate = LocalDate.of(2023, 9, 28),
          endDate = LocalDate.of(2024, 9, 27),
          dueDate = LocalDate.of(2025, 9, 27),
          underEnquiry = false,
          obligations = Seq(
            Obligation(
              obligationType = ObligationType.Pillar2TaxReturn,
              status = ObligationStatus.Open,
              canAmend = true,
              submissions = Seq(
                Submission(submissionType = SubmissionType.GIR, receivedDate = now.withYear(2025).withMonth(5).withDayOfMonth(10), country = None),
                Submission(submissionType = SubmissionType.UKTR, receivedDate = now.withYear(2025).withMonth(5).withDayOfMonth(10), country = None)
              )
            )
          )
        )
      )
    )
  )

  //Success response with no submissions
  def noSubmissionHistory(): ObligationsAndSubmissionsSuccessResponse = ObligationsAndSubmissionsSuccessResponse(
    ObligationsAndSubmissionsSuccess(
      processingDate = now,
      accountingPeriodDetails = Seq(
        AccountingPeriodDetails(
          startDate = LocalDate.of(2025, 1, 1),
          endDate = LocalDate.of(2025, 12, 31),
          dueDate = LocalDate.of(2026, 1, 31),
          underEnquiry = false,
          obligations = Seq(
            Obligation(
              obligationType = ObligationType.Pillar2TaxReturn,
              status = ObligationStatus.Open,
              canAmend = true,
              submissions = Seq.empty
            )
          )
        )
      )
    )
  )
}

case class ObligationsAndSubmissionsSuccess(processingDate: ZonedDateTime, accountingPeriodDetails: Seq[AccountingPeriodDetails])

object ObligationsAndSubmissionsSuccess {
  implicit val format: OFormat[ObligationsAndSubmissionsSuccess] = Json.format[ObligationsAndSubmissionsSuccess]
}

case class AccountingPeriodDetails(
  startDate:    LocalDate,
  endDate:      LocalDate,
  dueDate:      LocalDate,
  underEnquiry: Boolean,
  obligations:  Seq[Obligation]
)

object AccountingPeriodDetails {
  implicit val format: OFormat[AccountingPeriodDetails] = Json.format[AccountingPeriodDetails]
}

case class ObligationsAndSubmissionsSimpleErrorResponse(error: ObligationsAndSubmissionsSimpleError) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsSimpleErrorResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsSimpleErrorResponse] = Json.format[ObligationsAndSubmissionsSimpleErrorResponse]

  def InvalidJsonError(errorMessage: String = "Invalid JSON"): ObligationsAndSubmissionsSimpleErrorResponse =
    ObligationsAndSubmissionsSimpleErrorResponse(
      ObligationsAndSubmissionsSimpleError(
        code = BAD_REQUEST_400,
        message = errorMessage,
        logID = "C0000000000000000000000000000400"
      )
    )

  lazy val SAPError: ObligationsAndSubmissionsSimpleErrorResponse =
    ObligationsAndSubmissionsSimpleErrorResponse(
      ObligationsAndSubmissionsSimpleError(
        code = INTERNAL_SERVER_ERROR_500,
        message = "Internal server error",
        logID = "C0000000000000000000000000000500"
      )
    )
}

case class ObligationsAndSubmissionsSimpleError(code: String, message: String, logID: String)

object ObligationsAndSubmissionsSimpleError {
  implicit val format: OFormat[ObligationsAndSubmissionsSimpleError] = Json.format[ObligationsAndSubmissionsSimpleError]
}

case class ObligationsAndSubmissionsDetailedErrorResponse(errors: ObligationsAndSubmissionsDetailedError) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsDetailedErrorResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsDetailedErrorResponse] = Json.format[ObligationsAndSubmissionsDetailedErrorResponse]

  def apply(errorCode: (String, String)): ObligationsAndSubmissionsDetailedErrorResponse = ObligationsAndSubmissionsDetailedErrorResponse(
    ObligationsAndSubmissionsDetailedError(
      processingDate = now,
      code = errorCode._1,
      text = errorCode._2
    )
  )

  lazy val invalidDateRange: ObligationsAndSubmissionsDetailedErrorResponse = ObligationsAndSubmissionsDetailedErrorResponse(
    ObligationsAndSubmissionsDetailedError(
      processingDate = now,
      code = "001",
      text = "Invalid date range: toDate must be after fromDate"
    )
  )
}

case class ObligationsAndSubmissionsDetailedError(processingDate: ZonedDateTime, code: String, text: String)

object ObligationsAndSubmissionsDetailedError {
  implicit val format: OFormat[ObligationsAndSubmissionsDetailedError] = Json.format[ObligationsAndSubmissionsDetailedError]
}

object ObligationsAndSubmissionsErrorCodes {
  val PILLAR_2_ID_MISSING_OR_INVALID_002: (String, String) = ("002", "Pillar2 ID is missing or invalid")
  val REQUEST_COULD_NOT_BE_PROCESSED_003: (String, String) = ("003", "Request could not be processed or invalid")
  val DUPLICATE_SUBMISSION_004:           (String, String) = ("004", "Duplicate Submission")
  val NO_DATA_FOUND_025:                  (String, String) = ("025", "No associated data found")
  val BAD_REQUEST_400           = "400"
  val INTERNAL_SERVER_ERROR_500 = "500"
}
