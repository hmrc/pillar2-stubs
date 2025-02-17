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
