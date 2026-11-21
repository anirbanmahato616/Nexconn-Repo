# RecoverX Conversation Service

Stores RecoverX's mapping between a business invoice and its Nexconn channel. Nexconn remains responsible for chat transport/UI; this service stores the RecoverX-side conversation reference.

## Configuration

Environment variables:
- `DB_URL` (default `jdbc:mysql://localhost:3306/recoverx_conversation_db`)
- `DB_USERNAME` (default `root`)
- `DB_PASSWORD` (set in your environment; do not commit credentials)
- `JWT_SECRET` (must match Auth Service and Business Service)
- `EUREKA_SERVER_URL` (default `http://localhost:8761/eureka/`)
- `BUSINESS_SERVICE_URL` (default `http://localhost:8088`)
- `INVOICE_SERVICE_URL` (default `http://localhost:8082`)

Invoice validation expects:
`GET /api/businesses/{businessId}/invoices/{invoiceId}`
with JSON fields `id` and `businessId`. Conversation Service forwards the incoming Bearer token to Invoice Service.

Business membership validation expects:
`GET /api/businesses/{businessId}/members/{userId}`
with JSON fields `businessId`, `userId`, and `active`.

## API

Base path: `/api/businesses/{businessId}/conversations`

- `POST /api/businesses/{businessId}/conversations`
- `GET /api/businesses/{businessId}/conversations`
- `GET /api/businesses/{businessId}/conversations/{id}`
- `GET /api/businesses/{businessId}/conversations/invoice/{invoiceId}`

Create body:
```json
{
  "invoiceId": 1,
  "nexconnChannelId": "nexconn-channel-unique-id"
}
```

## Security behavior

- All API endpoints require a valid JWT containing `userId`, `email`, and an expiration.
- Every operation checks that the authenticated user is an active member of the requested business through Business Service.
- Invoice Service validation on conversation creation forwards the original Authorization header.
- Requests without or with invalid tokens receive 401; authenticated users without active business membership receive 403.
- This membership-based authorization does not yet implement customer-only access to conversations assigned to their invoices. That needs a separate customer relationship/assignment authorization flow before customer-facing chat access is enabled.
- This service does not create Nexconn channels, send messages, manage participants, or call Nexconn APIs. It stores the channel ID supplied by the upstream integration.

## Nexconn server integration (initial phase)

Configure these environment variables before using the Nexconn token endpoint:

- `NEXCONN_APP_KEY`
- `NEXCONN_APP_SECRET` (never commit this value)
- `NEXCONN_BASE_URL` (Singapore: `https://api.sg-light-api.com`)

The authenticated endpoint `POST /api/nexconn/token` issues a Nexconn access token for the current RecoverX JWT user. RecoverX user IDs are mapped to stable opaque Nexconn IDs (`rx_...`); the same ID is generated on every request for that account.

Configure Nexconn's `message:send` post-event webhook to call `POST /api/nexconn/webhook`. This endpoint verifies `AppKey`, `Nonce`, `Timestamp`, and `Signature`, rejects stale requests, and persists each event once using Nexconn's unique event ID. It currently stores events for subsequent conversation/AI processing; it does not yet associate messages with invoices or invoke the AI service.

If server API messages should also be delivered to this webhook, enable **Include Server API Messaging in post-event webhooks** in Nexconn Chat settings.
