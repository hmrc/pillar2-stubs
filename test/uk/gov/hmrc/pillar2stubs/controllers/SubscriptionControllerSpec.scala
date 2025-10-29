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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Inspectors, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames

class SubscriptionControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues with Inspectors {

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
            | 				"organisationName": "duplicateSub",
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

      "must return UnprocessableEntity response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE0000123456789",
            | 				"organisationName": "unprocessableSub",
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

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must Service Unavailable Conflict response" in {

        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "subServerError",
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
            | 				"organisationName": "subRecordNotFound",
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

      "must return CREATED with XEPLR0000000001 for UPE contact name Quick Processing" in {
        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test Corp",
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
            | 				"name": "Quick Processing",
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
        val responseBody = contentAsString(result)
        responseBody should include("XEPLR0000000001")
      }

      "must return CREATED with XEPLR0000000002 for UPE contact name Medium Processing" in {
        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Test Corp",
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
            | 				"name": "Medium Processing",
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
        val responseBody = contentAsString(result)
        responseBody should include("XEPLR0000000002")
      }

      "must return CREATED with XEPLR0000000001 for organisation name Quick Processing Corp" in {
        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Quick Processing Corp",
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
            | 				"name": "Test Contact",
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
        val responseBody = contentAsString(result)
        responseBody should include("XEPLR0000000001")
      }

      "must return CREATED with XEPLR0000000002 for organisation name Medium Processing Corp" in {
        val input: String =
          """{
            | 			"upeDetails": {
            | 				"safeId": "XE6666666666666",
            | 				"organisationName": "Medium Processing Corp",
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
            | 				"name": "Test Contact",
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
        val responseBody = contentAsString(result)
        responseBody should include("XEPLR0000000002")
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
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0123456400").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "must return NOT_FOUND response when subscription does not exist" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0123456404").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe NOT_FOUND
      }

      "must return UNPROCESSABLE_ENTITY response for unprocessable requests" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0123456422").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must return INTERNAL_SERVER_ERROR response when an unexpected error occurs" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0123456500").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "must return SERVICE_UNAVAILABLE response when the service is down" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0123456503").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe SERVICE_UNAVAILABLE
      }

      "must return UNPROCESSABLE_ENTITY initially for XEPLR0000000001 (Quick Processing)" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0000000001").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must return UNPROCESSABLE_ENTITY initially for XEPLR0000000002 (Medium Processing)" in {
        val request = FakeRequest(GET, routes.SubscriptionController.retrieveSubscription("XEPLR0000000002").url).withHeaders(authHeader)
        val result  = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must return a OK with an inactive subscription for the given IDs" in {
        val ids      = Seq("XEPLR2000000109", "XEPLR2000000110", "XEPLR2000000111", "XEPLR2000000111")
        val requests = ids.map(id => FakeRequest(GET, routes.SubscriptionController.retrieveSubscription(id).url).withHeaders(authHeader))
        val results  = requests.map(route(app, _).value)
        forAll(results) { result =>
          (contentAsJson(result) \ "success" \ "accountStatus" \ "inactive").as[Boolean] shouldBe true
          status(result)                                                                 shouldBe OK
        }

      }

    }
  }

  "PUT" - {
    "amendSubscription" - {

      val authHeader: (String, String) = HeaderNames.authorisation -> "token"

      "must return OK response with valid data when subscription exists" in {
        val json: JsValue = Json.parse(""" {
                                                       |          "upeDetails": {
                                                       |            "plrReference": "XMPLR0012345678",
                                                       |            "customerIdentification1": "SA7743248",
                                                       |            "customerIdentification2": "1234567890",
                                                       |            "organisationName": "tin tin",
                                                       |            "registrationDate": "1994-12-05",
                                                       |            "domesticOnly": true,
                                                       |            "filingMember": false
                                                       |          },
                                                       |          "upeCorrespAddressDetails": {
                                                       |            "addressLine1": "10 High Street",
                                                       |            "addressLine2": "Egham",
                                                       |            "addressLine3": "Surrey",
                                                       |            "addressLine4": "Wembley",
                                                       |            "postCode": "SU10 6HH",
                                                       |            "countryCode": "GB"
                                                       |          },
                                                       |          "primaryContactDetails": {
                                                       |            "name": "Fred Jones",
                                                       |            "telephone": "01159700700",
                                                       |            "emailAddress": "fred.jones@acme.com"
                                                       |          },
                                                       |          "secondaryContactDetails": {
                                                       |            "name": "Jill Jones",
                                                       |            "telephone": "01159788799",
                                                       |            "emailAddress": "jill.jones@acme.com"
                                                       |          },
                                                       |          "filingMemberDetails": {
                                                       |            "addNewFilingMember": true,
                                                       |            "safeId": "XV5277988337712",
                                                       |            "customerIdentification1": "DD17743248",
                                                       |            "customerIdentification2": "431234567890",
                                                       |            "organisationName": "Acme Upe Ltd"
                                                       |          },
                                                       |          "accountingPeriod": {
                                                       |            "startDate": "2022-04-06",
                                                       |            "endDate": "2023-04-05"
                                                       |          }
                                                       |        }
                                                       |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)
        val result  = route(app, request).value

