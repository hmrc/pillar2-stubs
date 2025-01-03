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

import play.api.libs.json.Json
import play.api.libs.json.OFormat

final case class BarsBusinessAssessmentRequest(account: Account, business: Business)

final case class Account(
  sortCode:      String,
  accountNumber: String,
  rollNumber:    Option[String] = None
)

final case class Business(companyName: String)

object Account {
  implicit val format: OFormat[Account] = Json.format[Account]
}

object Business {
  implicit val format: OFormat[Business] = Json.format[Business]
}
object BarsBusinessAssessmentRequest {
  implicit val format: OFormat[BarsBusinessAssessmentRequest] = Json.format[BarsBusinessAssessmentRequest]
}
