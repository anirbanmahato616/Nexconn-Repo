# RecoverX Commitment Service

Owns payment-promise commitments and their proposed installments. AI output is stored as a suggestion; only a business-side human review can approve or reject a commitment.

## Configuration
- `DB_URL` default `jdbc:mysql://localhost:3306/recoverx_commitment_db`
- `DB_USERNAME` default `root`
- `DB_PASSWORD` default empty
- `EUREKA_URL` default `http://localhost:8761/eureka/`
- `INVOICE_SERVICE_URL` default `http://localhost:8082`

Invoice Service contract:
`GET /api/businesses/{businessId}/invoices/{invoiceId}` returning `id` and `businessId`.

## APIs
Base path: `/api/businesses/{businessId}/commitments`

- `POST /api/businesses/{businessId}/commitments`
- `GET /api/businesses/{businessId}/commitments`
- `GET /api/businesses/{businessId}/commitments/{id}`
- `PATCH /api/businesses/{businessId}/commitments/{id}/approve`
- `PATCH /api/businesses/{businessId}/commitments/{id}/reject`
- `POST /api/businesses/{businessId}/commitments/installments`
- `GET /api/businesses/{businessId}/commitments/{commitmentId}/installments`
- `GET /api/businesses/{businessId}/commitments/installments/{id}`

Create commitment example:
```json
{
  "invoiceId": 10,
  "conversationId": 5,
  "sourceMessageId": "message-123",
  "summary": "Customer says they will pay next Friday",
  "aiClassification": "PAYMENT_PROMISE",
  "proposedAmount": 2500.00,
  "proposedPaymentDate": "2026-11-20"
}
```
Review body:
```json
{ "reviewedBy": 101, "note": "Confirmed with customer" }
```
Reject requires a nonblank note. Status values: `PENDING_REVIEW`, `APPROVED`, `REJECTED`.

Installment create body:
```json
{ "commitmentId": 1, "amount": 1250.00, "proposedDate": "2026-11-20" }
```
Installments can be created only after commitment approval and start as `PROPOSED`.

## AI integration boundary
The service accepts fields that can be mapped from AI Service's payment-promise analysis. It does not call Gemini directly, consume messages, or automatically create/approve commitments. A future orchestrator or event integration will map AI results to this API. Only `PAYMENT_PROMISE` is accepted when `aiClassification` is supplied.

## Security / migration notes
Business-scoped routes and repository queries are not authorization. JWT identity, business membership, and reviewer permissions will be enforced during Auth/Business integration. `reviewedBy` is temporary request data and must later be derived from JWT.
The new `business_id`, `conversation_id`, AI metadata and enum status columns are not automatically backfilled for old rows. Back up and migrate existing data before running against a populated database. If the database is empty, Hibernate can create/update the schema.
The original uploaded properties contained a database password. It has been removed from this project; rotate that password if it is real and has been used.

## Phase 2: JWT security

All commitment endpoints require a valid Bearer JWT issued by RecoverX Auth Service.
The service validates the token signature and expiry locally using the same `JWT_SECRET`
configured in Auth Service and Business Service.

Before serving a business-scoped request, Commitment Service checks the authenticated
user's active membership through Business Service:
`GET /api/businesses/{businessId}/members/{userId}`.

Authorization rules:
- Active business members can create and read commitments.
- Only OWNER and ADMIN can approve/reject commitments.
- Only OWNER and ADMIN can create installments.
- Active members can read installments.
- `reviewedBy` is derived from the authenticated JWT user ID; a client-supplied
  `reviewedBy` value is ignored.

The service forwards the current Bearer token when calling Business Service and
Invoice Service. This allows those services to validate the same caller identity.

Environment variables:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (must match Auth Service and Business Service)
- `BUSINESS_SERVICE_URL` (default `http://localhost:8088`)
- `INVOICE_SERVICE_URL` (default `http://localhost:8082`)
- `EUREKA_SERVER_URL` (default `http://localhost:8761/eureka/`)

Do not use the demonstration JWT secret or local database credentials in production.
