# Plan: BR-601 Order placement and stub payment

## Objective
Enable authenticated customers to check out, turning their active cart items into a `PENDING` order. A stub payment service will simulate a successful transaction, and the user's cart will be cleared upon success.

## Scope
### In Scope
- Creating `Order` and `OrderItem` database entities and Flyway migrations.
- Creating an `OrderStatus` enum.
- Creating a `PaymentService` stub.
- Implementing the checkout transaction logic in `OrderService`.
- Exposing a `POST /api/orders/checkout` endpoint.
- Updating `CartDrawer.tsx` to include a Checkout button.
- Managing frontend state to clear the cart after a successful checkout.
### Out of Scope
- Actual payment gateway integration (Stripe, Razorpay, etc.).
- Admin dashboards for viewing the orders (that is BR-603).

## Approach
We will build the `Order` and `OrderItem` domain entities to hold a historical snapshot of the purchase. The `OrderService` will expose a `checkout(User)` method that wraps everything in a `@Transactional` block: verifying the cart is not empty, calculating the total, calling `PaymentService`, creating the order rows, and finally deleting the cart items. On the frontend, `CartDrawer.tsx` will add a Checkout button that posts to this new endpoint and triggers a cart refresh.

## Affected Areas
| Area | Files / Modules | Change Type |
|------|----------------|-------------|
| Backend | `domain/Order.java` | Add |
| Backend | `domain/OrderItem.java` | Add |
| Backend | `enums/OrderStatus.java` | Add |
| Backend | `db/migration/V3__create_order_tables.sql` | Add |
| Backend | `service/OrderService.java` | Add |
| Backend | `service/PaymentService.java` | Add |
| Backend | `controller/OrderController.java` | Add |
| Frontend | `orders/api/orders.api.ts` | Add |
| Frontend | `cart/CartDrawer.tsx` | Modify |

## Assumptions
1. `[ASSUMPTION]` The backend endpoint `POST /api/orders/checkout` does not need a payload because it implicitly uses the currently authenticated user's cart.
2. `[ASSUMPTION]` We will use a Flyway migration `V3__` assuming `V1` and `V2` exist.

## Open Questions (resolved)
| # | Question | Answer / Decision |
|---|----------|------------------|
| 1 | None at this stage | - |

## Risks & Mitigations
| Risk | Mitigation |
|------|-----------|
| Empty cart checkout | Backend throws `BadRequestException` if cart is empty. Frontend disables button. |
