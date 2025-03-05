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

package uk.gov.hmrc.pillar2stubs.models

import play.api.libs.json.{JsObject, Json, OFormat}

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZonedDateTime}

case class UKTRSubmissionResponse(success: UKTRSubmissionSuccess)

object UKTRSubmissionResponse {
  implicit val format: OFormat[UKTRSubmissionResponse] = Json.format[UKTRSubmissionResponse]

  def successfulLiabilityResponse(implicit clock: Clock): JsObject =
    successfulNilResponse.deepMerge(Json.obj("success" -> Json.obj("chargeReference" -> "XTC01234123412")))

  def successfulNilResponse(implicit clock: Clock): JsObject = Json.obj(
    "success" -> Json.obj("processingDate" -> ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_INSTANT), "formBundleNumber" -> "119000004320")
  )
}

case class UKTRSubmissionSuccess(processingDate: LocalDateTime, formBundleNumber: String, chargeReference: Option[String])

object UKTRSubmissionSuccess {
  implicit val format: OFormat[UKTRSubmissionSuccess] = Json.format[UKTRSubmissionSuccess]
}
