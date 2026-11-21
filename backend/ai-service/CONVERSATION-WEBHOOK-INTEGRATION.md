# RecoverX AI Service - Conversation webhook integration

## New internal endpoint
`POST /api/ai/analyze/internal`

Headers:
- `Content-Type: application/json`
- `X-Internal-Api-Key: <same secret configured in Conversation Service>`

The endpoint accepts the existing `AIAnalysisRequest` DTO and returns the existing `PaymentAnalysisResponse` DTO. It is intended only for server-to-server calls from Conversation Service. The existing `/api/ai/analyze` endpoint remains protected by JWT and Business Service membership validation.

## Required environment variable
Set `RECOVERX_INTERNAL_API_KEY` to the same long random secret in BOTH AI Service and Conversation Service. Do not commit it to Git.

AI Service still requires `GEMINI_API_KEY` and the same `JWT_SECRET` used by the other RecoverX services.

## Webhook behavior
Conversation Service verifies Nexconn webhook signatures, stores events idempotently, maps customer-originated direct messages to a RecoverX conversation, and calls this internal endpoint only when:
- the sender maps to the customer in the conversation;
- the Nexconn channel maps to the owner;
- exactly one RecoverX conversation matches; and
- that conversation has an explicit invoice ID.

AI analysis is stored in the Conversation Service `nexconn_webhook_events.ai_analysis_json` column. The AI does not verify payments or approve commitments.
