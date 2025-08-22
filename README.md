# API-Framework: SKU API Test Automation

## Overview
This project provides automated acceptance and regression tests for a retail SKU (Stock Keeping Unit) API. The API supports CRUD operations for SKUs, including creation, retrieval, update, and deletion. The framework is designed to support both production (real API) and development (mocked API) modes.

## API Under Test
- **POST /api/skus**: Create or update a SKU. Expects JSON body with `sku`, `description`, and `price`.
- **GET /api/skus**: Retrieve all SKUs.
- **GET /api/skus/{id}**: Retrieve a SKU by ID.
- **DELETE /api/skus/{id}**: Delete a SKU by ID.

## Test Approach
- **Modes**: Tests can run against the real API (production) or a mocked API (development, using WireMock).
- **Coverage**: Tests cover normal, edge, and error cases for all endpoints.
- **ATDD Style**: Tests are written to reflect acceptance criteria and expected behaviors.
- **Feedback**: Assertion failures are clear and focused on service behavior, not test code.

## Running the Tests

### Prerequisites
- Java 17+
- Maven

### Modes
- **Development (default)**: Uses WireMock to mock the API.
- **Production**: Uses the real API at `https://example.com/api/skus`.

### Commands
- **Run all tests (development mode):**
  ```
  mvn test
  ```
- **Run all tests (production mode):**
  ```
  mvn test -Dtest.mode=PRODUCTION
  ```
  - To run only Smoke tests:  
  ```
  mvn test -Dtest.group=Smoke
  ```
- To run only Regression tests:  
```
  mvn test -Dtest.group=Regression
  ```  
- To run all tests (default): 
``` 
  mvn test -Dtest.group=all  
  ```
  or simply  
  ```
  mvn test
  ```

## Test Intentions
- Validate all CRUD operations for SKUs.
- Ensure the API handles both valid and invalid requests gracefully.
- Provide fast feedback to developers before and after API deployment.

## Extending the Framework
- Add new test cases by extending `BaseApiTest` and using the configuration utility for endpoint selection.
- Add new WireMock stubs in `setupStubs()` for additional endpoints or scenarios.

---

For questions or contributions, please contact the project maintainer.
