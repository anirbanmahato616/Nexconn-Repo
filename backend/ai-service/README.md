# RecoverX AI Service

Spring Boot AI service using Spring AI Google GenAI (Gemini).

## Endpoint

`POST /api/ai/analyze`

This endpoint requires a valid RecoverX JWT in the `Authorization: Bearer <token>` header.
The request must include a positive `businessId`. AI Service validates the JWT locally and
checks that the authenticated user is an active member of that business through Business Service.

## Environment variables

- `GEMINI_API_KEY` — required Google Gemini API key; do not commit it.
- `GEMINI_MODEL` — optional; defaults to `gemini-2.5-flash`.
- `JWT_SECRET` — required Base64-encoded JWT signing secret; must match Auth Service and all other JWT-validating services.
- `BUSINESS_SERVICE_URL` — optional; defaults to `http://localhost:8088`.
- `EUREKA_URL` — optional; defaults to `http://localhost:8761/eureka/`.
- `SERVER_PORT` — optional; defaults to `8086`.

## Authorization

- JWT signature, expiry, subject and numeric `userId` are validated.
- Business membership is checked using `GET /api/businesses/{businessId}/members/{userId}` with the original Bearer token forwarded.
- Membership must be active.
- `/actuator/health` and `/actuator/info` are public; AI analysis is protected.

## Analysis categories

- `PAYMENT_PROMISE`
- `ALREADY_PAID`
- `PAYMENT_REFUSAL`
- `UNCLEAR`

An AI classification is only an interpretation of a message. `ALREADY_PAID` is a customer claim, not proof of payment. AI Service does not verify payments, mark invoices paid, or approve commitments. A human/business owner must review proposed commitments.

## Request example

```json
{
  "message": "I will pay 5000 next Friday",
  "messageTimestamp": "2026-11-10T10:30:00Z",
  "timezone": "Asia/Kolkata",
  "businessId": 501,
  "invoiceId": 1001,
  "conversationId": 201,
  "sourceMessageId": 9001
}
```

`businessId` is required for authorization. The other correlation IDs remain optional.

## Integration boundaries

This service currently analyzes the supplied message and returns structured JSON. It does not call Conversation Service or Commitment Service and does not create commitments. Conversation/Nexconn integration must call this endpoint with the actual message, timestamp and timezone.
