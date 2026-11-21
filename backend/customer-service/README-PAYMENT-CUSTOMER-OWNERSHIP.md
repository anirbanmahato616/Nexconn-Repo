# Payment customer ownership integration

Payment Service verifies customer ownership through:
`GET /internal/businesses/{businessId}/customers/{customerId}/user/{userId}`

The endpoint requires `X-Internal-Service-Key`. Configure the same
`PAYMENT_SERVICE_INTERNAL_SECRET` value in Customer Service, Invoice Service,
and Payment Service. The local fallback is for development only.

Customer Service's SMTP credentials are now environment-backed:
`SMTP_USERNAME` and `SMTP_PASSWORD`. Do not commit SMTP credentials.
