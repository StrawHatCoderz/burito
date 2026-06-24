# Burito Project — Agent Rules

These rules apply to ALL work in this repository, regardless of which skill is active.

---

## 1. Test With Code, Not After Code

Every piece of new logic must have its tests written **in the same step**, not deferred.
If you write a method, write its test before moving to the next method. If you finish
implementation and then start writing tests as a separate phase, you are doing it wrong.

Before any commit, verify test coverage on the changed code using the project's coverage
tool (JaCoCo). New code must not degrade coverage. If tests reveal a bug in the
implementation, fix the implementation — the test is telling you the truth.

---

## 2. Respect Architectural Boundaries

Every codebase has a layered structure. Before writing code, identify the layers
(e.g., controller → service → repository) and **never violate the dependency direction**.

- A layer must only depend on the layer directly below it.
- If you find yourself importing a class from a layer above (e.g., a service importing
  a controller), you have a circular dependency — stop and refactor.
- If you find yourself skipping a layer (e.g., a controller importing a repository
  directly), extract the logic into the appropriate intermediate layer.
- Utility/mapping logic does not belong on controllers. If a mapper, converter, or
  helper method is needed by multiple layers, it belongs in its own dedicated class.

---

## 3. Consistency Over Cleverness

Before introducing any pattern (response wrapping, exception handling, date types,
naming conventions), **check how the existing codebase already handles the same concern**.
Follow the established pattern exactly. If two competing patterns exist, flag it to the
user rather than picking one silently.

This applies to:
- Exception types and how they are thrown
- API response envelope structures
- Date/time types across entities
- Naming conventions for files, classes, methods, and test methods
- Import styles and package organisation

If no established pattern exists, propose one to the user and get agreement before
writing code.

---

## 4. Verify Against Actual Code — Never Assume

Before writing any code that depends on an interface, return type, method signature,
or class structure — **read the actual source file** and verify it. Do not rely on
memory, naming conventions, or what "should" be there.

Common traps:
- Assuming a repository method returns `Optional<T>` when it returns `T` directly
- Assuming a DTO is a record when it is a class (or vice versa)
- Assuming a field is nullable when it is a primitive
- Assuming an annotation exists on a class without checking

If you get a compilation or test failure, the root cause is almost always that you
assumed something instead of verifying it. Read the source first.

---

## 5. No Dead Weight

Do not introduce code that is not immediately used. This includes:
- Enum values with no reference anywhere in the codebase
- DTOs or view classes defined but never sent or received
- Config classes that duplicate or conflict with framework auto-configuration
- `TODO` comments committed without a linked story or issue

Before every commit, verify that every new symbol (class, method, field, enum value)
you introduced is actually referenced. If it isn't, remove it.

---

## 6. Single Responsibility at Every Level

Each class, method, and module should have one reason to change. If a class is doing
two jobs (e.g., a controller that also contains business logic, authorization checks,
and data mapping), split it. The question to ask is: *"If I needed to change just the
mapping logic, how many files would I touch?"* If the answer is a controller, the
mapping logic is in the wrong place.

---

## 7. Tech Stack Reference

| Concern            | Standard                                                        |
|--------------------|-----------------------------------------------------------------|
| Framework          | Spring Boot 3.4+                                                |
| Test Annotations   | `@MockitoBean` (not deprecated `@MockBean`)                     |
| Test Slices        | `@WebMvcTest` from `org.springframework.boot.test.autoconfigure.web.servlet` |
| Exceptions         | `APIException` hierarchy with static factory methods            |
| API Responses      | `APIResponse<T>` wrapper on all controller return values        |
| Timestamps         | `LocalDateTime` for all date-time fields across entities        |
| Coverage Tool      | JaCoCo — run `jacocoTestReport` and verify before committing    |

---

## 8. Frontend-Backend API Response Contracts

All backend controllers wrap response payloads inside the standard `APIResponse<T>` envelope. To maintain consistent behavior and prevent runtime crashes:

- **Frontend Client Data Extraction**:
  All frontend API client methods hitting wrapped backend endpoints must explicitly unwrap the payload using `data.data` (e.g. `const { data } = await client.get<ApiResponse<T>>(...)` and return `data.data`), rather than returning the raw Axios response body (`data`).
  - **Exception**: Authentication endpoints, which return tokens directly within the nested payload (`response.data.accessToken` and `response.data.refreshToken`).

- **Test Mock Alignment**:
  Any mock setup in frontend testing environments must mimic this exact response envelope structure. Network mock results must resolve inside a `{ data: { success: true, data: mockObject, error: null } }` object to ensure they align with the real backend.

- **Unified Error Parsing**:
  Always use the central `extractErrorMessage(error)` utility in catch blocks rather than accessing raw Axios messages (`err.message` or `err.response?.data?.message`). This correctly retrieves wrapped backend errors (`error.message`) and provides robust fallbacks for network issues.
