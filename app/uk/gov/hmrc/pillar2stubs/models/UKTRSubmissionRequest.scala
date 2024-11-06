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

case class SubmissionLiability( // Changed from `Liability` to `SubmissionLiability`
  totalLiability:     Option[BigDecimal] = None,
  totalLiabilityDTT:  Option[BigDecimal] = None,
  totalLiabilityIIR:  Option[BigDecimal] = None,
  totalLiabilityUTPR: Option[BigDecimal] = None,
  liableEntities:     Option[Seq[LiableEntity]] = None,
  returnType:         Option[String] = None // Optional for NIL_RETURN
)

object SubmissionLiability {
  implicit val reads: Reads[SubmissionLiability] = (
    (JsPath \ "totalLiability").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityDTT").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityIIR").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityUTPR").readNullable[BigDecimal] and
      (JsPath \ "liableEntities").readNullable[Seq[LiableEntity]] and
      (JsPath \ "returnType").readNullable[String]
  )(SubmissionLiability.apply _)

  implicit val writes: OWrites[SubmissionLiability] = Json.writes[SubmissionLiability]
}

case class UKTRSubmissionRequest(
  accountingPeriodFrom: String,
  accountingPeriodTo:   String,
  qualifyingGroup:      Boolean,
  obligationDTT:        Boolean,
  obligationMTT:        Boolean,
  electionUKGAAP:       Boolean,
  liabilities:          SubmissionLiability // Updated to `SubmissionLiability`
)

object UKTRSubmissionRequest {
  implicit val format: OFormat[UKTRSubmissionRequest] = Json.format[UKTRSubmissionRequest]
}
