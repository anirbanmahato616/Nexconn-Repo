# RecoverX Auth Service - Phase 2 JWT update

## Change
JWTs now contain:
- `sub`: normalized user email (existing subject retained)
- `email`: normalized user email
- `userId`: the persisted Auth Service user database ID
- `iat` and `exp`: issue and expiry timestamps

Registration and login both generate the updated token after the user record has been saved or authenticated.

## JWT service API
- `generateToken(Long userId, String email)`
- `extractEmail(String token)`
- `extractUserId(String token)`
- `isTokenValid(String token, String email, Long userId)`

The JWT authentication filter now requires the `userId` claim and validates the token's signature, email, user ID claim and expiry.

## Postman verification
1. Start Eureka and MySQL, then start Auth Service.
2. Register a new user or log in with an existing account:
   `POST http://localhost:8087/api/auth/login`
3. Copy `accessToken` from the response.
4. Decode the token locally (for example, using a trusted JWT debugger) and verify that the payload includes `userId` and `email`. Do not paste production tokens into public websites.
5. Call `GET http://localhost:8087/api/auth/test` with `Authorization: Bearer <accessToken>`. Expected: `200 OK`.
6. Call the same endpoint without a token or with a modified token. Expected: `401 Unauthorized`.

The login/register response structure is unchanged and still includes `userId` as a response field.

## Important
- Existing tokens issued before this change do not contain `userId`; log in again to obtain a new token.
- The current JWT secret in the supplied project is a demo secret. Replace it with a securely generated secret before production and configure the same signing/verification key in services that validate these tokens.
- The supplied `application.properties` still contains local database settings. Use environment variables/secrets for deployment.
