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

import play.api.libs.json.{Json, Reads, __}

/*case class Organisation(organisationName: String)

object Organisation {
  implicit val reads: Reads[Organisation] = Json.reads[Organisation]
}*/

case class Subscription(safeId: String, organisationName: String)

object Subscription {

  implicit lazy val reads: Reads[Subscription] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "createSubscriptionRequest" \ "requestBody" \ "upeDetails" \ "safeId").read[String] and
        (__ \ "createSubscriptionRequest" \ "requestBody" \ "upeDetails" \ "organisationName").read[String]
    )(Subscription.apply _)
  }
}
