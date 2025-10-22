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

import play.api.libs.json.{Json, OFormat}

sealed trait Liability

case class LiabilityData(
  electionDTTSingleMember:  Boolean,
  electionUTPRSingleMember: Boolean,
  numberSubGroupDTT:        Int,
  numberSubGroupUTPR:       Int,
  totalLiability:           BigDecimal,
  totalLiabilityDTT:        BigDecimal,
  totalLiabilityIIR:        BigDecimal,
  totalLiabilityUTPR:       BigDecimal,
  liableEntities:           Seq[LiableEntity]
) extends Liability

object LiabilityData {
  given OFormat[LiabilityData] = Json.format[LiabilityData]
}

case class LiabilityNilReturn(returnType: ReturnType) extends Liability

object LiabilityNilReturn {
  given OFormat[LiabilityNilReturn] = Json.format[LiabilityNilReturn]
}
