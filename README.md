# pillar2-stubs

The Pillar2 stubbs service provides stubbs for the GRS systems to mock the responses.

## Running the service locally

#### To compile the project:

`sbt clean update compile`

#### To check code coverage:

`sbt scalafmt test:scalafmt it:test::scalafmt coverage test it/test coverageReport`

#### Integration and unit tests

To run the unit tests within the project:

`sbt test`

#### Starting the server in local

`sbt run`

By default, the service runs locally on port **10052**

To use test-only route locally, run the below:

`sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10052'`

### Using Service Manager

You can use service manager to provide necessary assets to the pillar2 backend.
**PILLAR2_ALL** service is responsible for starting up all the services required by the tax credits service project.

This can be started by running the below in a new terminal:

    sm2 --start PILLAR2_ALL

#### Using sbt

For local development, use `sbt run` but if it is already running in sm2, execute below command to stop the
service before running sbt commands.

    sm2 --stop PILLAR_2_STUBS

This is an authenticated service, so users first need to be authenticated via GG in order to use the service.

Navigate to http://localhost:9949/auth-login-stub/gg-sign-in which redirects to auth-login-stub page.

Make sure to fill in the fields as below:

***Redirect URL: http://localhost:10050/report-pillar2-top-up-taxes***

***Affinity Group: Organisation***

## Endpoints

```
POST /registration/02.00.00/organisation 
```

Creates a Registration request without passing ID

#### Happy Path:
To trigger the happy path, ensure you provide a valid request body

```dtd
{
  "regime": "PLR",
  "acknowledgementReference": "d31186c7412e4823897ecc7ee339545c",
  "isAnAgent": false,
  "isAGroup": true,
  "organisation": {
    "organisationName": "Stark Corp"
  },
  "address": {
    "addressLine1": "100",
    "addressLine3": "Newyork",
    "postalCode": "10052",
    "countryCode": "US"
  },
  "contactDetails": {
    "emailAddress": "stark.tony@starkind.com"
  }
}
```

> Response status: 200
>
> Response body: N/A

#### Unhappy Path:
To trigger the unhappy paths, ensure you provide a valid request body.<br>
The below error responses can be expected:

HTTP 500 Internal Server Error

```dtd
{
  "errorDetail": {
    "timestamp": "2016-08-23T18:15:41Z",
    "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
    "errorCode": "500",
    "errorMessage": "Internal error",
    "source": "Internal Server error"
  }
}
```

> Response status: 500
>
> Response body: N/A

HTTP 400 Bad Request Error

```dtd
{
  "errorDetail": {
    "timestamp" : "2023-02-14T12:58:44Z",
      "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
      "errorCode": "400",
      "errorMessage": "Invalid ID",
      "source": "Back End",
      "sourceFaultDetail":{
        "detail":[
          "001 - Invalid Regime"
        ]
      }
  }
}
```

> Response status: 400
>
> Response body: N/A

HTTP 503 Service Unavailable Error

```dtd
{
  "errorDetail": {
    "timestamp": "2016-08-23T18:15:41Z",
    "correlationId": "",
    "errorCode": "503",
    "errorMessage": "Send timeout",
    "source": "Back End",
    "sourceFaultDetail": {
      "detail": ["101504 - Timeout "]
    }
  }
}
```

> Response status: 503
>
> Response body: N/A

HTTP 503 Request Could not be processed Error

```dtd
{
  "errorDetail": {
    "source": "Back End",
    "timestamp": "2020-11-11T13:19:52.307Z",
    "errorMessage": "Request could not be processed",
    "errorCode": "503",
    "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
    "sourceFaultDetail": {
      "detail": [
        "001 - Request Cannot be processed"
      ]
    }
  }
}

```

> Response status: 503
>
> Response body: N/A

HTTP 404 Record Not Found Error

```dtd
{
  "errorDetail": {
    "source": "Back End",
    "timestamp": "2020-11-23T13:19:52.307Z",
    "errorMessage": "Record not found",
    "errorCode": "404",
    "correlationId": "36147652-e594-94a4-a229-23f28e20e841",
    "sourceFaultDetail": {
      "detail": [
        "Detail cannot be found"
      ]
    }
  }
}
```

> Response status: 404
>
> Response body: N/A
---

```
POST /pillar2/subscription
```

Creates a Subscription request

#### Happy Path:
To trigger the happy path, ensure you provide a valid request body

```dtd
{
  "upeDetails": {
    "safeId": "XE6666666666666",
    "organisationName": "Stark Corp",
    "registrationDate": "2023-12-08",
    "domesticOnly": false,
    "filingMember": true
  },
  "accountingPeriod": {
    "startDate": "2024-01-01",
    "endDate": "2025-01-01"
  },
  "upeCorrespAddressDetails": {
    "addressLine1": "100",
    "addressLine3": "Newyork",
    "postCode": "10052",
    "countryCode": "US"
  },
  "primaryContactDetails": {
    "name": "Tony Stark",
    "emailAddress": "stark.tony@starkind.com"
  }
}
```

> Response status: 200
>
> Response body: N/A

#### Unhappy Path:
To trigger the unhappy paths, ensure you provide a valid request body.<br>
The below error responses can be expected:

HTTP 409 Duplicate Submission Error

```dtd
{
  "errorDetail": {
    "timestamp" : "2023-03-11T08:20:44Z",
    "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
    "errorCode": "409",
    "errorMessage": "Duplicate submission",
    "source": "Back End",
    "sourceFaultDetail": {
      "detail": [
        "Duplicate submission"
      ]
    }
  }
}
```

> Response status: 409
>
> Response body: N/A

