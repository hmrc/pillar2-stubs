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
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.Future

class BarsControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  private val authHeader: (String, String) = HeaderNames.authorisation -> "token"

  private def barsRequest(sortCode: String, accountNumber: String, companyName: String = "Epic Adventure Inc"): JsValue =
    Json.obj(
      "account" -> Json.obj(
        "sortCode"      -> sortCode,
        "accountNumber" -> accountNumber
      ),
      "business" -> Json.obj(
        "companyName" -> companyName
      )
    )

  "BarsController" - {

    "must return FORBIDDEN response when 'Authorization' header is missing" in {
      val json: JsValue = Json.obj()
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json)
      val result  = route(app, request).value

      status(result) shouldBe Status.FORBIDDEN
    }

    "must return BAD_REQUEST for invalid JSON structure" in {
      val json: JsValue = Json.obj("invalid" -> "payload")
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
      val result  = route(app, request).value

      status(result) shouldBe Status.BAD_REQUEST
    }

    "must return OK with matched account for 206705/86473611" in {
      val json    = barsRequest("206705", "86473611", "Epic Adventure Inc")
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
      val result: Future[Result] = route(app, request).value

      status(result) shouldBe Status.OK
      val response = contentAsJson(result)
      (response \ "accountName").asOpt[String] shouldBe Some("Epic Adventure Inc")
      (response \ "nameMatches").asOpt[String] shouldBe Some("yes")
    }

    "must return OK with partial name match for 206705/86473611 with different company name" in {
      val json    = barsRequest("206705", "86473611", "Epic")
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
      val result: Future[Result] = route(app, request).value

      status(result) shouldBe Status.OK
      val response = contentAsJson(result)
      (response \ "nameMatches").asOpt[String] shouldBe Some("partial")
    }

    "must return OK with no name match for 206705/86473611 with unrelated company name" in {
      val json    = barsRequest("206705", "86473611", "Different Company")
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
      val result: Future[Result] = route(app, request).value

      status(result) shouldBe Status.OK
      val response = contentAsJson(result)
      (response \ "nameMatches").asOpt[String] shouldBe Some("no")
    }

    "must return OK for other valid account combinations" in {
      val testCases = Seq(
        ("206705", "86563612"),
        ("206705", "76523611"),
        ("206705", "56523611"),
        ("206705", "56945688"),
        ("207102", "86473611"),
        ("207102", "86563611"),
        ("207102", "76523611"),
        ("207102", "56523611"),
        ("207102", "74597611"),
        ("207106", "86473611"),
        ("207106", "86563611"),
        ("207106", "76523611"),
        ("207106", "56523611"),
        ("207106", "74597611"),
        ("609593", "96863604"),
        ("609593", "96113600")
      )

      testCases.foreach { case (sortCode, accountNumber) =>
        val json    = barsRequest(sortCode, accountNumber)
        val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
        val result: Future[Result] = route(app, request).value

        status(result) shouldBe Status.OK
        val response = contentAsJson(result)
        (response \ "accountName").asOpt[String] shouldBe defined
      }
    }

    "must return INTERNAL_SERVER_ERROR for unmatched account combination" in {
      val json    = barsRequest("000000", "00000000")
      val request = FakeRequest(POST, routes.BarsController.verify.url).withBody(json).withHeaders(authHeader)
      val result: Future[Result] = route(app, request).value

      status(result)          shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe "Unable to match"
    }

  }

}
