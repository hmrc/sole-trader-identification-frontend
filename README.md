
# Sole Trader Identification Frontend

This is a Scala/Play frontend to allow Sole Traders to provide their information to HMRC.

### How to run the service
1. Make sure any dependent services are running using the following service-manager command `sm --start SOLE_TRADER_IDENTIFICATION_ALL -r`
2. Stop the frontend in service manager using `sm --stop SOLE_TRADER_IDENTIFICATION_FRONTEND`
3. Run the frontend locally using
`sbt 'run 9717 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`
   
## Testing

---
See [TestREADME](TestREADME.md) for more information about test data and endpoints

## End-Points

### POST /sole-trader-identification/api/sole-trader-journey

---
Creates a new journey for a Sole Trader, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.
optFullNamePageLabel if present will allow the calling service to customise title and H1 of the starting journey page.

All other fields must be provided.

The businessVerificationCheck field allows the calling service to bypass the verification and 
continue to register where a successful match is found. By default "businessVerificationCheck" will be true.

```
{
    "continueUrl" : "/test",
    "businessVerificationCheck" : true ,
    "optServiceName" : "Service Name",
    "deskProServiceId" : "abc",
    "signOutUrl" : "/sign-out",
    "accessibilityUrl" : "/accessibility-statement/my-service",
    "optFullNamePageLabel" : "What is the name of the nominated partner?",
    "regime" : "VATC"
}
```

### POST /sole-trader-identification/api/individual-journey

---
Creates a new journey for an Individual, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.
optFullNamePageLabel if present will allow the calling service to customise title and H1 of the starting journey page.

All other fields must be provided.

```
{
    "continueUrl" : "/test",
    "optServiceName" : "Service Name",
    "deskProServiceId" : "abc",
    "signOutUrl" : "/sign-out",
    "accessibilityUrl" : "/accessibility-statement/my-service",
    "optFullNamePageLabel" : "What is the name of the nominated partner?",
    "regime" : "VATC"
}
```

#### Response:
Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "/testUrl"}
```

### GET /sole-trader-identification/api/journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.
#### Request:
A valid journeyId must be sent in the URI

#### Response:
Status:

| Expected Response                       | Reason
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response body for the Sole Trader Flow:
```
{
    "firstName": "John",
    "lastName": "Smith",
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

Example response body for the Individual Flow:
```
{
    "firstName": "John",
    "lastName": "Smith",
    "dateOfBirth": 1978-01-05,
    "nino": "AA111111A",
    "identifiersMatch": true
}
```

### POST /sole-trader-identification/api/journey
### Deprecated - use POST /sole-trader-identification/api/sole-trader-journey instead

---
Creates a new journey for a Sole Trader, storing the journeyConfig against the journeyId.
#### Request:

optServiceName will default to `Entity Validation Service` if the field is not provided.
optFullNamePageLabel if present will allow the calling service to customise title and H1 of the starting journey page.

The enableSautrCheck field allows the calling service to decide whether to ask the user to provide
an SAUTR that will be verified. By default "enableSautrCheck" will be false.

All other fields must be provided.

The businessVerificationCheck field allows the calling service to bypass the verification and
continue to register where a successful match is found. By default "businessVerificationCheck" will be true.

```
{
    "continueUrl" : "/test",
    "businessVerificationCheck" : true ,
    "optServiceName" : "Service Name",
    "deskProServiceId" : "abc",
    "signOutUrl" : "/sign-out",
    "enableSautrCheck" : "true",
    "optFullNamePageLabel" : "What is the name of the nominated partner?"
    "regime" : "VATC"
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
