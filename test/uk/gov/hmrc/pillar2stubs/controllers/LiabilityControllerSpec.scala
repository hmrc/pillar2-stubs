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

package uk.gov.hmrc.pillar2stubs.controllers

import org.openqa.selenium.remote.tracing.HttpTracing.inject
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames

class LiabilityControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  "LiabilityController POST" - {

    "return CREATED for valid request" in {
      val input: String =
        """{
          |  "accountingPeriodFrom": "2024-08-14",
          |  "accountingPeriodTo": "2024-12-14",
          |  "qualifyingGroup": true,
          |  "obligationDTT": true,
          |  "obligationMTT": true,
          |  "liabilities": {
          |    "totalLiability": 10000.99,
          |    "totalLiabilityDTT": 5000.99,
          |    "totalLiabilityIIR": 4000,
          |    "totalLiabilityUTPR": 10000.99,
          |    "liableEntities": [
          |      {
          |        "ukChargeableEntityName": "Newco PLC",
          |        "idType": "CRN",
          |        "idValue": "12345678",
          |        "amountOwedDTT": 5000,
          |        "electedDTT": true,
          |        "amountOwedIIR": 3400,
          |        "amountOwedUTPR": 6000.5,
          |        "electedUTPR": true
          |      }
          |    ]
          |  }
          |}
    """.stripMargin

      val json: JsValue = Json.parse(input)
      val request = FakeRequest(POST, routes.LiabilityController.createLiability.url)
        .withHeaders("Content-Type" -> "application/json")
        .withBody(json)

      val result = route(app, request).value

      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "success" \ "formBundleNumber").as[String] mustBe "123456789012345"
    }

    "return BAD_REQUEST for invalid JSON" in {
      val input: String =
        """{
          |  "invalid": "json"
          |}
    """.stripMargin

      val json: JsValue = Json.parse(input)
      val request = FakeRequest(POST, routes.LiabilityController.createLiability.url)
        .withHeaders("Content-Type" -> "application/json")
        .withBody(json)

      val result = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid JSON")
    }

    "return BAD_REQUEST for non-JSON body" in {
      val input: String = "non-json body"

      val request = FakeRequest(POST, routes.LiabilityController.createLiability.url)
        .withHeaders("Content-Type" -> "application/json")
        .withBody(input)

      val result = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid Json: Unrecognized token")
    }

  }
}
