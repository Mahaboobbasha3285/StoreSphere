# ShopSphere

Intermediate e-commerce application using Java 21, Spring Boot, React, Vite and MySQL.

## Requirements
- Java 21+
- Maven 3.9+
- Node.js 20+
- Docker (for the supplied MySQL service)

## Configure and run
1. Start MySQL: `docker compose up -d`
2. Set the admin credentials before starting the backend (a password is required; use a strong unique value):
   - Linux/macOS: `export ADMIN_USERNAME=admin` and `export ADMIN_PASSWORD='your-strong-password'`
   - PowerShell: `$env:ADMIN_USERNAME='admin'` and `$env:ADMIN_PASSWORD='your-strong-password'`
3. Backend: `cd backend && mvn test && mvn spring-boot:run`
4. Frontend: `cd frontend && npm install && npm run dev`
   - After `npm install` creates `package-lock.json`, commit that file and use `npm ci` for clean/reproducible installs.
5. Open http://localhost:5173. Backend API: http://localhost:8080/api

## Features
Product catalogue, search, category filter, cart persisted in localStorage, checkout, pessimistic row locking for checkout and admin product updates, private order lookup using a random access token, admin-protected product writes, request validation, central API errors, MySQL Docker service and H2 integration tests.

## API
- `GET /api/health`
- `GET /api/products?q=&category=` (public)
- `GET /api/products/{id}` (public)
- `POST /api/orders` (public checkout; returns order ID, total and one-time access token)
- `POST /api/orders/lookup` (public, requires order ID, checkout email and private access token)
- `POST /api/products`, `PUT /api/products/{id}`, `DELETE /api/products/{id}` (HTTP Basic admin authentication)

Product JSON: `{"name":"Desk Lamp","description":"Warm light","price":999.00,"category":"Home","imageUrl":"","stock":10}`
Order JSON: `{"customerName":"Asha","email":"asha@example.com","address":"Bengaluru","items":[{"productId":1,"quantity":2}]}`
Order lookup JSON: `{"orderId":1,"email":"asha@example.com","accessToken":"token-returned-at-checkout"}`

After checkout, save the private access token. It is stored as a SHA-256 hash in the database and cannot be recovered if lost. Product write requests use HTTP Basic with `ADMIN_USERNAME` and `ADMIN_PASSWORD`, plus a CSRF token. First call `GET /api/csrf` and retain the `XSRF-TOKEN` cookie. Send that cookie and its value in the `X-XSRF-TOKEN` header, together with HTTP Basic credentials, for authenticated write requests.

## Security and production notes
The admin account is in-memory and intended for a single-instance demo. For production, replace it with persistent identity management, HTTPS, managed secrets, database migrations, rate limiting, CSRF-aware browser authentication, payment integration, audit logging and operational monitoring. The order access token is a bearer secret: do not share it.

## Verification status
The source includes integration tests, but they must be run in an environment with Maven and the required dependency registry access. The submitted archive does not currently contain `frontend/package-lock.json`; run `npm install` to generate one before using `npm ci` in a reproducible CI pipeline.
