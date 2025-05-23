/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2stubs.models.error

import enumeratum._
import play.api.libs.json._

case class HIPErrorWrapper[T](origin: Origin, response: T)

object HIPErrorWrapper {

  implicit def format[T: OFormat]: OFormat[HIPErrorWrapper[T]] = new OFormat[HIPErrorWrapper[T]] {
    override def reads(json: JsValue): JsResult[HIPErrorWrapper[T]] =
      for {
        origin   <- (json \ "origin").validate[Origin]
        response <- (json \ "response").validate[T]
      } yield HIPErrorWrapper(origin, response)

    override def writes(obj: HIPErrorWrapper[T]): JsObject = Json.obj("origin" -> obj.origin, "response" -> obj.response)
  }
}

case class HIPFailure(failures: List[HIPError])

object HIPFailure {
  implicit val format: OFormat[HIPFailure] = Json.format
}

case class HIPError(reason: String, `type`: String)

object HIPError {
  implicit val format: OFormat[HIPError] = Json.format
}

sealed trait Origin extends EnumEntry

object Origin extends Enum[Origin] with PlayJsonEnum[Origin] {
  val values = findValues

  case object HIP extends Origin
  case object HoD extends Origin
}
