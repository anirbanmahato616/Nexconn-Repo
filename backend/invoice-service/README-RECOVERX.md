# RecoverX Invoice Service — Updated

## What changed

- Replaced `ownerId` with `businessId`.
- Kept `customerId` as the ID of the customer record in Customer Service (not the RecoverX user ID).
- All CRUD operations are scoped by `businessId`.
- Invoice numbers must be unique within a business.
- New invoices start with status `UNPAID`.
- General invoice create/update requests cannot set or change payment status.
- General invoice updates cannot move an invoice to another business or change its assigned customer.
- Database credentials are read from environment variables.

## Requirements

- Java 17
- Maven
- MySQL
- Eureka Server at port 8761 (or configure `EUREKA_URL`)

## Database

Create the empty database if it does not exist:

```sql
CREATE DATABASE recoverx_invoice_db;
```

Hibernate `ddl-auto=update` creates/updates the `invoices` table.

The important columns are:
- `id`: Invoice Service's invoice ID.
- `business_id`: ID of the business that issued the invoice.
- `customer_id`: Customer Service's customer-record ID.
- `invoice_number`, `description`, `amount`, `issue_date`, `due_date`, `status`.

No cross-service JPA relationship is used. IDs are scalar references.

## Environment variables (Windows PowerShell)

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:DB_URL="jdbc:mysql://localhost:3306/recoverx_invoice_db"
$env:EUREKA_URL="http://localhost:8761/eureka/"
```

Set real values in your local environment; never commit passwords to Git.

## Import into STS

1. Extract this ZIP.
2. In STS, choose **File > Import > Maven > Existing Maven Projects**.
3. Select the extracted `invoice-service` folder and its `pom.xml`.
4. Finish, then right-click the project > **Maven > Update Project**.
5. Set Java compiler level to 17 if required.
6. Create the database, set environment variables, and run `InvoiceServiceApplication.java`.

## API endpoints

Base path: `/api/businesses/{businessId}/invoices`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/businesses/{businessId}/invoices` | Create invoice |
| GET | `/api/businesses/{businessId}/invoices` | List invoices for a business |
| GET | `/api/businesses/{businessId}/invoices/{invoiceId}` | Get invoice within a business |
| PUT | `/api/businesses/{businessId}/invoices/{invoiceId}` | Update invoice details |

Example create request:

```http
POST http://localhost:8082/api/businesses/501/invoices
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "invoiceNumber": "INV-1001",
  "description": "Website development",
  "amount": 15000.00,
  "issueDate": "2026-11-10",
  "dueDate": "2026-11-20"
}
```

The response includes `businessId` and an initial `status` of `UNPAID`.

## JWT security added

- All invoice endpoints require a valid Bearer JWT.
- JWT signature and expiry are validated locally using the same `JWT_SECRET` as Auth Service and Business Service.
- The token must contain the expected `email` subject/claim and numeric `userId` claim.
- Before any invoice operation, Invoice Service forwards the caller's Authorization header to Business Service and verifies that the caller has an active membership in the requested business.
- Requests without a token or with an invalid/expired token return `401`.
- Authenticated users who are not active members of the requested business receive `403`.
- The membership lookup uses `GET /api/businesses/{businessId}/members/{userId}`.
- This version allows any active business member to use the existing invoice CRUD endpoints. A finer OWNER/ADMIN/STAFF permission matrix should be defined separately before enforcing role-specific invoice actions.

## Remaining integration/security limitations

1. The service accepts `customerId` as a scalar Customer Service record ID. It does not yet call Customer Service to verify that the customer record belongs to the same `businessId`. That inter-service validation remains required.
2. Payment Service does not yet have a dedicated protected internal API to update invoice payment status. Ordinary invoice CRUD requests still cannot set `PAID`; payment-status integration will be designed with Payment Service.
3. Keep service ports internal in deployment. JWT validation is enabled here, but the API Gateway should remain the public entry point.

4. Existing `owner_id` data is not automatically migrated to `business_id`. You said this database has already been created and tested; if it contains invoice rows, back up and migrate them deliberately. If it is empty, Hibernate can create the new structure.


## Verified payment integration

Invoice Service tracks `paidAmount`, `outstandingAmount`, and invoice status:
- `UNPAID` when no verified payment exists
- `PARTIALLY_PAID` when some, but not all, of the invoice is paid
- `PAID` when the full invoice amount is paid

Payment Service records a verified payment through the direct internal endpoint:
`PATCH /internal/businesses/{businessId}/invoices/{invoiceId}/verified-payments`

The endpoint requires `X-Internal-Service-Key` and is intended for direct service-to-service traffic, not API Gateway use. The payment ID is stored with a unique constraint so retries do not add the same payment twice.

Configure the same value in Invoice Service and Payment Service:
`PAYMENT_SERVICE_INTERNAL_SECRET=...`

The local development fallback is `RecoverX-Local-Payment-Internal-Secret-Change-Before-Deployment`. Replace it with a long random secret before deployment and do not expose the Invoice Service internal port publicly.