        status(result) shouldBe OK
      }

      "must return BAD_REQUEST response for invalid requests" in {
        val json: JsValue = Json.parse(""" {
                                          |          "upeDetails": {
                                          |            "plrReference": "XMPLR0012345678",
                                          |            "customerIdentification1": "SA7743248",
                                          |            "customerIdentification2": "1234567890",
                                          |            "organisationName": "400",
                                          |            "registrationDate": "1994-12-05",
                                          |            "domesticOnly": true,
                                          |            "filingMember": false
                                          |          },
                                          |          "upeCorrespAddressDetails": {
                                          |            "addressLine1": "10 High Street",
                                          |            "addressLine2": "Egham",
                                          |            "addressLine3": "Surrey",
                                          |            "addressLine4": "Wembley",
                                          |            "postCode": "SU10 6HH",
                                          |            "countryCode": "GB"
                                          |          },
                                          |          "primaryContactDetails": {
                                          |            "name": "400",
                                          |            "telephone": "01159700700",
                                          |            "emailAddress": "fred.jones@acme.com"
                                          |          },
                                          |          "secondaryContactDetails": {
                                          |            "name": "Jill Jones",
                                          |            "telephone": "01159788799",
                                          |            "emailAddress": "jill.jones@acme.com"
                                          |          },
                                          |          "filingMemberDetails": {
                                          |            "addNewFilingMember": true,
                                          |            "safeId": "XV5277988337712",
                                          |            "customerIdentification1": "DD17743248",
                                          |            "customerIdentification2": "431234567890",
                                          |            "organisationName": "Acme Upe Ltd"
                                          |          },
                                          |          "accountingPeriod": {
                                          |            "startDate": "2022-04-06",
                                          |            "endDate": "2023-04-05"
                                          |          }
                                          |        }
                                          |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)
        val result  = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }

      "must return CONFLICT response when subscription does not exist" in {
        val json: JsValue = Json.parse(""" {
                                          |          "upeDetails": {
                                          |            "plrReference": "XMPLR0012345678",
                                          |            "customerIdentification1": "SA7743248",
                                          |            "customerIdentification2": "1234567890",
                                          |            "organisationName": "409",
                                          |            "registrationDate": "1994-12-05",
                                          |            "domesticOnly": true,
                                          |            "filingMember": false
                                          |          },
                                          |          "upeCorrespAddressDetails": {
                                          |            "addressLine1": "10 High Street",
                                          |            "addressLine2": "Egham",
                                          |            "addressLine3": "Surrey",
                                          |            "addressLine4": "Wembley",
                                          |            "postCode": "SU10 6HH",
                                          |            "countryCode": "GB"
                                          |          },
                                          |          "primaryContactDetails": {
                                          |            "name": "409",
                                          |            "telephone": "01159700700",
                                          |            "emailAddress": "fred.jones@acme.com"
                                          |          },
                                          |          "secondaryContactDetails": {
                                          |            "name": "Jill Jones",
                                          |            "telephone": "01159788799",
                                          |            "emailAddress": "jill.jones@acme.com"
                                          |          },
                                          |          "filingMemberDetails": {
                                          |            "addNewFilingMember": true,
                                          |            "safeId": "XV5277988337712",
                                          |            "customerIdentification1": "DD17743248",
                                          |            "customerIdentification2": "431234567890",
                                          |            "organisationName": "Acme Upe Ltd"
                                          |          },
                                          |          "accountingPeriod": {
                                          |            "startDate": "2022-04-06",
                                          |            "endDate": "2023-04-05"
                                          |          }
                                          |        }
                                          |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)
        val result  = route(app, request).value

        status(result) shouldBe CONFLICT
      }

      "must return UNPROCESSABLE_ENTITY response for unprocessable requests" in {
        val json: JsValue = Json.parse(""" {
                                          |          "upeDetails": {
                                          |            "plrReference": "XMPLR0012345678",
                                          |            "customerIdentification1": "SA7743248",
                                          |            "customerIdentification2": "1234567890",
                                          |            "organisationName": "422",
                                          |            "registrationDate": "1994-12-05",
                                          |            "domesticOnly": true,
                                          |            "filingMember": false
                                          |          },
                                          |          "upeCorrespAddressDetails": {
                                          |            "addressLine1": "10 High Street",
                                          |            "addressLine2": "Egham",
                                          |            "addressLine3": "Surrey",
                                          |            "addressLine4": "Wembley",
                                          |            "postCode": "SU10 6HH",
                                          |            "countryCode": "GB"
                                          |          },
                                          |          "primaryContactDetails": {
                                          |            "name": "422",
                                          |            "telephone": "01159700700",
                                          |            "emailAddress": "fred.jones@acme.com"
                                          |          },
                                          |          "secondaryContactDetails": {
                                          |            "name": "Jill Jones",
                                          |            "telephone": "01159788799",
                                          |            "emailAddress": "jill.jones@acme.com"
                                          |          },
                                          |          "filingMemberDetails": {
                                          |            "addNewFilingMember": true,
                                          |            "safeId": "XV5277988337712",
                                          |            "customerIdentification1": "DD17743248",
                                          |            "customerIdentification2": "431234567890",
                                          |            "organisationName": "Acme Upe Ltd"
                                          |          },
                                          |          "accountingPeriod": {
                                          |            "startDate": "2022-04-06",
                                          |            "endDate": "2023-04-05"
                                          |          }
                                          |        }
                                          |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)
        val result  = route(app, request).value

        status(result) shouldBe UNPROCESSABLE_ENTITY
      }

      "must return INTERNAL_SERVER_ERROR response when an unexpected error occurs" in {
        val json: JsValue = Json.parse(""" {
                                          |          "upeDetails": {
                                          |            "plrReference": "XMPLR0012345678",
                                          |            "customerIdentification1": "SA7743248",
                                          |            "customerIdentification2": "1234567890",
                                          |            "organisationName": "500",
                                          |            "registrationDate": "1994-12-05",
                                          |            "domesticOnly": true,
                                          |            "filingMember": false
                                          |          },
                                          |          "upeCorrespAddressDetails": {
                                          |            "addressLine1": "10 High Street",
                                          |            "addressLine2": "Egham",
                                          |            "addressLine3": "Surrey",
                                          |            "addressLine4": "Wembley",
                                          |            "postCode": "SU10 6HH",
                                          |            "countryCode": "GB"
                                          |          },
                                          |          "primaryContactDetails": {
                                          |            "name": "500",
                                          |            "telephone": "01159700700",
                                          |            "emailAddress": "fred.jones@acme.com"
                                          |          },
                                          |          "secondaryContactDetails": {
                                          |            "name": "Jill Jones",
                                          |            "telephone": "01159788799",
                                          |            "emailAddress": "jill.jones@acme.com"
                                          |          },
                                          |          "filingMemberDetails": {
                                          |            "addNewFilingMember": true,
                                          |            "safeId": "XV5277988337712",
                                          |            "customerIdentification1": "DD17743248",
                                          |            "customerIdentification2": "431234567890",
                                          |            "organisationName": "Acme Upe Ltd"
                                          |          },
                                          |          "accountingPeriod": {
                                          |            "startDate": "2022-04-06",
                                          |            "endDate": "2023-04-05"
                                          |          }
                                          |        }
                                          |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)

        val result = route(app, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "must return SERVICE_UNAVAILABLE response when the service is down" in {

        val json: JsValue = Json.parse(""" {
                                          |          "upeDetails": {
                                          |            "plrReference": "XMPLR0012345678",
                                          |            "customerIdentification1": "SA7743248",
                                          |            "customerIdentification2": "1234567890",
                                          |            "organisationName": "503",
                                          |            "registrationDate": "1994-12-05",
                                          |            "domesticOnly": true,
                                          |            "filingMember": false
                                          |          },
                                          |          "upeCorrespAddressDetails": {
                                          |            "addressLine1": "10 High Street",
                                          |            "addressLine2": "Egham",
                                          |            "addressLine3": "Surrey",
                                          |            "addressLine4": "Wembley",
                                          |            "postCode": "SU10 6HH",
                                          |            "countryCode": "GB"
                                          |          },
                                          |          "primaryContactDetails": {
                                          |            "name": "503",
                                          |            "telephone": "01159700700",
                                          |            "emailAddress": "fred.jones@acme.com"
                                          |          },
                                          |          "secondaryContactDetails": {
                                          |            "name": "Jill Jones",
                                          |            "telephone": "01159788799",
                                          |            "emailAddress": "jill.jones@acme.com"
                                          |          },
                                          |          "filingMemberDetails": {
                                          |            "addNewFilingMember": true,
                                          |            "safeId": "XV5277988337712",
                                          |            "customerIdentification1": "DD17743248",
                                          |            "customerIdentification2": "431234567890",
                                          |            "organisationName": "Acme Upe Ltd"
                                          |          },
                                          |          "accountingPeriod": {
                                          |            "startDate": "2022-04-06",
                                          |            "endDate": "2023-04-05"
                                          |          }
                                          |        }
                                          |""".stripMargin)
        val request = FakeRequest(PUT, routes.SubscriptionController.amendSubscription.url).withHeaders(authHeader).withBody(json)

        val result = route(app, request).value

        status(result) shouldBe SERVICE_UNAVAILABLE
      }
    }

  }

}
