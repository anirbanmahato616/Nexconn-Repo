# RecoverX Customer Service — Invitation Workflow Update

## Added functionality

- Business OWNER/ADMIN can invite a customer record.
- A cryptographically random, single-use 256-bit invitation token is generated.
- Only the SHA-256 hash of the token is stored in MySQL.
- Invitation expires after 48 hours.
- Sending a new invitation revokes earlier pending invitations for that customer.
- The invited user must be authenticated with a RecoverX account whose JWT email matches the invited email.
- Acceptance links `customers.user_id` to the authenticated JWT `userId`.
- Invitation status, accepted user and accepted timestamp are persisted.

## API endpoints (through API Gateway)

### Send invitation

`POST /api/businesses/{businessId}/customers/{customerId}/invite`

Authorization: Bearer token for an active OWNER or ADMIN.

Example:
`POST http://localhost:8080/api/businesses/7/customers/1/invite`

The customer must already exist in that business and must not already be linked to a RecoverX user.

### Accept invitation

`POST /api/businesses/{businessId}/customers/invitations/accept`

Authorization: Bearer token for the invited customer's RecoverX account.

Body:
```json
{
  "token": "token-from-invitation-email"
}
```

The authenticated JWT email must match the invited email. The customer must register or log in using the same email address before accepting.

## Required environment variables

Database:
- `DB_USERNAME` (defaults to `root`)
- `DB_PASSWORD` (required if MySQL root has a password)

SMTP (Gmail defaults):
- `SMTP_HOST` (default `smtp.gmail.com`)
- `SMTP_PORT` (default `587`)
- `SMTP_USERNAME` (your sending Gmail address)
- `SMTP_PASSWORD` (Gmail App Password, not your normal Gmail password)

Frontend:
- `FRONTEND_INVITATION_URL` (default `http://localhost:5173/accept-invitation`)

The invitation email link will be:
`FRONTEND_INVITATION_URL?businessId={businessId}&token={rawToken}`

The raw token is sent by email and is never stored in the database or returned by the API.

## Dependencies and assumptions

- Java 17
- Spring Boot 4.1.2
- Existing MySQL database `recoverx_customer_db`
- Existing Auth Service JWT contains `userId` and `email` claims
- Existing Business Service membership endpoint:
  `GET /api/businesses/{businessId}/members/{userId}`
- Existing API Gateway route `/api/businesses/*/customers/**` forwards these endpoints to Customer Service.

## Local run checklist

1. Set DB and SMTP environment variables in the Customer Service Run Configuration.
2. Ensure Eureka, Business Service and Auth Service are running.
3. Start Customer Service on port 8081.
4. Log in as a business OWNER or ADMIN.
5. Create a customer record if one does not exist.
6. Call the send-invitation endpoint.
7. Open the invitation email.
8. Register/login using the invited email.
9. Call the accept endpoint with the token from the email.
10. Fetch the customer record and confirm `userId` is populated.

## Important

Email sending requires valid SMTP credentials. For Gmail, create a Google App Password with 2-Step Verification enabled. Do not commit SMTP credentials or database passwords to Git.
