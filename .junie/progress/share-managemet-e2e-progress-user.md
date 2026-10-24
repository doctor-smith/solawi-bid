## 24.10.26
1) Second test is passing: B

## 22.10.2026 (morning 9:55)
1) First devTest is passing :-) : `provider approves pending subscription without payment mandate`

### Done:
#### Suggestions from the last meeting:
- [x] Add the source url to the failure network log entries
- [x] Also added a timestamp to the requests in failure network log
- [x] Add a timestamp / date to the progress file etc for better tracking
- [x] Added possibility to run ./e2e-local.sh with filters
- 
### Fixed known issues in E2E code:
[x] all fixed

### Known issues in the source code:
still there -> next sprint

## Issues where the main blame is unclear
much better

## till 20.10.2026:
### I fixed some bugs: 
1. in the e2e code: in some forms, some fields have not been considered at all. 
2. role assignment did not work at all, now it does 
 


- I added another test annotated with the new annotation @Develop. Here i navigate by clicking on links and buttons, which works much better.  
- I also used assertThat(<element>).<function>() instead of waitfor to stabilize test runs.
- I added the context header to the entries in the failure-network.log file

### Suggestions
- Add the source url to the failure network log entries 
- Add a timestamp / date to the progress file etc for better tracking

## Known Issues in the E2E code
1) the tests are flaky / unstable - from time to time they fail on completely fine production code. 
2) I first added screenshots to the e2e tests to understand what is going on. then the test passed continuously. After I removed them due to performance considerations, the test almost never passed again and failed at random steps.
3) No possibility to run ./e2e-local.sh with filters
4) The selection of the user on the UpsertShareSubscriptionModal does not work reliably. Sometimes Playwright does not register the change 

## Known Issues in Source Code
1) Page Reload does not really work on all pages. It happens that data is not present after full refresh, while it is there if you just navigate by clicking through the app
2) Navigating to the organization-application pages works fine if one navigates from the dashboard to the right organization using the organization card. But using the dedicated Application card (Banking, Shares, Distribution) to move the List of orgs using the application and then to the organization-application page leads to an incomplete store in some cases (-> clarify which)



### There are three requests leading to errors:

1)
=== GET http://localhost:18081/banking/bank-accounts/all?legal_entity=e359dc66-cfa8-44d1-8683-991c9836c78f -> 403 ===
context-header: fa29e904-9acd-4ccc-94bf-0ba3ba177920
response body: {"value":"Access denied"}

2)
=== GET http://localhost:18081/banking/legal-entities/personal?party=e359dc66-cfa8-44d1-8683-991c9836c78f -> 404 ===
context-header: fa29e904-9acd-4ccc-94bf-0ba3ba177920
response body: {"value":"No such legal entity party: id = e359dc66-cfa8-44d1-8683-991c9836c78f"}

3)
=== GET http://localhost:18081/banking/creditors/identifiers/by-legal-entity?legal_entity=466026bc-3773-4212-b241-6660a7362ccb -> 500 ===
context-header: fa29e904-9acd-4ccc-94bf-0ba3ba177920
response body: {"value":"No such context; id = with app=org.solyton.solawi.bid.module.application.schema.Application@6b744d2e, org = 466026bc-3773-4212-b241-6660a7362ccb"}

These are bugs in the source code. But they are not severe. 
I will fix them in the next sprint. 
These are all frontend issues. 
  - 1) and 2) can be avoided by permission checks 
  - 3) here we have to assure that the corresponding page will only be rendered if a context entity is defined and stored 


### Frontend
1. UpsertFiscalYearsModal:
   - Forbidden time intervals make no sense if there are no other fiscal years yet


## Issues where the main blame is unclear
1) Usage of kotlin compose web in forms leads to rerendering. This is fine for the app, but Playwright struggles hard with it -> Race conditions. Errors occur, which do not occur in production. 

