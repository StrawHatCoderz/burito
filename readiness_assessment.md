# Burito Codebase Readiness Assessment

**Date:** 2026-06-23  
**Assessor Role:** Senior Staff Engineer / Technical Program Manager  
**Scope:** Independent evidence-based audit to determine microservices migration readiness

---

## 1. Executive Summary

Burito is an early-stage food delivery monolith with approximately 6 features implemented across a Spring Boot backend and a React/Vite frontend. The application is described as a "modular monolith with bounded contexts" (Identity, Catalog, Ordering, Delivery, Notifications), but **this description does not match reality**: the codebase is organized by technical layer (`controller/`, `service/`, `repository/`, `domain/`), not by domain boundary. Of the five intended bounded contexts, only three have any implementation at all (Identity, Catalog, Ordering), while **Delivery and Notifications exist purely as concepts** — there is zero code for either. The codebase has multiple architectural inconsistencies: controllers bypassing the service layer to access repositories directly, three competing exception handling strategies coexisting without unified coverage, date/time type inconsistencies across entities, and an unused `CartStatus.BOOKED` enum value. Payment is a hardcoded stub. The test suite contains ~117 backend tests and ~43 frontend tests, but has zero coverage for the `AdminOrderController` (the most complex controller), zero WebSocket tests, and no end-to-end tests. CI runs builds and tests but enforces no coverage thresholds. **This codebase is not ready for a microservices migration.** It first needs to complete its feature surface, consolidate its architectural patterns, and establish bounded-context separation within the monolith before splitting would be anything other than premature.

---

## 2. Feature Completeness Matrix

| Feature | Backend Status | Frontend Status | Overall |
|---|---|---|---|
| **Customer Registration** | ✅ Complete (validation, persistence, JWT) | ✅ Complete (RegisterPage) | ✅ End-to-end |
| **Customer Login / Logout** | ✅ Complete (JWT + refresh tokens) | ✅ Complete (LoginPage, AuthContext, token storage) | ✅ End-to-end |
| **Admin Registration** | ✅ Complete (creates user + restaurant) | ✅ Complete (AdminRegisterPage) | ✅ End-to-end |
| **Admin Login** | ✅ Complete (JWT with restaurantId claim) | ✅ Complete (AdminLoginPage) | ✅ End-to-end |
| **Token Refresh** | ✅ Complete | ⚠️ No automatic refresh logic in frontend | ⚠️ Partial |
| **User Profile (read)** | ✅ Complete (`GET /api/me`) | ⚠️ NavBar shows email, no profile page | ⚠️ Partial |
| **User Profile (update)** | ❌ No endpoint exists | ❌ No UI | ❌ Missing |
| **Restaurant Listing** | ✅ Complete (search + cuisine filter) | ✅ Complete (RestaurantsPage with search) | ✅ End-to-end |
| **Restaurant Detail + Menu** | ✅ Complete | ✅ Complete (RestaurantDetailPage) | ✅ End-to-end |
| **Admin Restaurant Management** | ✅ Complete (update name, cuisine, open/close, image) | ✅ Complete (RestaurantProfileForm) | ✅ End-to-end |
| **Admin Menu CRUD** | ✅ Complete (create, update, delete, availability toggle) | ✅ Complete (MenuManager + MenuItemForm) | ✅ End-to-end |
| **Cart (add/remove/decrement/clear)** | ✅ Complete (user + guest carts, merge) | ✅ Complete (CartDrawer + CartContext) | ✅ End-to-end |
| **Checkout / Order Placement** | ✅ Complete (transactional, clears cart) | ✅ Complete (CartDrawer → checkout button) | ✅ End-to-end |
| **Payment** | ⚠️ **Stub** — always returns `true` | N/A | ⚠️ Stub |
| **Admin Order Dashboard** | ✅ Complete (list pending/accepted, update status) | ✅ Complete (AdminOrderDashboard) | ✅ End-to-end |
| **Active Order Tracking (customer)** | ✅ Complete (`GET /api/orders/active`) | ✅ Complete (ActiveOrderPage) | ✅ End-to-end |
| **Real-time: New Orders → Admin** | ✅ WebSocket broadcast on checkout | ✅ STOMP subscription in AdminOrderDashboard | ✅ End-to-end |
| **Real-time: Status → Customer** | ✅ `convertAndSendToUser` by email | ✅ OrderNotificationListener | ✅ End-to-end |
| **Real-time: Restaurant availability** | ✅ Broadcast on open/close toggle | ✅ useRestaurantSocket hook | ✅ End-to-end |
| **Real-time: Menu updates** | ✅ Broadcast on item CRUD / availability | ✅ useRestaurantSocket hook | ✅ End-to-end |
| **Order History** | ❌ No endpoint | ❌ No UI | ❌ Missing |
| **Delivery Tracking** | ❌ No code whatsoever | ❌ No UI | ❌ Missing |
| **Notifications (email/push)** | ❌ No code whatsoever | ❌ No UI | ❌ Missing |
| **Address Management** | ⚠️ Entity exists, seeded for restaurants only | ❌ No user address UI | ⚠️ Partial |
| **Ratings / Reviews** | ❌ Rating field on Restaurant entity only (static seed data) | ❌ No UI | ❌ Missing |
| **Admin Analytics / Reports** | ❌ No code | ❌ No UI | ❌ Missing |

