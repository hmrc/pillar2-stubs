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

import java.time.{LocalDate, LocalDateTime}

final case class FinancialDataResponse(
  idType:                String,
  idNumber:              String,
  regimeType:            String,
  processingDate:        LocalDateTime,
  financialTransactions: Seq[FinancialTransaction]
)

object FinancialDataResponse {

  implicit val format: OFormat[FinancialDataResponse] = Json.format[FinancialDataResponse]

}

final case class FinancialTransaction(
  mainTransaction: Option[String],
  items:           Seq[FinancialItem]
)

object FinancialTransaction {

  implicit val format: OFormat[FinancialTransaction] =
    Json.format[FinancialTransaction]

}

final case class FinancialItem(
  dueDate:        Option[LocalDate] = None,
  amount:         Option[BigDecimal] = None,
  paymentAmount:  Option[BigDecimal] = None,
  clearingDate:   Option[LocalDate] = None,
  clearingReason: Option[String] = None
)

object FinancialItem {

  implicit val format: OFormat[FinancialItem] =
    Json.format[FinancialItem]

}
