# RecoverX Customer Service (updated)

## What changed

- Customer records are now scoped to `businessId`, not `businessOwnerId`.
- Added nullable `userId` to link a customer record to a RecoverX account after invitation acceptance.
- Customer CRUD endpoints are business-scoped.
- Duplicate email addresses are prevented within the same business; the same email may exist in different businesses.
- Updating a customer cannot move it to another business or change its linked RecoverX user ID.
- Added consistent 400, 404 and 409 error responses.
- Database credentials are read from environment variables.

## Requirements

- Java 17
- Maven
- MySQL
- Eureka Server (optional while local Eureka is running; see note below)

## Database

Create the empty database once:

```sql
CREATE DATABASE recoverx_customer_db;
```

Hibernate `ddl-auto=update` will create/update the `customers` table.

The new table contains:
- `id`
- `business_id`
- `user_id` (nullable until invitation acceptance is integrated)
- `name`
- `email`
- `phone`
- `created_at`

Because you confirmed the database currently has no tables/data, no data migration is needed. The old `business_owner_id` field is removed from the new entity.

## Environment variables

Set these before starting the service.

Windows PowerShell (current terminal session):

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:DB_URL="jdbc:mysql://localhost:3306/recoverx_customer_db"
$env:EUREKA_URL="http://localhost:8761/eureka/"
```

For a persistent Windows environment variable, use `setx`, then restart STS/terminal. Do not put real credentials in this file or commit them to Git.

If you are not running Eureka yet, you can temporarily disable Eureka client registration in `pom.xml` / configuration, or start your Eureka server at port 8761.

## API endpoints

Base path: `/api/businesses/{businessId}/customers`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/businesses/{businessId}/customers` | Create customer |
| GET | `/api/businesses/{businessId}/customers` | List customers for a business |
| GET | `/api/businesses/{businessId}/customers/{customerId}` | Get one customer within a business |
| PUT | `/api/businesses/{businessId}/customers/{customerId}` | Update customer |

Example create request:

```http
POST http://localhost:8081/api/businesses/10/customers
Content-Type: application/json
```

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "phone": "9876543210"
}
```

The `businessId` comes from the URL, not from the request body.

## Important security note

This version scopes every customer query by `businessId`, but it does **not yet authenticate the caller or prove that the caller is authorized for that business**. That is intentionally deferred until Auth Service and business membership integration are implemented. Do not expose this service publicly or treat a caller-supplied business ID as authorization before then. The future JWT/security integration must derive the user identity from the authenticated token and check the user's membership in the requested business.

## Import into Spring Tool Suite

1. Extract this ZIP.
2. In STS, select **File > Import > Maven > Existing Maven Projects**.
3. Browse to the extracted `customer-service` folder.
4. Select the detected `pom.xml` and finish.
5. Right-click the project > **Maven > Update Project**.
6. Set Java compiler level to 17 if STS does not detect it.
7. Create the MySQL database and set environment variables.
8. Run `CustomerServiceApplication.java` as a Spring Boot App.

## Important API change

The previous endpoints `/api/customers...` and request field `businessOwnerId` have been replaced by business-scoped paths. Any existing Postman requests or frontend calls must be updated to use the new paths and `businessId` path variable.
