# RecoverX Customer Service — JWT Secured

## Security changes
- Stateless Spring Security configuration.
- Validates JWT signature, expiry, subject/email and numeric `userId` using JJWT 0.12.6.
- Requires authentication for all customer API endpoints.
- Checks active business membership with Business Service before every customer operation.
- Forwards the caller's Bearer token to Business Service for membership verification.
- Returns 401 for missing/invalid/expired JWT and 403 when the user is not an active member.
- Health and info actuator endpoints remain public.

## Important architecture note
Customer Service does not trust `businessId` from the URL by itself. It calls Business Service:
`GET /api/businesses/{businessId}/members/{userId}`
using the same Bearer token. Business Service returns the caller's own active membership or rejects the request. If Business Service is unavailable, Customer Service returns 503 rather than silently allowing access.

## Configuration
Java 17, Spring Boot 4.1.2, Maven, MySQL, Eureka.

Set environment variables in the STS Run Configuration:
- `DB_URL=jdbc:mysql://localhost:3306/recoverx_customer_db`
- `DB_USERNAME=root`
- `DB_PASSWORD=<your local MySQL password>`
- `JWT_SECRET=<same Base64 secret used by Auth Service and Business Service>`
- `BUSINESS_SERVICE_URL=http://localhost:8088`
- `EUREKA_SERVER_URL=http://localhost:8761/eureka/`

The application properties contain a demo-only JWT fallback for local development. Do not use that fallback in production. Never commit real database credentials or production JWT secrets.

The Business Service must be running because Customer Service checks membership before serving business-scoped customer operations.

## Endpoints
Base path: `/api/businesses/{businessId}/customers`

- `POST /api/businesses/{businessId}/customers`
- `GET /api/businesses/{businessId}/customers`
- `GET /api/businesses/{businessId}/customers/{customerId}`
- `PUT /api/businesses/{businessId}/customers/{customerId}`

All require `Authorization: Bearer <JWT>`.

## Test sequence
1. Start Eureka.
2. Start Auth Service and Business Service.
3. Start Customer Service.
4. Login through API Gateway and copy the current `accessToken`.
5. Through API Gateway, GET `/api/businesses/{businessId}/customers` with no token: expect 401.
6. Repeat with an invalid token: expect 401.
7. Repeat with a valid token belonging to an active member: expect 200.
8. Use a valid token belonging to a user who is not an active member: expect 403.
9. Deactivate a member in Business Service and retry: expect 403.
10. Test POST/GET by ID/PUT using an active member token.

## Import into STS
Extract the ZIP, then use File > Import > Maven > Existing Maven Projects and select this project's `pom.xml`. Run Maven > Update Project, set Java 17, configure the environment variables above, and start `CustomerServiceApplication`.
