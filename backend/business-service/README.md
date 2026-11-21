# RecoverX Business Service

Business Service for RecoverX. Uses Java 17, Spring Boot 4.1.2, MySQL, Eureka, Spring Security and JJWT 0.12.6.

## Configuration

Configure the same Base64 JWT signing secret in Auth Service and Business Service.

```properties
jwt.secret=${JWT_SECRET:UmVjb3ZlclgtRGVtby1TZWNyZXQtS2V5LUZvci1IUzI1Ng==}
```

The fallback is for local development only. Set `JWT_SECRET` as an environment variable for deployment. Never commit a production signing secret.

Database settings can be supplied through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Eureka URL can be supplied through `EUREKA_SERVER_URL`.

## Authentication

All API endpoints require:

```http
Authorization: Bearer <accessToken>
```

The token must be signed with the same HMAC key as Auth Service and contain:
- `sub`: authenticated user's email
- `userId`: authenticated user's numeric database ID

Business Service validates the JWT signature and expiration locally. It does not call Auth Service for every request.

## Main endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/businesses` | Authenticated user creates a business; owner ID comes from JWT |
| GET | `/api/businesses/owner/me` | User's own active businesses |
| GET | `/api/businesses/{businessId}` | Active business member |
| PUT | `/api/businesses/{businessId}` | Business OWNER |
| DELETE | `/api/businesses/{businessId}` | Business OWNER; soft deactivation |
| POST | `/api/businesses/{businessId}/members` | OWNER or ADMIN; ADMIN can add STAFF only |
| GET | `/api/businesses/{businessId}/members` | OWNER or ADMIN |
| GET | `/api/businesses/memberships/me` | User's own memberships |
| GET | `/api/businesses/{businessId}/members/{userId}` | User themselves, OWNER or ADMIN |
| DELETE | `/api/businesses/{businessId}/members/{userId}` | OWNER or ADMIN; ADMIN cannot deactivate OWNER/ADMIN |

## Example: create a business

```http
POST /api/businesses
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "name": "ABC Enterprises",
  "email": "abc@example.com",
  "phone": "9876543210",
  "address": "Kolkata, West Bengal",
  "industry": "Technology"
}
```

Do not send `ownerUserId`. The service derives it from the verified JWT.

## Important limitations

- Membership user IDs are currently accepted by the add-member endpoint. Validation that the target user exists in Auth Service, and RecoverX's customer invitation/acceptance workflow, are separate integration tasks.
- JWT uses a shared HMAC secret for the current MVP. Use a strong randomly generated secret and secure secret management before deployment.
- API Gateway routing and gateway-level authentication are not configured in this service update.
- No Maven build could be run in the preparation environment because Maven Wrapper could not download Maven. Import into STS, update the Maven project, and compile/test there.
