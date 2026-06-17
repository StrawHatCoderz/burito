# Tasks: BR-601 Order placement and stub payment

## Task 1: Backend Domain & Database Setup
**Objective:** Create the database schema and JPA entities for Orders.
- [ ] Create `OrderStatus` enum (`PENDING`, `ACCEPTED`, `DELIVERED`, `CANCELLED`).
- [ ] Create `Order` entity mapping to `User` and `Restaurant`.
- [ ] Create `OrderItem` entity containing historical prices.
- [ ] Create Flyway migration `V3__create_order_tables.sql`.
- [ ] Ensure the project compiles and database migrations run successfully.

## Task 2: Backend Repositories & Services
**Objective:** Implement the business logic for checking out a cart.
- [ ] Create `OrderRepo` and `OrderItemRepo`.
- [ ] Create `PaymentService` stub.
- [ ] Implement `OrderService.checkout()` with `@Transactional`, validating cart and clearing items.
- [ ] Expose `POST /api/orders/checkout` in `OrderController`.
- [ ] Write integration test or verify the flow works up to the controller layer.

## Task 3: Frontend Integration
**Objective:** Wire up the UI to call the checkout endpoint.
- [ ] Create `frontend/src/features/orders/api/orders.api.ts`.
- [ ] Add Checkout button to `CartDrawer.tsx`.
- [ ] Call API on checkout, display toast, and clear cart state locally.
- [ ] Verify the complete end-to-end flow from the browser.
