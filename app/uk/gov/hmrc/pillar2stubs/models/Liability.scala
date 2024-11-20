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

case class Liability(
  totalLiability:     Option[BigDecimal],
  totalLiabilityDTT:  Option[BigDecimal],
  totalLiabilityIIR:  Option[BigDecimal],
  totalLiabilityUTPR: Option[BigDecimal],
  liableEntities:     Option[Seq[LiableEntity]],
  returnType:         Option[String]
)

object Liability {
  implicit val reads: Reads[Liability] = (
    (JsPath \ "totalLiability").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityDTT").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityIIR").readNullable[BigDecimal] and
      (JsPath \ "totalLiabilityUTPR").readNullable[BigDecimal] and
      (JsPath \ "liableEntities")
        .readNullable[Seq[LiableEntity]]
        .filter(JsonValidationError("liableEntities must not be empty"))(_.forall(_.nonEmpty)) and
      (JsPath \ "returnType").readNullable[String]
  )(Liability.apply _).filter(JsonValidationError("Missing required fields")) { liability =>
    liability.returnType.contains("NIL_RETURN") ||
    (liability.totalLiability.isDefined &&
      liability.totalLiabilityDTT.isDefined &&
      liability.totalLiabilityIIR.isDefined &&
      liability.totalLiabilityUTPR.isDefined &&
      liability.liableEntities.isDefined)
  }

  implicit val writes: OWrites[Liability] = Json.writes[Liability]
}
