# RecoverX Commitment + AI integration (MVP)

## What changed

- Added `POST /api/businesses/{businessId}/commitments/from-ai-analysis`.
- The endpoint accepts the structured response from AI Service and creates a commitment in `PENDING_REVIEW` only when `analysisType` is `PAYMENT_PROMISE` and `paymentIntent` is `true`.
- Invoice ownership is still checked through Invoice Service.
- Repeated submission for the same business + conversation + source message returns the existing commitment rather than creating a duplicate.
- Installments can only be added to APPROVED commitments, and their cumulative amount cannot exceed the commitment's proposed amount (when an amount was extracted).
- AI does not approve commitments or verify payments. An OWNER/ADMIN must review them.

## 1. Analyze a message

`POST http://localhost:8080/api/ai/analyze`

Authorization: Bearer token of an active business member.

```json
{
  "message": "I will pay Rs. 5000 on 15 November 2026.",
  "messageTimestamp": "2026-11-11T14:30:00Z",
  "timezone": "Asia/Kolkata",
  "businessId": 1,
  "invoiceId": 1,
  "conversationId": 1,
  "sourceMessageId": 101
}
```

Use the actual IDs from your database. AI Service returns `analysisType`, `paymentIntent`, `amount`, `proposedDate`, and `summary` along with correlation IDs.

## 2. Create a commitment from the AI response

`POST http://localhost:8080/api/businesses/1/commitments/from-ai-analysis`

Authorization: Bearer token of an active business member. Content-Type: application/json.

Copy these fields from the AI response into the request body (sourceMessageId can be sent as a string):

```json
{
  "invoiceId": 1,
  "analysisType": "PAYMENT_PROMISE",
  "paymentIntent": true,
  "conversationId": 1,
  "sourceMessageId": "101",
  "summary": "Customer promises to pay Rs. 5000 on 15 November 2026.",
  "amount": 5000,
  "proposedDate": "2026-11-15"
}
```

Expected: `201 Created`, status `PENDING_REVIEW`. If the same business/conversation/source message is submitted again, the existing commitment is returned.

A result classified as `ALREADY_PAID`, `PAYMENT_REFUSAL`, or `UNCLEAR` must not create a commitment.

## 3. Owner/admin approves

`PATCH http://localhost:8080/api/businesses/1/commitments/{commitmentId}/approve`

Authorization: OWNER_TOKEN. Body:

```json
{ "note": "Reviewed with the customer; payment date accepted." }
```

Expected: `200 OK`, status `APPROVED`.

## 4. Create installments

`POST http://localhost:8080/api/businesses/1/commitments/installments`

Authorization: OWNER_TOKEN. Body:

```json
{
  "commitmentId": 1,
  "amount": 2500,
  "proposedDate": "2026-11-15"
}
```

Add a second installment for the remaining amount/date if required. Cumulative installment amount cannot exceed the approved commitment amount.

## 5. List installments

`GET http://localhost:8080/api/businesses/1/commitments/1/installments`

Authorization: OWNER_TOKEN.

### Important MVP boundary

Installments are currently planned (`PROPOSED`) records. They are not automatically marked paid by Payment Service yet. Payment verification remains the source of truth for actual money received. Do not treat a proposed installment as a verified payment.
