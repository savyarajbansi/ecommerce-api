# API Reference — ecommerce-api

Base URL (default): `http://localhost:8080`

All endpoints are JSON unless noted.

## Authentication

This API uses stateless JWT authentication.

Send the token on protected endpoints:

```
Authorization: Bearer <jwt>
```

Public routes:

- `/api/auth/**`
- `GET /api/products/**`

All other routes require authentication.

Admin-only operations:

- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

Note: there is no API endpoint to promote a user to `ADMIN`; role changes must be done directly in the database.

## Common error formats

### Domain errors (400/404)

```json
{ "error": "message" }
```

### Validation errors (400)

```json
{
  "errors": {
    "field": "validation message"
  }
}
```

### Auth errors

- `401 Unauthorized` is returned by Spring Security’s authentication entry point with a plain `Unauthorized` message.
- `403 Forbidden` is returned when authenticated but lacking required role/authority.

---

## Auth

### POST /api/auth/register

Creates a new user account.

Auth: Public

Request body (`RegisterRequest`):

```json
{
  "email": "user@example.com",
  "password": "secret123",
  "name": "User"
}
```

Validation:

- `email`: required, valid email
- `password`: required, min length 6
- `name`: required

Response 200 (`AuthResponse`):

```json
{
  "token": "<jwt>",
  "email": "user@example.com",
  "role": "USER"
}
```

Errors:

- `400 {"error":"Email already registered"}`
- `400 {"errors":{...}}` (validation)

### POST /api/auth/login

Authenticates a user and returns a JWT.

Auth: Public

Request body (`LoginRequest`):

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response 200 (`AuthResponse`):

```json
{
  "token": "<jwt>",
  "email": "user@example.com",
  "role": "USER"
}
```

Errors:

- `400 {"error":"Invalid email or password"}`
- `400 {"errors":{...}}` (validation)

---

## Products

### GET /api/products

Lists all products.

Auth: Public

Response 200 (`ProductResponse[]`):

```json
[
  {
    "id": 1,
    "name": "T-Shirt",
    "description": "Cotton",
    "price": 19.99,
    "stockQuantity": 50,
    "imageUrl": "https://example.com/img.png"
  }
]
```

### GET /api/products/{id}

Gets one product by ID.

Auth: Public

Path params:

- `id` (number)

Response 200 (`ProductResponse`): same shape as above.

Errors:

- `404 {"error":"Product not found: <id>"}`

### GET /api/products/search?query=...

Searches products by name (case-insensitive substring).

Auth: Public

Query params:

- `query` (string, required)

Response 200 (`ProductResponse[]`)

### POST /api/products

Creates a product.

Auth: Admin

Request body (`ProductRequest`):

```json
{
  "name": "T-Shirt",
  "description": "Cotton",
  "price": 19.99,
  "stockQuantity": 50,
  "imageUrl": "https://example.com/img.png"
}
```

Validation:

- `name`: required
- `price`: required, >= 0.01
- `stockQuantity`: required, >= 0

Response 200 (`ProductResponse`)

### PUT /api/products/{id}

Updates a product.

Auth: Admin

Path params:

- `id` (number)

Request body: `ProductRequest` (same as create)

Response 200 (`ProductResponse`)

Errors:

- `404 {"error":"Product not found: <id>"}`

### DELETE /api/products/{id}

Deletes a product.

Auth: Admin

Path params:

- `id` (number)

Response 204 (no body)

Errors:

- `404 {"error":"Product not found: <id>"}`

---

## Cart

All cart endpoints require authentication.

### GET /api/cart

Gets the current user’s cart (created on first access).

Auth: Required

Response 200 (`CartResponse`):

```json
{
  "items": [
    {
      "productId": 1,
      "productName": "T-Shirt",
      "price": 19.99,
      "quantity": 2,
      "subtotal": 39.98
    }
  ],
  "totalPrice": 39.98
}
```

### POST /api/cart/items

Adds an item to the cart. If the product already exists in the cart, the quantity is incremented.

Auth: Required

Request body (`CartItemRequest`):

```json
{
  "productId": 1,
  "quantity": 2
}
```

Validation:

- `productId`: required
- `quantity`: required, >= 1

Response 200 (`CartResponse`)

Errors:

- `404 {"error":"Product not found: <id>"}`
- `400 {"error":"Insufficient stock for product: <name>"}`

Stock note: stock is not reserved when items are added to the cart; stock is decremented during checkout.

### PUT /api/cart/items/{productId}?quantity=...

Updates the quantity for a product in the cart.

If `quantity <= 0`, the item is removed.

Auth: Required

Path params:

- `productId` (number)

Query params:

- `quantity` (number, required)

Response 200 (`CartResponse`)

Errors:

- `404 {"error":"Product not in cart"}`

### DELETE /api/cart/items/{productId}

Removes a product from the cart.

Auth: Required

Response 204 (no body)

### DELETE /api/cart

Clears the cart.

Auth: Required

Response 204 (no body)

---

## Orders

All order endpoints require authentication.

### POST /api/orders/checkout

Charges the cart total via Stripe, creates an order, decrements stock, and clears the cart.

Auth: Required

Request body (`CheckoutRequest`):

```json
{
  "stripeToken": "tok_visa"
}
```

Notes:

- Currency is hard-coded to `usd`.
- The Stripe amount is calculated as `totalAmount * 100` and sent as an integer number of cents.

Response 200 (`OrderResponse`):

```json
{
  "id": 123,
  "totalAmount": 39.98,
  "status": "PAID",
  "stripePaymentId": "ch_...",
  "createdAt": "2026-04-07T12:34:56.123",
  "items": [
    {
      "productId": 1,
      "productName": "T-Shirt",
      "quantity": 2,
      "priceAtPurchase": 19.99
    }
  ]
}
```

Errors:

- `400 {"error":"Cart is empty"}`
- `400 {"error":"Payment failed: ..."}`
- `400 {"errors":{...}}` (validation)

### GET /api/orders

Lists orders for the current user, newest first.

Auth: Required

Response 200 (`OrderResponse[]`)

### GET /api/orders/{id}

Gets an order by ID, only if it belongs to the current user.

Auth: Required

Path params:

- `id` (number)

Response 200 (`OrderResponse`)

Errors:

- `400 {"error":"Order not found"}` (not found or not owned by the current user)
