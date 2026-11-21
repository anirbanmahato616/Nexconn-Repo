# Customer payment submission

A logged-in customer can submit a payment claim only when the invoice's
customer record is linked to that JWT user ID. Payment Service verifies this
through Customer Service's internal endpoint using `X-Internal-Service-Key`.

Submitting a payment creates a `PENDING` record. Only OWNER/ADMIN members can
verify or reject it. Verification updates Invoice Service through the
idempotent paymentId-based internal endpoint.

Configure the same `PAYMENT_SERVICE_INTERNAL_SECRET` in Customer, Invoice,
and Payment Services. The local fallback is for development only.
