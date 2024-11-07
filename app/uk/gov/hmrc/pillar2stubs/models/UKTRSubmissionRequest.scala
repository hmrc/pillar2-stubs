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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

case class UKTRSubmissionRequest(
  accountingPeriodFrom: String,
  accountingPeriodTo:   String,
  qualifyingGroup:      Boolean,
  obligationDTT:        Boolean,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          Liability
) {
  def isValid: Boolean = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fromDate  = Try(LocalDate.parse(accountingPeriodFrom, formatter)).toOption
    val toDate    = Try(LocalDate.parse(accountingPeriodTo, formatter)).toOption
    (fromDate, toDate) match {
      case (Some(from), Some(to)) => !to.isBefore(from)
      case _                      => false
    }
  }
}

object UKTRSubmissionRequest {
  implicit val reads: Reads[UKTRSubmissionRequest] = (
    (JsPath \ "accountingPeriodFrom").read[String] and
      (JsPath \ "accountingPeriodTo").read[String] and
      (JsPath \ "qualifyingGroup").read[Boolean] and
      (JsPath \ "obligationDTT").read[Boolean] and
      (JsPath \ "obligationMTT").read[Boolean] and
      (JsPath \ "electionUKGAAP").read[Boolean] and
      (JsPath \ "liabilities").read[Liability]
  )(UKTRSubmissionRequest.apply _)

  implicit val writes: OWrites[UKTRSubmissionRequest] = Json.writes[UKTRSubmissionRequest]
}
