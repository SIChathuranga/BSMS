# BSMS – Motorbike Spare Parts Management System

This repo contains a full‑stack web application for managing and selling motorbike spare parts.

- Frontend: HTML/CSS/JS (Bootstrap) located in `spareparts-frontend/`
- Backend: Spring Boot (Java) located in `spareparts-backend/`
- Auth: Firebase Authentication (Google Sign‑In). Backend verifies ID token via Firebase Admin SDK.
- DB: PostgreSQL via Spring Data JPA + Flyway migrations. Product images stored in Firebase Storage; URLs kept in DB.

## Quick start (local)

1) Prerequisites
- Java 17+, Maven
- PostgreSQL 13+
- Node or static server (optional) to serve frontend
- A Firebase project with Google Sign‑In and Storage enabled

2) Firebase setup
- Create a Firebase project, enable Google Sign‑In and Storage.
- Create a Service Account key (JSON). EITHER set env `FIREBASE_SA_KEY` to the JSON content or `FIREBASE_SA_KEY_PATH` to the file path.
- Copy your client web config JSON from Firebase Console and store it locally in browser: open DevTools and run `localStorage.setItem('FIREBASE_CONFIG', JSON.stringify({ ...your config... }))`.

3) Database
- Create DB: `spareparts`. Set env variables or edit `application.properties`:
	- `DB_URL=jdbc:postgresql://localhost:5432/spareparts`
	- `DB_USER=spareadmin`
	- `DB_PASS=sparepass`
- Flyway will create schema and seed data on app start.

4) Run backend
- From `spareparts-backend/` run `mvn spring-boot:run`.
- API base: `http://localhost:8080`.

5) Serve frontend
- Open `spareparts-frontend/index.html` with a local server (for example VSCode Live Server) to avoid CORS/file issues.
- Browse products (public).
- Click "Login with Google" to sign in. Authenticated requests will include `Authorization: Bearer <idToken>`.

## API overview (MVP)
- `GET /api/public/products`, `GET /api/public/products/{id}` – public product catalog
- `GET /api/user/orders`, `POST /api/user/orders` – customer orders (auth required)
- `POST /api/user/messages` – create message (auth required)
- `GET /api/admin/products`, `POST /api/admin/products`, `PUT /api/admin/products/{id}`, `DELETE /api/admin/products/{id}` – admin CRUD
- `GET /api/admin/orders`, `PATCH /api/admin/orders/{id}/status?status=CONFIRMED` – admin order management
- `GET /api/admin/analytics/summary` – totals

Admin endpoints require `ROLE_ADMIN` which maps from `users.is_admin`.

## Seeding
Flyway `V1__init.sql` seeds: 5 categories, 10 products, 2 users (admin/user), 3 orders, sample images, and a message.

## Postman collection
A Postman collection is included in `postman/BSMS.postman_collection.json` with environment placeholders. For auth endpoints, sign in on the frontend and copy your ID token into the `authToken` Postman variable.

## Deployment notes
- Frontend: Deploy to Firebase Hosting or any static host.
- Backend: Deploy to Render/Heroku/Cloud Run. Provide envs: `DB_URL`, `DB_USER`, `DB_PASS`, `FIREBASE_SA_KEY` or `FIREBASE_SA_KEY_PATH`, and `CORS_ALLOWED_ORIGINS`.

## Assumptions
- Payments: MVP uses COD; integrate a gateway later as needed.
- Using PostgreSQL. MySQL can be supported by swapping driver and Flyway database plugin.

## Testing
- Basic tests can be added under `spareparts-backend/src/test/...` for order placement and token filter using mocks. In local dev, use Firebase emulators or real project.

