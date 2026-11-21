# AI analysis to Commitment Service handoff

AI Service remains a focused analysis service. It returns a structured result and does not create or approve commitments.

After `POST /api/ai/analyze` returns `analysisType = PAYMENT_PROMISE` and `paymentIntent = true`, the client should call:

`POST /api/businesses/{businessId}/commitments/from-ai-analysis`

Forward the AI response fields as follows:

| AI response field | Commitment request field |
|---|---|
| `invoiceId` | `invoiceId` |
| `analysisType` | `analysisType` |
| `paymentIntent` | `paymentIntent` |
| `conversationId` | `conversationId` |
| `sourceMessageId` | `sourceMessageId` (convert to string) |
| `summary` | `summary` |
| `amount` | `amount` |
| `proposedDate` | `proposedDate` |

The commitment request must be made with a valid JWT for an active member of the business. The commitment remains `PENDING_REVIEW`; only an OWNER or ADMIN can approve/reject it. Do not create a commitment for `ALREADY_PAID`, `PAYMENT_REFUSAL`, or `UNCLEAR` classifications.
