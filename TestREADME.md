# Sole Trader Identification Frontend Test End-Points

## Testing

---

1. [Setting up a Sole Trader Journey](TestREADME.md#get-test-onlycreate-sole-trader-journey)
2. [Setting up an Individual Journey](TestREADME.md#get-test-onlycreate-individual-journey)
3. [Retrieving Journey Data](TestREADME.md#get-test-onlyretrieve-journeyjourneyid-or-test-onlyretrieve-journey)
4. Business Verification Stub
   - [Create Journey](TestREADME.md#post-test-onlybusiness-verificationjourney)
   - [Retrieve Result](TestREADME.md#get--test-onlybusiness-verificationjourneyjourneyidstatus)
5. Nino Identity Verification Stub
   - [Create Journey](TestREADME.md#post-test-onlynino-identity-verificationjourney)
   - [Retrieve Result](TestREADME.md#get--test-onlynino-identity-verificationjourneyjourneyidstatus)
6. Authenticator stub
   - [Test Data](TestREADME.md#using-the-authenticator-stub)
7. Known Facts stub
   - [Test Data](TestREADME.md#using-the-known-facts-stub)
   

### GET test-only/feature-switches

---
Shows all feature switches:
1. Sole Trader Identification Frontend

    - Use stub for Authenticator API
    - Use stub for Business Verification flow
    - Use stub for Known Facts API
    - Enable no nino journey (this is set to true in production)
   
2. Sole Trader Identification (see Sole Trader Identification TestREADME for more info)
   
   - Use stub for get SA Reference
   - Use stub for submissions to DES

### GET /test-only/create-sole-trader-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for a Sole Trader.

1. Continue URL (Required)

   - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

   - Service Name to use throughout the service
   - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

   - Used for the Beta feedback link
   - This is currently autofilled but can be changed

4. Sign Out Link (Required)

   - Shown in the HMRC header - typically a link to a feedback questionnaire
   - This is currently autofilled but can be changed
   
5. Business verification checkbox

   - Used for skipping further verification checks carried out currently by Business Verification (SI)
   - This is currently autofilled but can be changed
   
6. Accessibility Statement URL

   - Shown in the footer - a link to the accessibility statement for the calling service
   - This is currently autofilled but can be changed

7. Full Name Page Label (Optional)

   - To customise title and H1 of the starting journey page
   - Currently, this is empty by default, so the default page title and H1 will be used

8. Regime (Required)

   - This is the Tax Regime Identifier
   - It is passed down to the Registration API
   - This is currently defaulted to VATC but accepted values are PPT or VATC

9. Welsh translation for Full Name Page Label (Optional)

  - Welsh language version of full name page label (item 7)

10. Welsh translation for Service Name (Optional)

  - Welsh language translation for service name (item 2)

### GET /test-only/create-individual-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for an Individual.

1. Continue URL (Required)

   - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

   - Service Name to use throughout the service
   - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

   - Used for the Beta feedback link
   - This is currently autofilled but can be changed

4. Sign Out Link (Required)

   - Shown in the HMRC header - typically a link to a feedback questionnaire
   - This is currently autofilled but can be changed

5. Accessibility Statement URL

   - Shown in the footer - a link to the accessibility statement for the calling service
   - This is currently autofilled but can be changed

6. Full Name Page Label (Optional)

   - To customise title and H1 of the starting journey page
   - Currently, this is empty by default, so the default page title and H1 will be used

7. Regime (Required)

   - This is the Tax Regime Identifier
   - It is passed down to the Registration API
   - This is currently defaulted to VATC but accepted values are PPT or VATC

8. Welsh translation for Full Name Page Label (Optional)

   - Welsh language version of full name page label (item 6)

9. Welsh translation for Service Name (Optional)

   - Welsh language translation for service name (item 2)
   

### GET /test-only/create-journey
#### Deprecated - use GET /test-only/create-sole-trader-journey

---
This is a test entry point which simulates a service by triggering the initial POST call to set up a journey for a Sole Trader.

1. Continue URL (Required)

   - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

   - Service Name to use throughout the service
   - Currently, this is empty by default, so the default service name will be used

3. DeskPro Service ID (Required)

   - Used for the Beta feedback link
   - This is currently autofilled but can be changed

4. Sign Out Link (Required)

   - Shown in the HMRC header - typically a link to a feedback questionnaire
   - This is currently autofilled but can be changed

5. Enable SAUTR Check (Optional)

   - Shows the user an extra page where they can enter an SAUTR
   - This is currently defaulted to false unless otherwise specified
   - If this is enabled, refer to Using the Authenticator Stub section below

6. Accessibility Statement URL

   - Shown in the footer - a link to the accessibility statement for the calling service
   - This is currently autofilled but can be changed
   
7. Full Name Page Label (Optional)

   - To customise title and H1 of the starting journey page
   - Currently, this is empty by default, so the default page title and H1 will be used

8. Regime (Required)

   - This is the Tax Regime Identifier
   - It is passed down to the Registration API
   - This is currently defaulted to VATC but accepted values are PPT or VATC

9. Welsh translation for Full Name Page Label (Optional)

   - Welsh language version of full name page label (item 7)

10. Welsh translation for Service Name (Optional)

   - Welsh language translation for service name (item 2)

### GET test-only/retrieve-journey/:journeyId or test-only/retrieve-journey

---
Retrieves all the journey data that is stored against a specific journeyID.

#### Request:
A valid journeyId must be sent in the URI or as a query parameter. Example of using the query parameter:

`test-only/retrieve-journey?journeyId=1234567`

#### Response:
Status:

| Expected Response                       | Reason                          |
|-----------------------------------------|---------------------------------|
| ```OK(200)```                           | ```JourneyId exists```          |
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```   |

Example response body for the Sole Trader Journey:

```
{
    "fullName": {
      "firstName": "John",
      "lastName": "Smith"
    },
    "dateOfBirth": 1978-01-05,
    "nino": "AA111111A",
    "sautr": "1234567890",
    "identifiersMatch": true,
    "businessVerification": {
        "verificationStatus":"PASS"
      },
    "registration": {
        "registrationStatus":"REGISTERED",
        "registeredBusinessPartnerId":"X00000123456789"
      }
}
```

Example response body for the Sole Trader Flow where the Registration failed:
```
{
    "fullName": {
      "firstName": "John",
      "lastName": "Smith"
    },
    "dateOfBirth": 1978-01-05,
    "nino": "AA111111A",
    "sautr": "1234567890",
    "identifiersMatch": true,
    "businessVerification": {
        "verificationStatus":"PASS"
      },
    "registration": {
        "registrationStatus":"REGISTRATION_FAILED",
        "failures": [
            {
                "code": "PARTY_TYPE_MISMATCH",
                "reason": "The remote endpoint has indicated there is Party Type mismatch"
            }
        ]
      }
}
```

Example response body for the Individual Flow:
```
{
    "fullName": {
      "firstName": "John",
      "lastName": "Smith"
    },
    "dateOfBirth": 1978-01-05,
    "nino": "AA111111A",
    "identifiersMatch": true
}
```

### POST test-only/business-verification/journey

---
Stubs creating a Business Verification journey. The Business Verification Stub Feature Switch will need to be enabled.

#### Request:
No body is required for this request

#### Response:
Status: **Created(201)**

Example Response body:

```
{“redirectUri” : "/testUrl?journeyId=<businessVerificationJourneyId>"}
```

### GET  test-only/business-verification/journey/:journeyId/status

---
Stubs retrieving the result from the Business Verification Service. The Business Verification Stub feature switch will need to be enabled.

The stub bypasses the whole Business Verification flow. Always returns BusinessVerification Status as PASS.

#### Request:
A valid Business Verification journeyId must be sent in the URI

#### Response:
Status: **OK(200)**

Example Response body:
```
{
  "journeyType": "BUSINESS_VERIFICATION",
  "origin": vat,
  "identifier": {
    "saUtr" -> "1234567890"
  },
  "verificationStatus" -> "PASS"
}
```

### POST test-only/nino-identity-verification/journey

---
Stubs creating a Nino Identity Verification journey. The Stub Nino IV journey feature switch will need to be enabled.

#### Request:
No body is required for this request

#### Response:
Status:

| Expected Response    | Reason                            | Example                    |
|----------------------|-----------------------------------|----------------------------|
| ```CREATED(201)```   | ```Journey Created```             | ```Any other valid Nino``` |
| ```NOT_FOUND(404)``` | ```Data for Nino doesn't exist``` | ```"BB222222B"```          |


Example Response body:

```
{“redirectUri” : "/testUrl?journeyId=<NinoVerificationJourneyId>"}
```

### GET  test-only/nino-identity-verification/journey/:journeyId/status

---
Stubs retrieving the result from the Nino Identity Verification Service. The Stub Nino IV journey feature switch will need to be enabled.

The stub bypasses the whole Nino Verification flow.

#### Request:
A valid Nino Verification journeyId must be sent in the URI

#### Response:
Status: **OK(200)**

Example Response body:
```
{
  "origin": vat,
  "identifier": {
    "nino" -> "AA111111A"
  },
  "verificationStatus" -> "PASS"
}
```

### Using the Authenticator stub

This stubs the call we make to /authenticator/match which attempts to match the data provided against a HOD record.

This stub returns different responses based on the entered last name.

`fail` & `deceased` will return a data mismatch which upon submitting CYA will redirect the user to an error page.

`no-sautr` will return the data the user has entered and allow them to pass successfully provided the enableSautrCheck boolean is false or the user has clicked the `I do not have an SAUTR` link when the boolean is enabled.

Any other last name will return the data user has entered along with `1234567890` for the SAUTR. This is the SAUTR the user must provide in order to pass validation using this stub.

| Last Name                               | Response               |
|-----------------------------------------|------------------------|
| ```fail```                              | ```Unauthorized```     |
| ```deceased```                          | ```FailedDependency``` |
| ```no-sautr```                          | ```Ok``` (See above)   |
| Any other last name                     | ```Ok```               |


### Using the Known Facts stub

This stubs the call to enrolment-store-proxy (ES20) that we make.

This stub returns different response bodies based on the sautr entered.

The 'Use stub for Known Facts API' feature switch will need to be enabled to use this.

#### Sautr: 0000000000
___
Mimics a response for users without a UK SA postcode. If user asserts they have no SA postcode and IsAbroad if returned as true then identifersMatch will be stored as true

Status: **OK(200)**

Response body:
```
{
 "service": "IR-SA",
 "enrolments": [{
     "identifiers": [{
         "key": "UTR",
         "value": "0000000000"
     }],
     "verifiers": [{
         "key": "IsAbroad",
         "value": "Y"
     }]
 }]
}
```

#### Sautr: 1234567891
___
Mimics a response for the user with NINO BB111111B, that declare SAUTR equals to 1234567891. If user asserts they have no Nino, the front end will show a retry page given the mismatch between what the user declared (No Nino) and what Known Facts has recorded for the user (NINO BB111111B).

Status: **OK(200)**

Response body:
```
{
 "service": "IR-SA",
 "enrolments": [{
     "identifiers": [{
         "key": "UTR",
         "value": "1234567891"
     }],
     "verifiers": [{
         "key": "NINO",
         "value": "BB111111B"
     }]
 }]
}
```

#### Any other sautr
___
Mimics a response for users with a UK postcode. If this matches the SA postcode entered by the user then identifiersMatch will be stored as true

Status: **OK(200)**

Response body:
```
{
 "service": "IR-SA",
 "enrolments": [{
     "identifiers": [{
         "key": "UTR",
         "value": "1234567890"
     }],
     "verifiers": [{
         "key": "Postcode",
         "value": "AA1 1AA"
     }]
 }]
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
