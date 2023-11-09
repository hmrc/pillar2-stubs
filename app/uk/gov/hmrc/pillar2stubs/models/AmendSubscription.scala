/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}
import java.time.LocalDate

case class UpeDetails(
  plrReference:            String,
  customerIdentification1: String,
  customerIdentification2: String,
  organisationName:        String,
  registrationDate:        LocalDate,
  domesticOnly:            Boolean,
  filingMember:            Boolean
)

case class AccountingPeriod(
  startDate: LocalDate,
  endDate:   LocalDate
)

case class AddressDetails(
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  addressLine4: String,
  postCode:     String,
  countryCode:  String
)

case class ContactDetails(
  name:         String,
  telephone:    String,
  emailAddress: String
)

case class FilingMemberDetails(
  addNewFilingMember:      Boolean,
  safeId:                  String,
  customerIdentification1: String,
  customerIdentification2: String,
  organisationName:        String
)

case class SubscriptionRequest(
  upeDetails:               UpeDetails,
  accountingPeriod:         AccountingPeriod,
  upeCorrespAddressDetails: AddressDetails,
  primaryContactDetails:    ContactDetails,
  secondaryContactDetails:  ContactDetails,
  filingMemberDetails:      FilingMemberDetails
)

object UpeDetails {
  implicit val format: Format[UpeDetails] = Json.format[UpeDetails]
}

object AccountingPeriod {
  implicit val format: Format[AccountingPeriod] = Json.format[AccountingPeriod]
}

object AddressDetails {
  implicit val format: Format[AddressDetails] = Json.format[AddressDetails]
}

object ContactDetails {
  implicit val format: Format[ContactDetails] = Json.format[ContactDetails]
}

object FilingMemberDetails {
  implicit val format: Format[FilingMemberDetails] = Json.format[FilingMemberDetails]
}

object SubscriptionRequest {
  implicit val format: Format[SubscriptionRequest] = Json.format[SubscriptionRequest]
}
