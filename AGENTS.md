# AGENTS Guide for `ecommerce-api`

## Quick orientation
- Stack: Spring Boot 3.4.4, Java 17, Spring Security (JWT), Spring Data JPA, PostgreSQL runtime, H2 test profile (`pom.xml`).
- Architecture is strict layered flow: Controller -> Service -> Repository -> Entity/DB.
- Existing AI instruction files were searched via glob (`**/{.github/copilot-instructions.md,AGENT.md,AGENTS.md,CLAUDE.md,.cursorrules,.windsurfrules,.clinerules,.cursor/rules/**,.windsurf/rules/**,.clinerules/**,README.md}`) and none were found.

## Big-picture data flow (important)
- Auth: `AuthController` -> `AuthService` -> `UserRepository` + `JwtTokenProvider` (`src/main/java/com/ecommerce/api/controller/AuthController.java`, `src/main/java/com/ecommerce/api/service/AuthService.java`).
- Product catalog: public GETs, admin-only writes via `@PreAuthorize("hasRole('ADMIN')")` (`src/main/java/com/ecommerce/api/controller/ProductController.java`).
- Cart and order endpoints resolve current user from `@AuthenticationPrincipal UserDetails`, then fetch domain `User` through `UserRepository` in controller (`src/main/java/com/ecommerce/api/controller/CartController.java`, `src/main/java/com/ecommerce/api/controller/OrderController.java`).
- Checkout pipeline in `OrderService.checkout`: validate non-empty cart -> compute total -> Stripe charge -> persist `Order` + `OrderItem` -> decrement product stock -> clear cart.

## Security boundary rules
- URL-level rules in `SecurityConfig`: `/api/auth/**` and `GET /api/products/**` are public; everything else requires auth (`src/main/java/com/ecommerce/api/config/SecurityConfig.java`).
- JWT parsing happens in `JwtAuthenticationFilter` from `Authorization: Bearer <token>` and loads user via `UserDetailsServiceImpl`.
- Role authority format is `ROLE_<enum>` from `User.Role` (`src/main/java/com/ecommerce/api/security/UserDetailsServiceImpl.java`).
- Stripe key is globally initialized once by `StripeConfig` using `stripe.api.key` property.

## Project-specific coding patterns
- Keep business logic in services; controllers should mostly delegate and build `ResponseEntity`.
- Request DTOs use Jakarta validation annotations; controllers consistently use `@Valid` on `@RequestBody`.
- Response DTOs are mapped with static `from(...)` factories (examples: `ProductResponse.from`, `CartResponse.from`, `OrderResponse.from`).
- Repositories rely on Spring Data derived query names (`findByEmail`, `findByUserOrderByCreatedAtDesc`, `findByNameContainingIgnoreCase`).
- Use `BadRequestException` / `ResourceNotFoundException` for domain failures and let `GlobalExceptionHandler` shape JSON responses (`{"error": ...}` or `{"errors": ...}`).
- Mutating cart/order operations are typically `@Transactional` (`CartService`, `OrderService`).

## Entity and persistence conventions
- `User.email` is unique and role defaults to `USER` (`src/main/java/com/ecommerce/api/model/User.java`).
- `Cart` is one-to-one with `User`; `Cart.items` and `Order.items` use cascade + orphan removal.
- Stock is stored on `Product.stockQuantity` and decremented during checkout, not reservation-at-cart-time.

## Developer workflows
- Run app locally: `mvn spring-boot:run` (no Maven wrapper is present in repo).
- Run full tests (uses Surefire argLine in `pom.xml`): `mvn test -Dspring.profiles.active=test`.
- Run one test class while iterating: `mvn test -Dtest=OrderServiceTest -Dspring.profiles.active=test`.
- Test profile config lives in `src/main/resources/application-test.properties` (H2 + test JWT secret + fake Stripe key).

## Integration points and gotchas
- External calls: Stripe charge creation in `PaymentService.createCharge`; failures are converted to `BadRequestException`.
- Runtime DB is PostgreSQL (`application.properties`), but tests assume H2 schema lifecycle (`create-drop`).
- In controller tests, cleanup order matters because of FK constraints; existing tests delete `order`/`cart_item`/`cart` before users/products (see `ProductControllerTest`, `CartControllerTest`).
- If adding secured endpoints, update both `SecurityConfig` matchers and role annotations/tests to avoid accidental 401/403 regressions.

