# Payment integration

Payment Service reads invoice details through the internal endpoint:
`GET /internal/businesses/{businessId}/invoices/{invoiceId}/details`

It records verified payments through:
`PATCH /internal/businesses/{businessId}/invoices/{invoiceId}/verified-payments`

Both require `X-Internal-Service-Key`. Configure the same
`PAYMENT_SERVICE_INTERNAL_SECRET` value in Customer Service, Invoice Service,
and Payment Service. The local fallback is for development only.
