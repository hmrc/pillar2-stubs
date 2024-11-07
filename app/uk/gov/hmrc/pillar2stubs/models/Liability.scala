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

// File: Liability.scala
package uk.gov.hmrc.pillar2stubs.models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Liability(
  totalLiability:     BigDecimal,
  totalLiabilityDTT:  BigDecimal,
  totalLiabilityIIR:  BigDecimal,
  totalLiabilityUTPR: BigDecimal,
  liableEntities:     Seq[LiableEntity]
)

object Liability {
  implicit val reads: Reads[Liability] = (
    (JsPath \ "totalLiability").read[BigDecimal] and
      (JsPath \ "totalLiabilityDTT").read[BigDecimal] and
      (JsPath \ "totalLiabilityIIR").read[BigDecimal] and
      (JsPath \ "totalLiabilityUTPR").read[BigDecimal] and
      (JsPath \ "liableEntities")
        .read[Seq[LiableEntity]]
        .filter(JsonValidationError("liableEntities must not be empty"))(_.nonEmpty)
  )(Liability.apply _)

  implicit val writes: OWrites[Liability] = Json.writes[Liability]
}
