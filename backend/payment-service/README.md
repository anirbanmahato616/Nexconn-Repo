# RecoverX Payment Service — JWT Secured

## Requirements
- Java 17
- Maven
- MySQL
- Eureka Discovery Server
- Auth Service and Business Service
- Invoice Service for payment creation

## Environment variables
Configure these in the STS Run Configuration:

```text
DB_URL=jdbc:mysql://localhost:3306/recoverx_payment_db
DB_USERNAME=root
DB_PASSWORD=<your-local-mysql-password>
JWT_SECRET=<same-Base64-secret-used-by-auth-and-business-services>
BUSINESS_SERVICE_URL=http://localhost:8088
INVOICE_SERVICE_URL=http://localhost:8082
EUREKA_SERVER_URL=http://localhost:8761/eureka/
```

The development JWT fallback matches the demo secret used in the earlier RecoverX setup. Use a strong, private secret in any shared or production environment.

## Security behaviour
- Every `/api/businesses/{businessId}/payments/**` request requires a valid Bearer JWT.
- The service extracts `userId` from the signed JWT; client-supplied actor IDs are no longer trusted.
- Each request checks the caller's active membership using Business Service:
  `GET /api/businesses/{businessId}/members/{userId}`
- Only OWNER and ADMIN memberships can verify or reject a payment.
- Invoice Service calls forward the caller's Bearer token because Invoice Service is also JWT protected.
- Actuator health and info remain public.

## Important current scope
The current membership check allows active business members to use payment endpoints. A customer who is linked to a business as a customer record but is not a business member cannot yet submit a payment using this endpoint. Customer-to-business payment authorization needs a separate customer relationship check and should be implemented before customer-facing payment submission is released.

## Payment API
Base path: `/api/businesses/{businessId}/payments`

- `POST /` — create a pending payment (active business member)
- `GET /` — list payments (active business member)
- `GET /{id}` — get payment (active business member)
- `GET /invoice/{invoiceId}` — list invoice payments (active business member)
- `PATCH /{id}/verify` — verify pending payment (OWNER/ADMIN only)
- `PATCH /{id}/reject` — reject pending payment (OWNER/ADMIN only)

The `recordedBy` and `reviewedBy` values are now derived from the authenticated JWT, not request JSON.


## Invoice update after payment verification

When an OWNER or ADMIN verifies a pending payment, Payment Service first calls Invoice Service directly using:
`PATCH /internal/businesses/{businessId}/invoices/{invoiceId}/verified-payments`

Payment Service sends the payment ID and amount with the `X-Internal-Service-Key` header. Invoice Service records the payment idempotently and updates `paidAmount`, `outstandingAmount`, and invoice status. Only after Invoice Service confirms the update does Payment Service mark the payment `VERIFIED`.

Configure the same `PAYMENT_SERVICE_INTERNAL_SECRET` value in both services. The local development fallback is `RecoverX-Local-Payment-Internal-Secret-Change-Before-Deployment`. Replace it with a long random secret before deployment.
