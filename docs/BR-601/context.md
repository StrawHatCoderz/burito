## Context Summary: BR-601 — Order placement and stub payment

### Story Card
- **File:** `docs/iterations/iteration-6/BR-601-order-placement.md`
- **Status:** Not started

### What the story asks for
This story implements the core checkout flow. Customers will be able to convert their populated carts into actual orders. When an order is placed, a snapshot of the prices and items is saved in the database, the cart is cleared, and a mock payment service automatically approves the transaction. The order starts in a `PENDING` state.

### Acceptance Criteria (from requirements file)
- Customer can click "Checkout" from their cart to place an order
- Order is created with `PENDING` status, linked to the user and the restaurant
- Cart is completely cleared upon successful order placement
- Payment is handled by a stub service that always succeeds
- Order records a snapshot of item names, quantities, and prices at checkout time

### Linked stories
| ID | Title | Relationship |
|----|-------|-------------|
| BR-602 | WebSocket infrastructure setup | Dependent (already completed) |
| BR-603 | Restaurant Admin Order Dashboard | Blocks |

### Codebase findings
| Area              | Detail |
|-------------------|--------|
| Repo structure    | `backend/` (Spring Boot), `frontend/` (React/Vite) |
| Relevant files    | `backend/.../domain/Order.java` (New), `backend/.../service/OrderService.java` (New), `frontend/.../cart/CartDrawer.tsx` (Modify) |
| Test framework    | JUnit (backend), Vitest (frontend) |
| Tech stack        | Java 25, Spring Boot 4.0.6, React 19, Tailwind CSS |

### Initial observations
- **New Entities:** We need to create `Order` and `OrderItem` JPA entities. `Order` should map to `User` (customer) and `Restaurant`. `OrderItem` should contain historical price snapshots rather than just a foreign key to `MenuItem`, to protect against future price changes.
- **Cart Clearing:** The backend `OrderService` will need to orchestrate: fetching the cart, calculating total, calling `PaymentService`, creating the `Order`, and finally deleting items from `CartItem` (using `CartService` or `CartItemRepo`).
- **Transaction:** The entire checkout process must be wrapped in a Spring `@Transactional` to ensure data integrity (no order without cart cleared).
