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

import play.api.libs.json._

import java.time.LocalDate

object LocalDateImplicits {
  implicit val localDateReads: Reads[LocalDate] = Reads.localDateReads("yyyy-MM-dd")
  implicit val localDateWrites: Writes[LocalDate] = Writes.temporalWrites[LocalDate, java.time.format.DateTimeFormatter](
    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
  )
}

case class SubscriptionResponse(success: SubscriptionSuccess)

object SubscriptionResponse {
  implicit val format: OFormat[SubscriptionResponse] = Json.format[SubscriptionResponse]
}

case class SubscriptionSuccess(
  formBundleNumber:         Option[String],
  upeDetails:               UpeDetails,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberDetails],
  accountingPeriod:         AccountingPeriod,
  accountStatus:            Option[AccountStatus]
)

case class AmendSubscriptionSuccess(
  upeDetails:               UpeDetailsAmend,
  accountingPeriod:         AccountingPeriod,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberAmendDetails]
)

object AmendSubscriptionSuccess {
  implicit val format: OFormat[AmendSubscriptionSuccess] = Json.format[AmendSubscriptionSuccess]
}

case class AmendSubscriptionResponse(value: AmendSubscriptionSuccess)

object AmendSubscriptionResponse {
  implicit val format: OFormat[AmendSubscriptionResponse] = Json.format[AmendSubscriptionResponse]
}

case class UpeDetails(
  plrReference:            String,
  customerIdentification1: String,
  customerIdentification2: String,
  organisationName:        String,
  registrationDate:        LocalDate,
  domesticOnly:            Boolean,
  filingMember:            Boolean
)

object UpeDetails {
  implicit val format: OFormat[UpeDetails] = Json.format[UpeDetails]
}

final case class UpeCorrespAddressDetails(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postCode:     Option[String],
  countryCode:  String
)

object UpeCorrespAddressDetails {
  implicit val format: OFormat[UpeCorrespAddressDetails] = Json.format[UpeCorrespAddressDetails]
}

final case class UpeDetailsAmend(
  plrReference:            String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String,
  registrationDate:        LocalDate,
  domesticOnly:            Boolean,
  filingMember:            Boolean
)

object UpeDetailsAmend {
  implicit val format: OFormat[UpeDetailsAmend] = Json.format[UpeDetailsAmend]
}

final case class FilingMemberAmendDetails(
  addNewFilingMember:      Boolean = true,
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)

object FilingMemberAmendDetails {
  implicit val format: OFormat[FilingMemberAmendDetails] = Json.format[FilingMemberAmendDetails]
}

object SubscriptionSuccess {
  implicit val format: OFormat[SubscriptionSuccess] = Json.format[SubscriptionSuccess]
}

final case class ContactDetailsType(
  name:         String,
  telephone:    Option[String],
  emailAddress: String
)

object ContactDetailsType {
  implicit val format: OFormat[ContactDetailsType] = Json.format[ContactDetailsType]
}

final case class FilingMemberDetails(
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)

object FilingMemberDetails {
  implicit val format: OFormat[FilingMemberDetails] = Json.format[FilingMemberDetails]
}

final case class AccountingPeriod(
  startDate: LocalDate,
  endDate:   LocalDate,
  duetDate:  Option[LocalDate] = None
)

object AccountingPeriod {
  implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
}

final case class AccountStatus(
  inactive: Boolean
)

object AccountStatus {
  implicit val format: OFormat[AccountStatus] = Json.format[AccountStatus]

  implicit val optionWrites: Writes[Option[AccountStatus]] = new Writes[Option[AccountStatus]] {
    def writes(option: Option[AccountStatus]): JsValue = option match {
      case Some(accountStatus) => Json.toJson(accountStatus)(format)
      case None                => JsNull
    }
  }
  implicit val writes: Writes[AccountStatus] = Json.writes[AccountStatus]

  type AccountStatusOpt = Option[AccountStatus]
  implicit val accountStatusOptWrites: Writes[AccountStatusOpt] = optionWrites

}
