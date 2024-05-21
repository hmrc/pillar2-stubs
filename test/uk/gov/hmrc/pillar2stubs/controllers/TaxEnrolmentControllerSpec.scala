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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames

class TaxEnrolmentControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  private val authHeader: (String, String) = HeaderNames.authorisation -> "token"

  "TaxEnrolmentController" - {

    "allocate" - {
      "must return OK response with tax enrolment failure body response" in {
        val jsonResponse: String =
          s"""|{
              |  "code": "400",
              |  "message": "BAD_REQUEST"
              |}""".stripMargin
        val badGroupId = "0000"
        val json: JsValue = Json.obj()
        val request = FakeRequest(POST, routes.TaxEnrolmentController.allocate(badGroupId, "somePlr2Id").url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value
        status(result) shouldBe OK
        contentAsString(result) mustEqual jsonResponse
      }

      "must return CREATED response for successful tax enrolment" in {
        val goodGroupId = "1111"
        val json: JsValue = Json.obj()
        val request = FakeRequest(POST, routes.TaxEnrolmentController.allocate(goodGroupId, "somePlr2Id").url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value
        status(result) shouldBe CREATED
      }
    }

    "revoke" - {
      "must return OK response with tax enrolment failure body response" in {
        val jsonResponse: String =
          s"""|{
              |  "code": "400",
              |  "message": "BAD_REQUEST"
              |}""".stripMargin
        val badGroupId = "0000"
        val json: JsValue = Json.obj()
        val request = FakeRequest(DELETE, routes.TaxEnrolmentController.revoke(badGroupId, "somePlr2Id").url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value
        status(result) shouldBe OK
        contentAsString(result) mustEqual jsonResponse
      }

      "must return NO_CONTENT response for successful tax de-enrolment" in {
        val goodGroupId = "1111"
        val json: JsValue = Json.obj()
        val request = FakeRequest(DELETE, routes.TaxEnrolmentController.revoke(goodGroupId, "somePlr2Id").url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value
        status(result) shouldBe NO_CONTENT
      }
    }

  }
}
