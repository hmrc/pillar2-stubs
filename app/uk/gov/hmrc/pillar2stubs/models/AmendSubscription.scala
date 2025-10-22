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
  given Reads[LocalDate] = Reads.localDateReads("yyyy-MM-dd")
  given Writes[LocalDate] = Writes.temporalWrites[LocalDate, java.time.format.DateTimeFormatter](
    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
  )
}

case class SubscriptionResponse(success: SubscriptionSuccess)

object SubscriptionResponse {
  given OFormat[SubscriptionResponse] = Json.format[SubscriptionResponse]
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

  given Reads[AmendSubscriptionSuccess] = Reads { js =>
    if (js.asInstanceOf[JsObject].value.contains("replaceFilingMember"))
      JsError("AdditionalProperty")
    else JsSuccess(js)
  }.andThen(Json.reads[AmendSubscriptionSuccess])

  given OFormat[AmendSubscriptionSuccess] = OFormat(summon[Reads[AmendSubscriptionSuccess]], Json.writes[AmendSubscriptionSuccess])
}

case class AmendSubscriptionResponse(value: AmendSubscriptionSuccess)

object AmendSubscriptionResponse {
  given OFormat[AmendSubscriptionResponse] = Json.format[AmendSubscriptionResponse]
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
  given OFormat[UpeDetails] = Json.format[UpeDetails]
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
  given OFormat[UpeCorrespAddressDetails] = Json.format[UpeCorrespAddressDetails]
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
  given OFormat[UpeDetailsAmend] = Json.format[UpeDetailsAmend]
}

final case class FilingMemberAmendDetails(
  addNewFilingMember:      Boolean = true,
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)

object FilingMemberAmendDetails {
  given OFormat[FilingMemberAmendDetails] = Json.format[FilingMemberAmendDetails]
}

object SubscriptionSuccess {
  given OFormat[SubscriptionSuccess] = Json.format[SubscriptionSuccess]
}

final case class ContactDetailsType(
  name:         String,
  telephone:    Option[String],
  emailAddress: String
)

object ContactDetailsType {
  given OFormat[ContactDetailsType] = Json.format[ContactDetailsType]
}

final case class FilingMemberDetails(
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)

object FilingMemberDetails {
  given OFormat[FilingMemberDetails] = Json.format[FilingMemberDetails]
}

final case class AccountingPeriod(
  startDate: LocalDate,
  endDate:   LocalDate,
  dueDate:   Option[LocalDate] = None
)

object AccountingPeriod {
  given OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
}

final case class AccountStatus(
  inactive: Boolean
)

object AccountStatus {
  given OFormat[AccountStatus] = Json.format[AccountStatus]

  given Writes[Option[AccountStatus]] = new Writes[Option[AccountStatus]] {
    def writes(option: Option[AccountStatus]): JsValue = option match {
      case Some(accountStatus) => Json.toJson(accountStatus)(using summon[OFormat[AccountStatus]])
      case None                => JsNull
    }
  }
  given Writes[AccountStatus] = Json.writes[AccountStatus]

  type AccountStatusOpt = Option[AccountStatus]
  given Writes[AccountStatusOpt] = summon[Writes[Option[AccountStatus]]]

}
