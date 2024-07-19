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

final case class RepaymentDetails(
                                   plrReference:       String,
                                   name:               String,
                                   utr:                Option[String],
                                   reasonForRepayment: String,
                                   refundAmount:       BigDecimal
                                 )

object RepaymentDetails {
  implicit val format: OFormat[RepaymentDetails] = Json.format[RepaymentDetails]
}

final case class BankDetails(
                              nameOnBankAccount: String,
                              bankName:          String,
                              sortCode:          Option[String],
                              accountNumber:     Option[String],
                              iban:              Option[String],
                              bic:               Option[String],
                              countryCode:       Option[String]
                            )

object BankDetails {
  implicit val format: OFormat[BankDetails] = Json.format[BankDetails]
}

final case class RepaymentContactDetails(
                                          contactDetails: String
                                        )

object RepaymentContactDetails {
  implicit val format: OFormat[RepaymentContactDetails] = Json.format[RepaymentContactDetails]
}

final case class SendRepaymentDetails(
                                       repaymentDetails: RepaymentDetails,
                                       bankDetails: BankDetails,
                                       contactDetails: RepaymentContactDetails,
                                     )

object SendRepaymentDetails {
  implicit val format: OFormat[SendRepaymentDetails] = Json.format[SendRepaymentDetails]
}

