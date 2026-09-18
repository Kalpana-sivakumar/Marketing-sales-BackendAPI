# Marketing & Sales App — Backend

Spring Boot (Java 21) backend skeleton for:
- **Admin / Marketing Manager dashboard** (web)
- **Staff mobile app**

This build contains **only the login/authentication architecture** — a maintainable,
layered foundation to build feature modules (campaigns, leads, targets, reports, etc.) on top of.

## Stack

| Concern            | Choice                                   |
|---------------------|-------------------------------------------|
| Language / Runtime  | Java 21                                   |
| Framework           | Spring Boot 3.3.4                         |
| Security            | Spring Security 6 + JWT (jjwt 0.12.5)     |
| Persistence         | Spring Data JPA + PostgreSQL              |
| Schema migrations   | Flyway                                    |
| Validation          | Jakarta Bean Validation                   |
| API docs            | springdoc-openapi (Swagger UI)            |
| Build tool          | Maven                                     |

## Architecture / package layout

```
com.marketingsales.backend
├── config/          # SecurityConfig, OpenApiConfig, CORS, beans
├── constant/         # Role enum, shared constants
├── controller/        # REST endpoints (thin — delegate to services)
├── dto/
│   ├── request/       # Inbound payloads + validation annotations
│   └── response/       # ApiResponse envelope, AuthResponse, etc.
├── entity/            # JPA entities
├── exception/         # Custom exceptions + GlobalExceptionHandler
├── repository/        # Spring Data JPA repositories
├── security/           # JwtUtil, JwtAuthenticationFilter, UserDetailsService, UserPrincipal
├── service/            # Interfaces
│   └── impl/           # Implementations
└── util/               # (reserved for shared helpers)
```

Design choices, and why:
- **Interface + impl service layer** — keeps controllers/tests decoupled from implementation, standard for scaling to more services.
- **`ApiResponse<T>` envelope** — one consistent response shape for both the dashboard and mobile clients.
- **Stateless JWT auth** — no server-side session; both dashboard and mobile clients authenticate the same way (`Authorization: Bearer <token>`).
- **Access + refresh tokens** — short-lived access token (15 min default) + longer refresh token (7 days default), so mobile clients aren't forced to re-login constantly.
- **`Role` enum (`ADMIN`, `MARKETING_MANAGER`, `STAFF`)** — drives `@PreAuthorize` checks (`@EnableMethodSecurity`). See `DemoRoleController` for the pattern (delete that class once real endpoints exist).
- **Flyway** — schema is version-controlled via SQL migrations (`src/main/resources/db/migration`); Hibernate is set to `validate` only, never auto-generates schema.
- **Profiles** — `application.yml` (base) + `application-dev.yml` / `application-prod.yml`, activated via `SPRING_PROFILES_ACTIVE`.
- **Global exception handling** — all errors return the same `ApiResponse` shape with correct HTTP status codes.

## Getting started

### 1. Start Postgres (Docker)
```bash
docker compose up -d
```

### 2. Configure environment
```bash
cp .env.example .env
# edit values as needed, especially JWT_SECRET for anything beyond local dev
```

### 3. Run
```bash
mvn spring-boot:run
```
Flyway runs automatically on startup and creates the `users` table (plus a dev seed admin —
see `V2__seed_dev_admin_user.sql`: `admin@marketingsales.dev` / `Admin@12345`, **dev only**).

### 4. Explore the API
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `GET /api/health`

## Auth endpoints

| Method | Endpoint                    | Description                    |
|--------|-------------------------------|---------------------------------|
| POST   | `/api/auth/login`              | Authenticate, get access+refresh tokens |
| POST   | `/api/auth/refresh-token`      | Exchange a refresh token for a new access token |

Everything else requires `Authorization: Bearer <accessToken>`.

## User management endpoints (admin only)

All user-management routes require an `ADMIN` access token. Passwords supplied to user-create
or user-update requests are BCrypt-hashed before persistence and are never returned by the API.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users?search=&role=&region=&enabled=&page=&size=` | List users; search by name/email and filter by role, region, or active status |
| GET | `/api/users/{id}` | Get a user |
| PUT | `/api/users/{id}` | Edit profile details, password, region, or role |
| PATCH | `/api/users/{id}/status` | Activate/deactivate with `{"enabled": true}` or `{"enabled": false}` |

`POST /api/auth/register` remains available only to authenticated admins for backwards
compatibility and also requires `region`.

## Product management endpoints (admin only)

SKU is unique and immutable after creation (edit requests do not accept SKU).

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/products` | Create product |
| GET | `/api/products?search=&category=&uom=&active=&page=&size=` | List products, search by SKU/name, filter by category/UOM/status |
| GET | `/api/products/{id}` | View product details |
| PUT | `/api/products/{id}` | Edit product details (except SKU) |
| PATCH | `/api/products/{id}/status` | Activate/deactivate with `{"active": true}` or `{"active": false}` |

Example create payload:
```json
{
  "sku": "GEN-CL-500",
  "productName": "Gen1 Chain Lube",
  "category": "Chain Care",
  "packSize": 500,
  "uom": "ML",
  "basePrice": 650,
  "mrp": 750,
  "status": "ACTIVE"
}
```

Product details response includes: category, pack size, UOM, base price, MRP, status, createdBy,
createdAt, updatedBy, and updatedAt.

Example login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@marketingsales.dev","password":"Admin@12345"}'
```

## Extending this skeleton

- **New feature module** (e.g. campaigns): add `entity/Campaign.java`, `repository/CampaignRepository.java`,
  `service/CampaignService.java` (+ `impl`), `dto/request` & `dto/response` classes, `controller/CampaignController.java`,
  and a new Flyway migration `V3__create_campaigns_table.sql`. Same pattern every time.
- **Role/permission model grows more complex** — promote the `Role` enum to a `Role`/`Permission` entity + join tables;
  `UserPrincipal.getAuthorities()` is the only place that needs to change.
- **Rate limiting, audit logging, multi-tenancy** — add as new filters/aspects in `config`/`security`, without touching feature code.

## Testing

```bash
mvn test
```
`BackendApplicationTests` verifies the full context (security, JPA, JWT beans) wires up correctly against an in-memory H2 database.

## Security notes for production

- Replace `JWT_SECRET` with a strong, random 256-bit+ value from a secrets manager — never hardcode it.
- Restrict `CORS_ALLOWED_ORIGINS` to real dashboard/mobile app domains.
- Remove or environment-guard `V2__seed_dev_admin_user.sql` before deploying to staging/production.
- Put the app behind HTTPS/TLS termination; cookies aren't used here, but tokens should still only travel over TLS.
