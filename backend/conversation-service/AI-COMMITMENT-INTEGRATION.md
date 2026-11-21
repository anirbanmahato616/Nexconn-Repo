# RecoverX AI -> Commitment handoff

## What this integration does
When a Nexconn `message:send` webhook is received, Conversation Service:
1. Validates the Nexconn signature.
2. Resolves the customer conversation and its explicit invoice context.
3. Calls AI Service for message analysis.
4. Persists the AI JSON result in `nexconn_webhook_events`.
5. If and only if `analysisType` is `PAYMENT_PROMISE` and `paymentIntent` is true, calls Commitment Service's internal endpoint.
6. Commitment Service creates (or returns an idempotent existing) `PENDING_REVIEW` commitment. It never auto-approves.

## Required environment variables

Conversation Service:
- `RECOVERX_INTERNAL_API_KEY`: shared random secret used for both AI and Commitment internal calls.
- `COMMITMENT_SERVICE_URL`: optional; defaults to `http://localhost:8084`.
- Existing `AI_SERVICE_URL`, Nexconn and database variables remain required as before.

Commitment Service:
- `RECOVERX_INTERNAL_API_KEY`: exactly the same value as Conversation Service.

AI Service:
- `RECOVERX_INTERNAL_API_KEY`: exactly the same value as Conversation Service (for the existing internal AI endpoint).

Set the same strong random value in all three services. Do not commit it to source control or expose it in Vite variables.

## New internal endpoint
`POST /api/internal/businesses/{businessId}/commitments/from-ai-analysis`

Header:
`X-Internal-Api-Key: <shared secret>`

This endpoint is permitted through the Commitment Service security chain but validates the shared key in the controller using a constant-time comparison. Do not expose the endpoint publicly without network restrictions and secret management.

## Webhook event statuses
- `AI_ANALYZED`: AI result stored, no eligible commitment was created.
- `COMMITMENT_CREATED_PENDING_REVIEW`: AI classified a payment promise and Commitment Service accepted the pending commitment.
- Other existing skipped/ignored statuses remain unchanged.

## Test
Send a customer message with a clear promise and invoice context. Then inspect:
`nexconn_webhook_events.processing_status` and `ai_analysis_json`.

Check Commitment Service database:
`SELECT * FROM commitments ORDER BY id DESC;`

Expected status for a new AI payment promise: `PENDING_REVIEW`.

Then use the existing owner/admin approval endpoint. AI never approves automatically.
