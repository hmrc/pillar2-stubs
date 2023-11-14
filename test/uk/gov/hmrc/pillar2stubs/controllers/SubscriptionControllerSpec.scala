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

package uk.gov.hmrc.pillar2stubs.controllers

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames

class SubscriptionControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  "POST " - {
    "createSubscription" - {
      "must return FORBIDDEN response when 'Authorization' header is missing" in {
        val json: JsValue = Json.obj()

        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json)
        val result  = route(app, request).value

        status(result) shouldBe FORBIDDEN
      }

      "must return OK response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Dave Smith",
            | 				"registrationDate": "2023-09-28",
            | 				"domesticOnly": false,
            | 				"filingMember": false
            | 			},
            | 			"accountingPeriod": {
            | 				"startDate": "2024-12-31",
            | 				"endDate": "2025-12-12"
            | 			},
            | 			"upeCorrespAddressDetails": {
            | 				"addressLine1": "10 High Street",
            | 				"addressLine2": "Egham",
            | 				"addressLine3": "Surrey",
            | 				"addressLine4": "South East England",
            | 				"countryCode": "GB"
            | 			},
            | 			"primaryContactDetails": {
            | 				"name": "Ashley Smith",
            | 				"emailAddress": "Test@test.com"
            | 			},
            | 			"filingMemberDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test"
            | 			}
            | 	}
            |
            |""".stripMargin

        val json:       JsValue          = Json.parse(input)
        val authHeader: (String, String) = HeaderNames.authorisation -> "token"
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe CREATED
      }

      "must return Conflict response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE0000123456789",
            | 				"organisationName": "duplicate",
            | 				"registrationDate": "2023-09-28",
            | 				"domesticOnly": false,
            | 				"filingMember": false
            | 			},
            | 			"accountingPeriod": {
            | 				"startDate": "2024-12-31",
            | 				"endDate": "2025-12-12"
            | 			},
            | 			"upeCorrespAddressDetails": {
            | 				"addressLine1": "10 High Street",
            | 				"addressLine2": "Egham",
            | 				"addressLine3": "Surrey",
            | 				"addressLine4": "South East England",
            | 				"countryCode": "GB"
            | 			},
            | 			"primaryContactDetails": {
            | 				"name": "Ashley Smith",
            | 				"emailAddress": "Test@test.com"
            | 			},
            | 			"filingMemberDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test"
            | 			}
            | 	}
            |
            |""".stripMargin

        val json:       JsValue          = Json.parse(input)
        val authHeader: (String, String) = HeaderNames.authorisation -> "token"
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe CONFLICT
      }

      "must Service Unavailable Conflict response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "server",
            | 				"registrationDate": "2023-09-28",
            | 				"domesticOnly": false,
            | 				"filingMember": false
            | 			},
            | 			"accountingPeriod": {
            | 				"startDate": "2024-12-31",
            | 				"endDate": "2025-12-12"
            | 			},
            | 			"upeCorrespAddressDetails": {
            | 				"addressLine1": "10 High Street",
            | 				"addressLine2": "Egham",
            | 				"addressLine3": "Surrey",
            | 				"addressLine4": "South East England",
            | 				"countryCode": "GB"
            | 			},
            | 			"primaryContactDetails": {
            | 				"name": "Ashley Smith",
            | 				"emailAddress": "Test@test.com"
            | 			},
            | 			"filingMemberDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test"
            | 			}
            | 	}
            |
            |""".stripMargin

        val json:       JsValue          = Json.parse(input)
        val authHeader: (String, String) = HeaderNames.authorisation -> "token"
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe SERVICE_UNAVAILABLE
      }

      "must return NotFound response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "notFound",
            | 				"registrationDate": "2023-09-28",
            | 				"domesticOnly": false,
            | 				"filingMember": false
            | 			},
            | 			"accountingPeriod": {
            | 				"startDate": "2024-12-31",
            | 				"endDate": "2025-12-12"
            | 			},
            | 			"upeCorrespAddressDetails": {
            | 				"addressLine1": "10 High Street",
            | 				"addressLine2": "Egham",
            | 				"addressLine3": "Surrey",
            | 				"addressLine4": "South East England",
            | 				"countryCode": "GB"
            | 			},
            | 			"primaryContactDetails": {
            | 				"name": "Ashley Smith",
            | 				"emailAddress": "Test@test.com"
            | 			},
            | 			"filingMemberDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test"
            | 			}
            | 	}
            |
            |""".stripMargin

        val json:       JsValue          = Json.parse(input)
        val authHeader: (String, String) = HeaderNames.authorisation -> "token"
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
      }

      "must return BadRequest response for the invalid input request" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": 2,
            | 				"organisationName": "Dave Smith",
            | 				"registrationDate": "2023-09-28",
            | 				"domesticOnly": false,
            | 				"filingMember": false
            | 			},
            | 			"accountingPeriod": {
            | 				"startDate": "2024-12-31",
            | 				"endDate": "Hello"
            | 			},
            | 			"upeCorrespAddressDetails": {
            | 				"addressLine1": "10 High Street",
            | 				"addressLine2": "Egham",
            | 				"addressLine3": "Surrey",
            | 				"addressLine4": "South East England",
            | 				"countryCode": "GB"
            | 			},
            | 			"primaryContactDetails": {
            | 				"name": "Ashley Smith",
            | 				"emailAddress": "Test@test.com"
            | 			},
            | 			"filingMemberDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test"
            | 			}
            | 	}
            |
            |""".stripMargin

        val json:       JsValue          = Json.parse(input)
        val authHeader: (String, String) = HeaderNames.authorisation -> "token"
        val request = FakeRequest(POST, routes.SubscriptionController.createSubscription.url).withBody(json).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

    }

  }

  "GET" - {
    "retrieveSubscription" - {

      val authHeader: (String, String) = HeaderNames.authorisation -> "token"

      "must return FORBIDDEN response when 'Authorization' header is missing" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("someId").url)
        val result  = route(app, request).value

        status(result) shouldBe FORBIDDEN
      }

      "must return OK response with valid data when subscription exists" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("validId").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe OK
      }

      "must return BAD_REQUEST response for invalid requests" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("400").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "must return NOT_FOUND response when subscription does not exist" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("404").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
      }

      "must return UNPROCESSABLE_ENTITY response for unprocessable requests" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("422").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must return INTERNAL_SERVER_ERROR response when an unexpected error occurs" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("500").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "must return SERVICE_UNAVAILABLE response when the service is down" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("503").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe SERVICE_UNAVAILABLE
      }
    }

  }
}