HTTP 503 Service Unavailable Error

```dtd
{
  "errorDetail": {
    "timestamp": "2016-08-23T18:15:41Z",
    "correlationId": "",
    "errorCode": "503",
    "errorMessage": "Send timeout",
    "source": "Back End",
    "sourceFaultDetail": {
      "detail": ["101504 - Timeout "]
    }
  }
}
```

> Response status: 503
>
> Response body: N/A

HTTP 404 Record Not Found Error

```dtd
{
  "errorDetail": {
    "source": "Back End",
    "timestamp": "2020-11-23T13:19:52.307Z",
    "errorMessage": "Record not found",
    "errorCode": "404",
    "correlationId": "36147652-e594-94a4-a229-23f28e20e841",
    "sourceFaultDetail": {
      "detail": [
        "Detail cannot be found"
      ]
    }
  }
}
```

> Response status: 404
>
> Response body: N/A

---

```
GET /pillar2/subscription/:plrReference
```

Retrieves the Subscription details for the specific plrReference

#### Happy Path:
To trigger the happy path, ensure you provide a valid plrReference

The below is the expected success response:

> Response status: 200
>
> Response body:

```dtd
{
  "success": {
      "plrReference": "[pillar2Reference]",
      "processingDate": "2010-12-12",
      "formBundleNumber": "119000004320",
      "upeDetails": {
          "domesticOnly": false,
          "organisationName": "International Organisation Inc.",
          "customerIdentification1": "12345678",
          "customerIdentification2": "12345678",
          "registrationDate": "2022-01-31",
          "filingMember": false
      },
      "upeCorrespAddressDetails": {
          "addressLine1": "1 High Street",
          "addressLine2": "Egham",
          "addressLine3": "Surrey",
          "postCode": "HP13 6TT",
          "countryCode": "GB"
      },
      "primaryContactDetails": {
          "name": "Fred Flintstone",
          "telepphone": "0115 9700 700",
          "emailAddress": "fred.flintstone@aol.com"
      },
      "secondaryContactDetails": {
          "name": "Donald Trump",
          "telepphone": "0115 9700 700",
          "emailAddress": "fred.flintstone@potus.com"
      },
      "filingMemberDetails": {
          "safeId": "XL6967739016188",
          "organisationName": "Domestic Operations Ltd",
          "customerIdentification1": "1234Z678",
          "customerIdentification2": "1234567Y"
      },
      "accountingPeriod": {
          "startDate": "2024-01-06",
          "endDate": "2025-04-06",
          "duetDate": "2024-04-06"
      },
      "accountStatus": {
          "inactive": true
      }
  }
}
```

#### Unhappy Path:
To trigger the unhappy paths, ensure you provide a valid request body.
The below error responses can be expected:

HTTP 500 Internal Server Error

```dtd
{
  "errorDetail": {
    "timestamp": "2016-08-23T18:15:41Z",
    "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
    "errorCode": "500",
    "errorMessage": "Internal error",
    "source": "Internal Server error"
  }
}
```

> Response status: 500
>
> Response body: N/A

HTTP 400 Bad Request Error

```dtd
{
  "errorDetail": {
    "timestamp" : "2023-02-14T12:58:44Z",
      "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
      "errorCode": "400",
      "errorMessage": "Invalid ID",
      "source": "Back End",
      "sourceFaultDetail":{
        "detail":[
          "001 - Invalid Regime"
        ]
      }
  }
}
```

> Response status: 400
>
> Response body: N/A

HTTP 503 Service Unavailable Error

```dtd
{
  "errorDetail": {
    "timestamp": "2016-08-23T18:15:41Z",
    "correlationId": "",
    "errorCode": "503",
    "errorMessage": "Send timeout",
    "source": "Back End",
    "sourceFaultDetail": {
      "detail": ["101504 - Timeout "]
    }
  }
}
```

> Response status: 503
>
> Response body: N/A

HTTP 503 Request Could not be processed Error

```dtd
{
  "errorDetail": {
    "source": "Back End",
    "timestamp": "2020-11-11T13:19:52.307Z",
    "errorMessage": "Request could not be processed",
    "errorCode": "503",
    "correlationId": "c182e731-2386-4359-8ee6-f911d6e5f4bc",
    "sourceFaultDetail": {
      "detail": [
        "001 - Request Cannot be processed"
      ]
    }
  }
}
```

> Response status: 503
>
> Response body: N/A

HTTP 404 Record Not Found Error

```dtd
{
  "errorDetail": {
    "source": "Back End",
    "timestamp": "2020-11-23T13:19:52.307Z",
    "errorMessage": "Record not found",
    "errorCode": "404",
    "correlationId": "36147652-e594-94a4-a229-23f28e20e841",
    "sourceFaultDetail": {
      "detail": [
        "Detail cannot be found"
      ]
    }
  }
}
```

> Response status: 404
>
> Response body: N/A
---

```
GET /enrolment-store-proxy/enrolment-store/enrolments/:serviceName/groups
```

Retrieves the Enrolment Store Response with and without groupId

#### Happy Path:
To trigger the happy path, ensure you provide a valid plrReference

The below is the expected success response:

Enrolment Store Response with groupID
> Response status: 200
>
> Response body:

```dtd
{
  "principalGroupIds": [
    "GHIJKLMIN1234567",
    "GHIJKLMIN1234568"
  ],
  "delegatedGroupIds": [
    "GHIJKLMIN1234567",
    "GHIJKLMIN1234568"
  ]
}
```

Enrolment Store Response without groupID
> Response status: 200
>
> Response body: {}


#### Unhappy Path:

> Response status: _
>
> Response body: NoContent

<br><br>
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").