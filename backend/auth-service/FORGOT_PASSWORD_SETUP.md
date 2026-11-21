# RecoverX Auth Service - Forgot Password Setup

## Added functionality

- `POST /api/auth/forgot-password`: accepts `{ "email": "user@example.com" }` and emails a one-time reset link when the account exists. The response message is intentionally generic to avoid account enumeration.
- `POST /api/auth/reset-password`: accepts `{ "token": "token-from-email", "newPassword": "NewPassword123" }`.
- Reset tokens are generated with `SecureRandom`, stored only as SHA-256 hashes, expire after 15 minutes by default, and are single-use.
- Passwords continue to be encoded with the existing BCrypt `PasswordEncoder`.

## Required environment variables

Set these in the environment used to run Auth Service:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (Base64-encoded key of at least 32 bytes for HS256)
- `MAIL_USERNAME`
- `MAIL_PASSWORD` (Gmail App Password if using Gmail SMTP)

Optional variables:

- `DB_URL` (defaults to `jdbc:mysql://localhost:3306/recoverx_auth_db`)
- `EUREKA_SERVER_URL` (defaults to `http://localhost:8761/eureka/`)
- `JWT_EXPIRATION` (defaults to `86400000` milliseconds)
- `SMTP_HOST` (defaults to `smtp.gmail.com`)
- `SMTP_PORT` (defaults to `587`)
- `FRONTEND_RESET_PASSWORD_URL` (defaults to `http://localhost:5173/reset-password`)

The frontend must implement `/reset-password`, read the `token` query parameter, and submit the token and new password to the reset endpoint.

## API examples

### Request a reset email

```http
POST /api/auth/forgot-password
Content-Type: application/json

{ "email": "user@example.com" }
```

### Set a new password

```http
POST /api/auth/reset-password
Content-Type: application/json

{ "token": "TOKEN_FROM_EMAIL", "newPassword": "NewPassword123" }
```

## API Gateway change required

Your API Gateway must also permit unauthenticated POST requests to:

- `/api/auth/forgot-password`
- `/api/auth/reset-password`

The existing `/api/auth/**` route to `auth-service` should route these endpoints automatically. Check the Gateway's own security allowlist; changing Auth Service security alone does not bypass Gateway security.

## Important notes

- Do not commit real SMTP credentials, database passwords, or JWT secrets.
- The reset email sender is configured directly in Auth Service using the same SMTP environment variables as Customer Service. No Notification Service is required.
- For production, use HTTPS, rate-limit the forgot-password endpoint, and configure proper email delivery monitoring.
