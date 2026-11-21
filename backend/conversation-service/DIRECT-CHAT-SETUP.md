# RecoverX direct Nexconn chat setup

## Endpoint

`POST http://localhost:8080/api/businesses/{businessId}/conversations/direct`

Authorization: `Bearer <RecoverX JWT>`

Body (invoiceId is optional):

```json
{
  "customerId": 2,
  "invoiceId": 1
}
```

The authenticated user must be an active member of the business. Conversation Service then fetches the customer record from Customer Service using `GET /api/businesses/{businessId}/customers/{customerId}`. The customer must have accepted the RecoverX invitation (`userId` must be populated). If invoiceId is supplied, Invoice Service validates that it belongs to this business.

The endpoint creates or returns one direct conversation for the (business, current user, customer user) pair. It does not create a channel through a server API: Nexconn direct channels are opened by the SDK using `DirectChannel(otherUserId)`. `channelId` and `otherUserNexconnId` are the customer's stable Nexconn user ID; `currentUserNexconnId` is the authenticated user's stable Nexconn ID. The frontend should request `/api/nexconn/token` for the logged-in user and initialize the SDK with that token. The customer separately obtains their own token when logged in.

## Database migration (existing database)

Back up the database first, then run this once against `recoverx_conversation_db`. It removes the old global unique channel constraint and allows a conversation to exist before an invoice is associated.

```sql
ALTER TABLE conversations MODIFY invoice_id BIGINT NULL;
ALTER TABLE conversations DROP INDEX uk_conversation_nexconn_channel;
ALTER TABLE conversations ADD COLUMN owner_user_id BIGINT NULL;
ALTER TABLE conversations ADD COLUMN customer_id BIGINT NULL;
ALTER TABLE conversations ADD COLUMN customer_user_id BIGINT NULL;
ALTER TABLE conversations ADD UNIQUE KEY uk_conversation_business_owner_customer (business_id, owner_user_id, customer_user_id);
```

If one or more of the new columns already exist, omit those `ADD COLUMN` statements. If the old unique index has a different name, inspect `SHOW INDEX FROM conversations` and drop the index whose column is `nexconn_channel_id`.

Set `CUSTOMER_SERVICE_URL` if Customer Service is not at `http://localhost:8081`.


## Nexconn message webhook -> AI Service handoff

Set the SAME strong random value in both services:
- Conversation Service: `RECOVERX_INTERNAL_API_KEY`
- AI Service: `RECOVERX_INTERNAL_API_KEY`

Also configure on Conversation Service:
- `AI_SERVICE_URL=http://localhost:8086` (or your AI service URL)
- `RECOVERX_DEFAULT_TIMEZONE=Asia/Kolkata`

AI Service exposes `POST /api/ai/analyze/internal`, protected by `X-Internal-Api-Key`.
The existing user-facing `POST /api/ai/analyze` remains JWT + business-membership protected.

The webhook analyzes only customer-originated direct messages that map to exactly one RecoverX conversation and have an explicit `invoiceId`. Messages without invoice context or ambiguous tenant mapping are skipped rather than guessing. The AI result is stored in `nexconn_webhook_events.ai_analysis_json`.

This integration currently stops after AI analysis. It does NOT automatically create a commitment; commitment creation/approval remains a separate human-reviewed workflow.
