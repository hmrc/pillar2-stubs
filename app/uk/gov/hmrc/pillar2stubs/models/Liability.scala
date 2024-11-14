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
