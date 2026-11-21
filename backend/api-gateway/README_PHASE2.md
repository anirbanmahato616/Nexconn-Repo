# RecoverX API Gateway

## Runtime
- Java 17
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- Spring Cloud Gateway Server Web MVC
- Eureka service discovery
- Spring Security + JJWT 0.12.6

## Configuration
The Gateway listens on port 8080 and discovers downstream services through Eureka.

Set the same JWT secret in Auth Service, Business Service, and API Gateway:
- Environment variable: `JWT_SECRET`
- Local fallback is present in `application.properties` for development only.

The local fallback must not be used in production. Use a strong random Base64-encoded secret of at least 32 bytes and keep it identical across all services that validate these HS256 tokens.

Optional:
- `EUREKA_SERVER_URL` (default `http://localhost:8761/eureka/`)
- `CORS_ALLOWED_ORIGINS` (default `http://localhost:5173,http://localhost:3000`)

## Routes
The Gateway uses explicit routes with `lb://service-name` destinations:
- `/api/auth/**` -> auth-service
- `/api/businesses/*/customers/**` -> customer-service
- `/api/businesses/*/invoices/**` -> invoice-service
- `/api/businesses/*/payments/**` -> payment-service
- `/api/businesses/*/commitments/**` -> commitment-service
- `/api/businesses/*/conversations/**` -> conversation-service
- `/api/ai/**` -> ai-service
- `/api/businesses/**` -> business-service

Specific business sub-resource routes are intentionally declared before the general Business Service route.

If your AI controller uses a path other than `/api/ai/**`, update that route to match the actual controller mapping.

## Authentication
- `POST /api/auth/register` and `POST /api/auth/login` are public.
- `/actuator/health` and `/actuator/info` are public.
- All other routes require `Authorization: Bearer <JWT>`.
- Gateway validates the JWT signature, expiry, email claim, and numeric `userId` claim.
- The original Authorization header is forwarded to downstream services. Downstream services must continue validating JWTs themselves; Gateway authentication does not replace service-level authorization.

## Postman smoke tests
1. `POST http://localhost:8080/api/auth/login` with registered credentials -> expect 200.
2. `GET http://localhost:8080/api/businesses/owner/me` without a token -> expect 401.
3. Repeat the GET with the login response's new Bearer token -> expect a response from Business Service (normally 200).
4. Call a non-existent downstream route or stop one service to verify gateway routing errors are handled as expected.

## Important
The Gateway can only route to services that have registered with Eureka and whose service IDs match the names above. Start Eureka and all required downstream services before testing routes.