### Key Observations

- **Two of the five claimed bounded contexts (Delivery, Notifications) have literally zero code.** Not even a placeholder service or interface.
- Payment is a `processPayment(Double amount)` stub that logs and returns `true`. No integration, no strategy pattern, no interface for future replacement.
- The `Address` entity is seeded for restaurants but has no user-facing CRUD. Users have no delivery address.
- Order history is absent — customers can only see their single active order.

---

## 3. Architectural Inconsistencies Found

### 3.1 No Bounded Context Package Structure

> **Severity: Critical for migration**

The codebase is organized purely by technical layer, not by domain:

```
com.burito/
├── controller/     ← ALL controllers from all contexts mixed together
├── service/        ← ALL services
├── repository/     ← ALL repositories
├── domain/         ← ALL entities
├── enums/          ← ALL enums
└── config/
```

**Evidence:** [backend/src/main/java/com/burito/](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito) — every domain object (`User`, `Restaurant`, `Cart`, `Order`, `MenuItem`, `Address`) lives in a single flat `domain/` package. There is zero separation that would guide a future split.

### 3.2 Controller Bypasses Service Layer — Direct Repository Access

> **Severity: High**

[AdminOrderController.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/controller/AdminOrderController.java) injects `OrderRepo`, `RestaurantRepo`, and `UserRepo` directly. It contains business logic (authorization checks, status transitions, WebSocket broadcasting) that belongs in a service class.

