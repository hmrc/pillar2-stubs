# Table of Contents
* [pillar2\-stubs](#pillar2-stubs)
    * [Running the service locally](#running-the-service-locally)
        * [To compile the project:](#to-compile-the-project)
        * [To check code coverage:](#to-check-code-coverage)
        * [Integration and unit tests](#integration-and-unit-tests)
        * [Starting the server in local](#starting-the-server-in-local)
        * [Using Service Manager](#using-service-manager)
            * [Using sbt](#using-sbt)
    * [Endpoints](#endpoints)
        * [Happy Path:](#happy-path)
        * [Unhappy Path:](#unhappy-path)
        * [Happy Path:](#happy-path-1)
        * [Unhappy Path:](#unhappy-path-1)
        * [Happy Path:](#happy-path-2)
        * [Unhappy Path:](#unhappy-path-2)
        * [Happy Path:](#happy-path-3)
        * [Unhappy Path:](#unhappy-path-3)
    * [BARS Business account test data](#bars-business-account-test-data)
    * [Financial Data \- Get Financial Test Data](#financial-data---get-financial-test-data)
        * [Test last seven years of transactions](#test-last-seven-years-of-transactions)
    * [Get Obligation \- Get Obligation Test Data](#get-obligation---get-obligation-test-data)
    * [Post Liability](#post-liability)
    * [Below Threshold Notification](#below-threshold-notification)


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

| plrReference    | Status Code | Status                | Description                                                            |
|-----------------|-------------|-----------------------|------------------------------------------------------------------------|
| XEPLR0123456400 | 400         | BAD_REQUEST           | Submission has not passed validation. Invalid plrReference.            |
| XEPLR0123456404 | 404         | NOT_FOUND             | Submission has not passed validation. Record not found.                |
| XEPLR0123456422 | 422         | CANNOT_COMPLETE_REQUEST  | Request could not be completed because the subscription is being created or amended.                 |
| XEPLR0123456500 | 500         | INTERNAL_SERVER_ERROR | Internal Server error.                                                 |
| XEPLR0123456503 | 503         | SERVICE_UNAVAILABLE   | Dependent systems are currently not responding.                        |
| XEPLR5555555555 | 200         | OK                    | Returns read success response with accountStatus.inactive set to true. |
| XEPLR6666666666 | 200         | OK                    | Returns read success response with upe registration year of 2011. |
| XEPLR1066196600 | 200         | OK                    | Returns read success response with domesticOnly set to true.        |
| XEPLR__________ | 200         | OK                    | Returns read success response .                                        |

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

> Response status: use - XEPLR0444444400
>
> Response body: NoContent

<br><br>

## BARS Business account test data

| Sort code | Account number | Company Name          | Valid | Error Returned                                                                                                                                           |
|-----------|----------------|-----------------------|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| 206705    | 86473611       | Epic Adventure Inc    | Yes   | Successful Response <br/> "Epic Adventure Inc" returns nameMatches = Yes <br/> "Foo" returns nameMatches = No <br/> "Epic" returns nameMatches = Partial |
| 206705    | 86563612       | Sanguine Skincare     | Yes   | accountNumberIsWellFormatted = No                                                                                                                        |
| 206705    | 76523611       | Vortex Solar          | Yes   | accountExists = No                                                                                                                                       |
| 206705    | 56523611       | Innovation Arch       | Yes   | accountExists = inapplicable                                                                                                                             |
| 206705    | 56945688       | Eco Focus             | Yes   | accountExists = indeterminate                                                                                                                            |
| 207102    | 86473611       | Flux Water Gear       | Yes   | accountExists = error                                                                                                                                    |
| 207102    | 86563611       | Lambent Illumination  | Yes   | nameMatches = No                                                                                                                                         |
| 207102    | 76523611       | Boneféte Fun          | Yes   | nameMatches = inapplicable                                                                                                                               |
| 207102    | 56523611       | Cogent-Data           | Yes   | nameMatches = indeterminate                                                                                                                              |
| 207102    | 74597611       | Cipher Publishing     | Yes   | nameMatches = error                                                                                                                                      |
| 207106    | 86473611       | Security Engima       | Yes   | sortCodeIsPresentOnEISCD = no                                                                                                                            |
| 207106    | 86563611       | Megacorp              | Yes   | sortCodeIsPresentOnEISCD = error                                                                                                                         |
| 207106    | 76523611       | Genomics Inc          | Yes   | nonStandardAccountDetailsRequiredForBacs = Yes                                                                                                           |
| 207106    | 56523611       | Full Force Futures    | Yes   | sortCodeSupportsDirectCredit = error                                                                                                                     |
| 207106    | 74597611       | Resource Refresh      | Yes   | sortCodeIsPresentOnEISCD = No, nameMatches = No, accountExists = No                                                                                      |
| 609593    | 96863604       | O'Connor Construction | Yes   | accountNumberIsWellFormatted = indeterminate, but accountExists = Yes                                                                                    |
| 609593    | 96113600       | Candyland Consulting  | Yes   | accountNumberIsWellFormatted = indeterminate, but accountExists = No                                                                                     |

<br><br>

## Financial Data - Get Financial Test Data

| idNumber (PLR Reference Number)                                          | Response Returned                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| XEPLR4000000000                                                          | INVALID_IDNUMBER Error Response                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| XEPLR4040000000                                                          | NOT_FOUND Error Response                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| XEPLR5000000000                                                          | SERVER_ERROR Error Response                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| XEPLR5030000000                                                          | SERVICE_UNAVAILABLE Error Response                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| XMPLR0000000(Last three digits are the number of transactions displayed) | For example <br/> - XMPLR0000000022 will display 22 transactions 12 refund and 12 payments. <br/> - XMPLR0000000122 will display 122 transactions 61 refunds and 61 payments <br/> **Please note** <br/> - Use even numbers since 13 will default to 6 refund and 6 payment <br/> - All returned values are randomised so figures won't be consistent <br/> Please note a user must be able to see only last 7 years of transactions on their account, to test read note below  |
| Any valid ID                                                             | Will return 10 transactions these values are consistent                                                                                                                                                                                                                                                                                                                                                                                                                         |
| XEPLR5555551111                                                          | Will return success response with registration date set to the current date                                                                                                                                                                                                                                                                                                                                                                                                     |                                               

### Test last seven years of transactions
As it currently stands the end date is always set to today's date, this means that it will generate transactions from the registration date to today's date.
<br/>
In the stubs the registration date is always 2024-01-31 therefore to override this date you need to override the config value set in the `pillar2-frontend` service.
<br/>
Example:
```sbt "-Dapplication.router=testOnlyDoNotUseInAppConf.Routes" "-Dfeatures.transactionHistoryEndDate=2044-01-31" run```
<br/>
This will set the end date to `2044-01-31` and generate transactions from `2037-01-31 to 2044-01-31`. The last seven years.
Worth noting this won't happen in any other environment apart unless you override the config.


## Get Obligation - Get Obligation Test Data

For now this API has not been developed by ETMP therefore we are making assumptions in order to provide test data and satisfy the requirements of the frontend.

| idNumber (PLR Reference Number)                                          | Response Returned                       |
|--------------------------------------------------------------------------|-----------------------------------------|
| XEPLR1000000000                                                          | Obligation with Fulfilled status        |
| XEPLR4040000000                                                          | NOT_FOUND Error Response                |
| Any valid ID                                                             | Will return a response with Open status |


## Post Liability

Liability Detail Submission

This endpoint allows submission of liability details based on a provided idNumber (PLR Reference Number). There are two main types of submissions supported:
•	Liability Submission: A detailed submission of liability amounts.
•	Nil Return Submission: A minimal submission indicating no liability for the period.

Request Types and Expected Payloads

Liability Submission

A valid liability submission includes details about the total liabilities and entities liable for the tax period. Here’s the expected structure for a successful liability submission:
```
{
  "accountingPeriodFrom": "2024-08-14",
  "accountingPeriodTo": "2024-12-14",
  "qualifyingGroup": true,
  "obligationDTT": true,
  "obligationMTT": true,
  "electionUKGAAP": true,
  "liabilities": {
    "totalLiability": 10000.99,
    "totalLiabilityDTT": 5000.99,
    "totalLiabilityIIR": 4000,
    "totalLiabilityUTPR": 10000.99,
    "liableEntities": [
      {
        "ukChargeableEntityName": "Newco PLC",
        "idType": "CRN",
        "idValue": "12345678",
        "amountOwedDTT": 5000,
        "electedDTT": true,
        "amountOwedIIR": 3400,
        "amountOwedUTPR": 6000.5,
        "electedUTPR": true
      }
    ]
  }
}
```

Nil Return Submission

A Nil Return submission is used when there is no liability for the specified period. The returnType field in liabilities should be set to "NIL_RETURN":
```
{
  "accountingPeriodFrom": "2024-08-14",
  "accountingPeriodTo": "2024-09-14",
  "qualifyingGroup": true,
  "obligationDTT": true,
  "obligationMTT": true,
  "electionUKGAAP": true,
  "liabilities": {
    "returnType": "NIL_RETURN"
  }
}
```

Response Codes and Conditions

| Status          | Description                                                                               |
|-----------------|-------------------------------------------------------------------------------------------|
| 201 CREATED     | Success response for a valid liability or Nil Return submission when idNumber is correct. |
| 400 BAD_REQUEST | Submission did not pass validation (e.g., invalid JSON format or required fields missing).    |
| 400 BAD_REQUEST | Non-JSON data received, expecting a valid JSON object.                                    |
| 404 NOT_FOUND   | No liabilities found for the provided idNumber (PLR Reference Number is incorrect).     |

Examples of Invalid Requests

Invalid JSON

A request with invalid JSON syntax will return a 400 BAD_REQUEST response:

        ```json
        {
        "accountingPeriod": "2024-08-1",
        "accountingPeriod": "2024-12-14"
        }
        ```

Non-JSON Body

If a non-JSON body is submitted, a 400 BAD_REQUEST response will be returned:

            ```
            This is not a JSON body
            ```
Details of Expected Fields

	•	idNumber (PLR Reference Number): Only the idNumber “XTC01234123412” will result in a successful 201 CREATED response.
	•	Valid idNumber: Returns 201 CREATED with the liability success details for valid liability submissions.
	•	Invalid idNumber: Returns 404 NOT_FOUND, indicating no matching liability data for other idNumbers.
	•	Liability Fields: In a liability submission, totalLiability, totalLiabilityDTT, totalLiabilityIIR, and totalLiabilityUTPR are expected fields. Additionally, liableEntities should be a non-empty array.
	•	Nil Return Field: In a Nil Return submission, liabilities.returnType should be "NIL_RETURN", indicating no liability.

Response Examples

Successful Liability Submission Response

If the idNumber is valid and the payload is correct, a 201 CREATED response will be returned with liability details:
    ```
        {
        "success": {
        "processingDate": "2024-08-14T09:26:17Z",
        "formBundleNumber": "119000004320",
        "chargeReference": "XTC01234123412"
        }
        }
    ```

Successful Nil Return Response

If the idNumber is valid and the payload indicates a Nil Return, a 201 CREATED response will be returned with the Nil Return details:
    ```
        {
        "success": {
        "processingDate": "2024-08-14T09:26:17Z",
        "message": "Nil return received and processed successfully"
        }
        }
    ```

## Below Threshold Notification

This endpoint allows submission of a below threshold notification, which defines an organisation as earning below the threshold
that makes them eligible for submitting pillar2 UKTR.

For this API, the payload is rather simple, so responses are limited in their scope

| Pillar2Id | Response Returned |
| ---------- | ---------------- |
| XEPLR4000000000 | BadRequest response |
| XEPLR5000000000 | InternalServerError response |
| Any valid ID    | Successful response |

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").