**Evidence:** Lines [30-33](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/controller/AdminOrderController.java#L30-L33):
```java
private final OrderRepo orderRepo;
private final RestaurantRepo restaurantRepo;
private final UserRepo userRepo;
private final SimpMessagingTemplate messagingTemplate;
```

Similarly, [OrderController.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/controller/OrderController.java#L22) injects `UserRepo` directly at line 22.

### 3.3 Static Utility Method on Controller — Architectural Inversion

> **Severity: High**

[AdminOrderController.mapToView()](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/controller/AdminOrderController.java#L111-L131) is a `public static` method on a controller that is **called by `OrderService`** (a service depending on a controller):

**Evidence:** [OrderService.java:86](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/service/OrderService.java#L86):
```java
OrderView view = AdminOrderController.mapToView(savedOrder);
```

This creates a circular dependency direction: `service → controller`, which is the opposite of clean architecture flow.

### 3.4 Three Competing Exception Handling Strategies

> **Severity: Medium**

The codebase uses three incompatible strategies simultaneously:

| Strategy | Where Used | Handled by GlobalExceptionHandler? |
|---|---|---|
| Custom `APIException` subclasses | `RestaurantNotFoundException`, `InvalidCredentialsException`, etc. | ✅ Yes |
| Spring `ResponseStatusException` | `AdminRestaurantService`, `AdminMenuService` (12 throw sites) | ❌ No — bypasses handler entirely |
| Raw `IllegalArgumentException` / `IllegalStateException` | `OrderService`, `CartService`, `WebSocketConfig` | ❌ No — results in 500 errors |

**Evidence:**
- [GlobalExceptionHandler.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/controller/GlobalExceptionHandler.java) handles only `APIException` and `MethodArgumentTypeMismatchException` (2 exception types)
- [AdminMenuService.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/service/AdminMenuService.java) throws `ResponseStatusException` at lines 31, 43, 63, 66, 93, 96
- [OrderService.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/service/OrderService.java) throws `IllegalStateException` at lines 53, 57, 64

### 3.5 `UserService.loadUserByUsername()` Hard-codes Role

> **Severity: Medium (correctness bug)**

[UserService.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/service/UserService.java#L28-L29) always sets `.roles("USER")` regardless of the actual `user.getRole()` value. This means `RESTAURANT_ADMIN` users would get `ROLE_USER` if authentication went through `UserDetailsService` (currently bypassed by the JWT filter, but still a latent bug that could surface if the auth flow changes).

### 3.6 Date/Time Type Inconsistency

> **Severity: Low**

| Entity | Field | Type |
|---|---|---|
| `User.createdAt` | `LocalDateTime` | Consistent |
| `Order.createdAt` | `LocalDateTime` | Consistent |
| `Cart.createdAt` | `LocalDateTime` | Consistent |
| `RefreshToken.expiresAt` | `LocalDateTime` | Consistent |
| **`Restaurant.createdAt`** | **`LocalDate`** | **Inconsistent** ← the only entity using `LocalDate` |
| `JWTService` | Token dates | `java.util.Date` | Legacy JJWT API (forced) |

**Evidence:** [Restaurant.java:32](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/domain/Restaurant.java#L32)

### 3.7 Unused Enum Value: `CartStatus.BOOKED`

> **Severity: Low**

[CartStatus.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/enums/CartStatus.java) defines `PENDING`, `BOOKED`, `EXPIRED`. The value `BOOKED` is **never referenced anywhere** in the codebase. Only `PENDING` and `EXPIRED` are used.

### 3.8 Inconsistent API Response Wrapping

> **Severity: Medium**

Some controllers wrap responses in `APIResponse<T>` (e.g., `RestaurantController`, `CartController`, `AuthController`), while others return raw entities or `Map.of(...)`:

| Controller | Response Pattern |
|---|---|
| `RestaurantController` | `APIResponse<T>` ✅ |
| `CartController` | `APIResponse<T>` ✅ |
| `AuthController` | `APIResponse<T>` ✅ |
| `AdminOrderController` | Raw `List<OrderView>` or `Map.of(...)` ❌ |
| `AdminRestaurantController` | Raw `Restaurant` entity ❌ |
| `AdminAuthController` | Raw `UserCreationView` / `JWTToken` ❌ |
| `OrderController` | `Map.of(...)` ❌ |

### 3.9 Security: Cart Endpoints Are `permitAll()`

> **Severity: Medium**

[Security.java:31](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/config/Security.java#L31): `/api/cart/**` is `permitAll()`. This is intentional for guest carts, but the controller logic compensates with manual null-checks on `userDetails` + `guestId`. This dual auth path is fragile and every cart endpoint repeats the same 6-line auth extraction block.

---

## 4. Data, Integration, and Workflow Gaps

### 4.1 Seed Data
✅ **Good**: The database is seeded with 15 realistic restaurants across 11 cuisines, with 8-10 menu items each (categorized). Seed data lives in Flyway migrations [V3](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/resources/db/migration/V3__seed_restaurants.sql) and [V5](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/resources/db/migration/V5__seed_menu_items.sql). Read endpoints return meaningful data out of the box.

### 4.2 Flyway Migration Health

| Migration | Purpose | Issues |
|---|---|---|
| V1 | Initial schema (users, restaurant, address) | ✅ Clean |
| V2 | Refresh tokens | ✅ Clean |
| V3 | Seed restaurants | ✅ Uses `gen_random_uuid()` |
| V4 | Menu items table | ✅ Clean |
| V5 | Seed menu items | ✅ Clean |
| V6 | Cart table | ✅ Clean |
| V7 | Cart items table | ✅ Clean |
| V8 | Guest cart support | ✅ Clean |
| V9 | Remove unique cart constraints | ⚠️ **Sign of rework** — constraint added then removed |
| V10 | Admin auth fields | ✅ Clean |
| V11 | Restaurant image_url | ✅ Clean |
| V12 | Menu item image_url | ✅ Clean |
| V13 | Order tables | ✅ Clean |

> [!NOTE]
> V9 (`remove_unique_cart_constraints`) indicates the cart schema was iterated on during development. This is normal for early-stage projects but worth noting — the constraint was too restrictive and was removed rather than replaced.

No V14 migration file exists on disk (contradicting the session context that said one was created — it may not have been committed).

### 4.3 Missing Integrations Between Bounded Contexts

| Expected Integration | Status |
|---|---|
| **Ordering → Delivery** | ❌ **Not implemented.** Orders move to `ACCEPTED` and `DELIVERED` status but there is no delivery assignment, tracking, or driver management. Status transitions are purely manual admin clicks. |
| **Ordering → Notifications** | ❌ **Not implemented.** WebSocket push exists for real-time browser sessions, but there are no email notifications, no SMS, and no push notifications. If a customer closes their browser, they learn nothing about their order status. |
| **Ordering → Payment** | ⚠️ **Stub.** [PaymentService.java](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/src/main/java/com/burito/service/PaymentService.java) is a 16-line class that logs and returns `true`. No interface, no strategy pattern, no gateway integration. |
| **Identity → Catalog** | ✅ Works. `AuthService.registerAdmin()` creates a `Restaurant` for the admin user. |

### 4.4 Missing Workflows

These are workflows a food delivery platform requires that have **zero implementation**:

1. **Delivery lifecycle** — driver assignment, pickup confirmation, delivery confirmation, ETA tracking
2. **Order cancellation by customer** — the `CANCELLED` status exists in the enum but no endpoint allows a customer to cancel
3. **Order history** — no endpoint to list past orders, no UI
4. **User address management** — `Address` entity exists but only for restaurants, not for delivery addresses
5. **Ratings and reviews** — `rating` field exists on `Restaurant` but is static seed data with no write path
6. **Search by location / distance** — no geolocation support
7. **Admin analytics** — no order summaries, revenue tracking, or dashboard metrics
8. **Email verification** — registration has no email confirmation step
9. **Password reset** — no forgot-password flow

---

## 5. Test and CI Health Summary

### 5.1 Test Pyramid

| Layer | Count | Notes |
|---|---|---|
| **Backend Unit Tests** (service-layer with mocks) | ~63 `@Test` methods across 10 service test files | Decent coverage of auth (14 tests), cart (12), JWT (6), restaurant (7). **OrderService has only 2 tests.** |
| **Backend Controller Tests** (`@WebMvcTest` + sliced context) | ~45 `@Test` methods across 6 controller test files | `AdminOrderController` has **zero tests** — the most complex controller with status transitions, authorization, and WebSocket broadcasting. |
| **Backend Integration Tests** (`@SpringBootTest` + Testcontainers) | 1 smoke test + CartController integration | Testcontainers configured for PostgreSQL. Minimal integration coverage. |
| **Frontend Component Tests** (Vitest + Testing Library) | ~43 `it()`/`test()` blocks across 11 test files | Good spread across catalog, cart, admin forms. No tests for `ActiveOrderPage`, `AdminOrderDashboard`, `WebSocketContext`, or `OrderNotificationListener`. |
| **End-to-End Tests** | **Zero** | No Playwright, Cypress, or any browser automation. |
| **WebSocket Tests** | **Zero** | No tests for any STOMP subscription, broadcast, or WebSocket authentication. |

### 5.2 Critical Untested Paths

| Critical Path | Tested? |
|---|---|
| Auth login + token generation | ✅ Yes (14 tests) |
| Cart add/remove/merge | ✅ Yes (12 + 10 tests) |
| **Checkout flow** (OrderService) | ⚠️ 2 tests only |
| **Admin order status transition** | ❌ Zero tests |
| **WebSocket message delivery** | ❌ Zero tests |
| **Real-time customer notification** | ❌ Zero tests |
| **Admin authorization** (role-based access) | ⚠️ Partial (AdminRestaurantController has 7 tests, AdminOrderController has 0) |

### 5.3 CI Pipeline

[ci.yml](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/.github/workflows/ci.yml) runs on PRs to `main`:

| Job | What It Does | Gaps |
|---|---|---|
| `backend` | `./gradlew bootJar` + `./gradlew test` | ✅ Builds and runs tests |
| `frontend` | `test -f frontend/index.html` | ❌ **This is a placeholder** — it only checks that `index.html` exists. No build, no lint, no test execution. |
| `lint` | `./gradlew checkstyleMain` (Google checks) | ✅ Runs Checkstyle |

> [!WARNING]
> **Frontend CI is effectively a no-op.** The 43 frontend tests exist but are never run in CI. The `frontend` job literally does `test -f frontend/index.html` — a file existence check, not validation.

**JaCoCo** is configured in [build.gradle](file:///Users/umarkhaji/workspace/mcu/asgard-projects/burito/backend/build.gradle) to generate reports but **enforces no minimum thresholds** (`jacocoTestCoverageVerification` is absent). Coverage is generated but never gated.

---

## 6. Microservices Readiness Scorecard

| Signal | Rating | Evidence |
|---|---|---|
| **Bounded context separation in code** | 🔴 **Not present** | All domain objects, services, and controllers share flat packages. `OrderService` directly imports `CartRepo`, `CartItemRepo`, `UserRepo`. `AuthService` directly imports `RestaurantRepo`. There are no interfaces or API boundaries between what would become separate services. |
| **Cross-context decoupling** | 🔴 **Fully coupled** | Splitting "Ordering" into a separate service would require it to directly query the `Cart`, `CartItem`, `User`, and `Restaurant` tables — which would all live in other services' databases. There are no domain events, no API contracts, and no anti-corruption layers. |
| **Shared mutable state** | 🔴 **Present** | `OrderService.checkout()` reads and writes `Cart` + `CartItem` tables in the same transaction as creating `Order` records. This tight transactional coupling would need to be replaced with sagas or eventual consistency. |
| **Chatty synchronous patterns** | 🟡 **Moderate risk** | `AdminOrderController.getActiveOrders()` calls `userRepo.findUserByEmail()` → `restaurantRepo.findByOwnerId()` → `orderRepo.findByRestaurant...()` in sequence. These would become 3 network hops in a distributed system. |
| **Operational baseline (observability)** | 🔴 **Minimal** | Actuator is included but only exposes `/health` with `show-details: never`. No Micrometer metrics, no distributed tracing (Zipkin/Jaeger), no structured logging, no log correlation IDs. |
| **Deployment maturity** | 🟡 **Single-service shaped** | Docker Compose deploys one backend, one frontend, one nginx, one PostgreSQL. No service discovery, no config server, no circuit breakers. This is appropriate for a monolith but provides no runway for multi-service deployment. |
| **Feature completeness baseline** | 🔴 **Too early** | 2 of 5 bounded contexts (Delivery, Notifications) have zero implementation. Payment is a stub. Core customer workflows (order history, cancellation, address management) are missing. Splitting now would distribute incomplete functionality across services. |
| **Test confidence for refactoring** | 🟡 **Partial** | 117 backend tests provide reasonable coverage for auth and cart, but the checkout-to-delivery flow — the exact seam where services would split — has only 2 unit tests and zero integration tests. |

---

## 7. Recommendation: NO-GO on Starting Microservices Migration Now

### Reasoning

The migration should **not** start because the foundational prerequisites are unmet:

1. **There are no bounded contexts to migrate.** The codebase is a classical layered monolith (`controller → service → repository`). A microservices migration requires clear domain boundaries to exist *first* — you extract along seams, you don't create seams and extract simultaneously. Today, splitting any "context" would require untangling shared repository access, shared domain objects, and cross-cutting transaction boundaries that have no defined interfaces.

2. **Two of five intended contexts don't exist yet.** Delivery and Notifications have zero code. Starting a migration now means splitting an incomplete system, which compounds the delivery risk of both finishing the features *and* distributing them correctly.

3. **The critical seams are the least tested.** The checkout flow (where Ordering meets Cart meets Payment) has 2 unit tests. The admin order management (where Ordering meets real-time Notifications) has 0 tests. These are exactly the seams where a split would occur, and there is no safety net for refactoring them.

4. **Operational readiness is absent.** No distributed tracing, no metrics beyond a health endpoint, no structured logging. Running multiple services without these is operating blind.

5. **Payment is a stub.** Introducing a real payment integration will significantly change the Ordering context's behavior and transaction boundaries. This should happen before, not during, a split.

### What Needs to Be True Before Flipping to GO

| # | Prerequisite | Current State |
|---|---|---|
| 1 | **Reorganize packages into bounded-context modules** (e.g., `com.burito.identity`, `com.burito.catalog`, `com.burito.ordering`) with explicit internal APIs (interfaces/facades) between them. No cross-context direct repository access. | Layer-based packages, 11+ cross-context repo imports |
| 2 | **Implement Delivery and Notifications contexts** at least to MVP (delivery status tracking, email/push notifications for order status changes). | Zero code |
| 3 | **Consolidate exception handling** into a single consistent strategy. Eliminate `ResponseStatusException` and raw `IllegalStateException` throws in favor of `APIException` hierarchy. | 3 competing strategies |
| 4 | **Complete test coverage for checkout and order management flows**, including integration tests that validate the transactional boundaries that would become eventual-consistency boundaries in a distributed system. | 2 unit tests, 0 integration tests |
| 5 | **Integrate a real payment gateway** (or at minimum, define a proper interface/strategy pattern for payment) so the Ordering context's transaction semantics are realistic. | `return true;` stub |
| 6 | **Add observability**: structured logging with correlation IDs, Micrometer metrics, and distributed tracing (at least OpenTelemetry instrumentation). | Health endpoint only |
| 7 | **Fix CI to actually validate the frontend** (run build + tests) and enforce JaCoCo coverage thresholds. | Frontend CI is `test -f frontend/index.html` |
| 8 | **Standardize API response wrapping** across all controllers. | Mixed `APIResponse<T>` vs raw entities vs `Map.of()` |

> [!IMPORTANT]
> Items 1-3 are prerequisite to even *planning* a migration. Items 4-8 are prerequisite to *executing* one safely. The recommended sequence is: fix architectural consistency (1, 3, 8) → complete features (2, 5) → harden testing and ops (4, 6, 7) → then reassess.

**Estimated effort to reach GO state:** 3-4 focused iterations (assuming 2-week iterations), depending on team size and scope of Delivery/Notifications MVPs.
