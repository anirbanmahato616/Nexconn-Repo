<<<<<<< HEAD
# RecoverX

**AI-powered invoice follow-up and payment recovery platform for small businesses.**

RecoverX brings invoices, customer conversations, proposed payment commitments and verified payment records into one place. An AI assistant reads customer messages such as *"I can pay ₹10,000 this Friday and ₹20,000 next month"* and turns them into structured proposals that the business owner reviews.

> **Core principle:** AI never verifies payments, never marks an invoice as paid and never approves a commitment. Every financial decision is made by a human.

> **Status:** Work in progress (MVP). See [Project Status](#project-status) for what is built and what is planned.

---

## Table of Contents

- [Key Features](#key-features)
- [Product Model](#product-model)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Customer Onboarding](#customer-onboarding)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Overview](#api-overview)
- [Business Rules](#business-rules)
- [Security](#security)
- [Testing](#testing)
- [Project Status](#project-status)
- [MVP Scope](#mvp-scope)
- [Contributing](#contributing)

---

## Key Features

- **One account, two modes:** a single login can own businesses (Business Mode) and be a customer of other businesses (Customer Mode).
- **Invitation-only onboarding:** owners invite customers by email; customers explicitly accept.
- **Invoice management:** create, assign and track invoices with calculated balances and statuses.
- **Invoice-linked chat:** private real-time conversation per invoice via Nexconn.
- **AI message analysis:** extracts intent, amounts, dates and uncertainty from customer messages.
- **Commitment review:** owners approve or reject AI-extracted proposals.
- **Verified payment recording:** balances are computed only from payments the owner has verified.
- **Dashboards:** totals, outstanding balances and overdue counts for both modes.

## Product Model

One person has one RecoverX account (one email, one password, one user ID).

| | Business Mode | Customer Mode |
|---|---|---|
| Acts as | Owner of a selected business | Customer of connected businesses |
| Can do | Manage customers and invoices, record verified payments, review AI proposals, chat with customers, view business dashboard | View own invoices and payment history, chat with the business owner, submit proposed payment dates and amounts |
| Sees | Only the business they own | Only invoices assigned to them, per connected business |

> Switching modes changes the working context only. It does **not** grant access. The backend enforces ownership, customer assignment and invoice-level access on every protected API.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React |
| Backend | Java, Spring Boot (microservices) |
| Service discovery | Eureka |
| Entry point | Spring Cloud API Gateway |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL |
| Real-time chat | Nexconn Chat SDK |
| AI | LLM API with structured output |

## Architecture

```
            React (Business Mode / Customer Mode)
                          │
                    API Gateway
                          │
     ┌────────┬───────────┼────────────┬──────────┐
   Auth   Customer     Invoice      Payment   Commitment
  Service  Service     Service      Service    Service
     │                                            │
  Invitation        Conversation Service     AI Service
  + Email                  │                      │
                       Nexconn                 LLM API
                          │
               Eureka (service discovery)
                          │
                        MySQL
```

**Main flows**

- **Invoice:** React → Gateway → service → validation → MySQL → response.
- **Chat:** backend verifies identity and authorization → generates a Nexconn token → React connects to Nexconn.
- **AI:** relevant message → backend → LLM → schema and business validation → proposal saved → owner reviews.
- **Payment:** owner verifies external payment → backend validates → stored → balance and status recalculated.

## Customer Onboarding

1. Owner enters the customer's name, email and phone in Business Mode and sends an invitation.
2. The backend creates a secure, single-use, expiring token; only its **hash** is stored, with status `PENDING`.
3. RecoverX emails the invitation link; React opens the acceptance page.
4. An existing customer logs in; a new customer registers and verifies the invited email.
5. The customer explicitly accepts; React submits the token to the backend.
6. The backend validates token, expiry, status and authenticated email, then in one transaction creates the business-customer connection and marks the invitation `ACCEPTED`.
7. The business appears automatically in the customer's dashboard, showing only invoices assigned to them.

## Project Structure

> Adjust folder and service names to match your repository.

```
RecoverX/
├── frontend/                 # React app
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── hooks/
│       ├── context/
│       └── utils/
├── backend/
│   ├── eureka-server/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── customer-service/
│   ├── invoice-service/
│   ├── payment-service/
│   ├── commitment-service/
│   ├── conversation-service/
│   └── ai-service/
├── database/
│   └── schema.sql
├── docs/
│   ├── architecture.md
│   ├── api-documentation.md
│   └── database-design.md
├── README.md
└── .gitignore
```

## Getting Started

### Prerequisites

- Java (JDK version used by your Spring Boot setup) and Maven or Gradle
- Node.js and npm
- MySQL
- Nexconn account and credentials (App ID and App Secret)
- API key for your chosen LLM provider

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd RecoverX
```

### 2. Set up the database

```sql
CREATE DATABASE recoverx;
```

Apply the schema from `database/schema.sql` or run your migrations.

### 3. Configure environment variables

Copy the example environment file and fill in your values (see [Configuration](#configuration)).

### 4. Start the backend (in this order)

1. `eureka-server`
2. `api-gateway`
3. `auth-service`
4. The remaining services (customer, invoice, payment, commitment, conversation, AI)

```bash
cd backend/<service-name>
./mvnw spring-boot:run
```

### 5. Start the frontend

```bash
cd frontend
npm install
npm start
```

## Configuration

Never commit secrets. Keep them in environment variables or a secret manager.

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET` | JWT signing key |
| `NEXCONN_APP_ID`, `NEXCONN_APP_SECRET` | Nexconn credentials (**backend only**) |
| `AI_API_KEY`, `AI_MODEL` | LLM provider credentials and model |
| `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Invitation email delivery |
| `FRONTEND_URL` | Used to build invitation links |

The Nexconn App Secret and AI key must never appear in React or any browser bundle.

## API Overview

Proposed contract; the full schema lives in OpenAPI/Swagger (`docs/api-documentation.md`).

| Area | Method | Endpoint | Purpose |
|---|---|---|---|
| Auth | POST | `/api/auth/register` | Register a user |
| Auth | POST | `/api/auth/login` | Log in and receive a JWT |
| Auth | GET | `/api/auth/me` | Current user profile |
| Businesses | POST / GET | `/api/businesses` | Create / list the user's businesses |
| Customers | POST / GET | `/api/customers` | Add / list a business's customers |
| Invitations | POST | `/api/invitations` | Send a customer invitation |
| Invitations | POST | `/api/invitations/accept` | Accept an invitation |
| Invoices | POST / GET | `/api/invoices` | Create / list authorized invoices |
| Invoices | GET | `/api/invoices/{id}/balance` | Calculated balance |
| Chat | POST | `/api/chat/token` | Generate Nexconn token |
| AI | POST | `/api/ai/analyze-message` | Analyze an authorized message |
| Commitments | POST | `/api/commitments/{id}/approve` | Approve a proposal |
| Commitments | POST | `/api/commitments/{id}/reject` | Reject a proposal |
| Payments | POST / GET | `/api/invoices/{id}/payments` | Record / list verified payments |
| Dashboard | GET | `/api/dashboard/business-summary` | Business Mode totals |
| Dashboard | GET | `/api/dashboard/customer-summary` | Customer Mode summary |

Example invoice request:

```json
{ "customerId": 101, "invoiceNumber": "INV-001", "description": "Website Development", "amount": 30000, "dueDate": "2026-11-16" }
```

## Business Rules

**Remaining balance = Invoice amount − Sum of verified payments.**

- Balance is always calculated, never edited directly by users or AI.
- A promise to pay (commitment) is never a payment.
- Payments are recorded only by the business owner after verification.
- Invoice statuses: `UNPAID`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `CANCELLED`.
- AI output is validated (schema, positive amounts, remaining balance, ambiguous dates) before it is saved as a proposal. The AI must not invent exact dates.

## Security

- Passwords hashed with BCrypt; JWT-based sessions.
- Authorization enforced in the backend on every request (business ownership, customer assignment, invoice access, chat membership).
- Invitation tokens are hashed, single-use and expiring; the authenticated email must match the invited email.
- Chat messages are treated as untrusted input to the AI.
- No passwords, tokens or private chat content in logs.
- Use HTTPS in deployment.

## Testing

Key areas to cover:

- Authentication and authorization, including negative tests (a customer opening another customer's invoice, a Customer Mode user calling an owner-only API).
- Invitation edge cases: expired, reused, wrong-email and tampered tokens.
- Invoice validation and balance/status calculation.
- AI cases: valid promise, ambiguous date, partial proposal, dispute, malformed JSON.
- Payments: duplicate references, concurrent updates, partial and full payment.
- Integration failures: Nexconn down, AI timeout, database errors.

## Project Status

| Area | Status |
|---|---|
| Eureka | Completed |
| Customer, Invoice, Payment, Commitment, Conversation and AI services | Completed (per blueprint) |
| API Gateway | Project set up; configuration pending |
| Auth Service | **Next** |
| Invitation + email workflow | Planned |
| Dashboard API | Planned |
| Nexconn integration | Planned |
| Inter-service integration | Planned |
| End-to-end and security testing | Planned |

Verify this table against your commits and tests before sharing externally.

## MVP Scope

**In scope:** authentication, business and customer management, invitation onboarding, invoices, invoice chat, AI analysis, commitment review, payment recording, dashboards.

**Out of scope:** Razorpay or other payment gateways, WhatsApp/SMS automation, native mobile apps, voice/video calls, group chat, accounting integrations, automatic collection or approval.

Customers pay externally (for example UPI or bank transfer); the owner verifies and records the payment.

### MVP acceptance scenario

Owner registers and creates a business → invites a customer → customer registers or logs in and accepts → business appears in Customer Mode → owner creates and assigns an invoice → customer views it and chats → AI extracts a proposed commitment → owner reviews it → owner records a verified external payment → both dashboards show the correct balance.

## Contributing

- Branches: `main` (stable), `develop` (integration), `feature/<name>` (modules).
- Open a pull request for every change and have a teammate review it.
- A feature is done when it has validation, tests, verified authorization, integration shown and updated docs.
- Never commit secrets or production credentials.

## License

Add your license here.
=======
This software is all about B2B conversation and helps them to communicate efficiently. The ai we created is assisting by filling the gaps in conversation and stores the commitment for any payments. 
>>>>>>> b9ede51d23f613c3ba941dc9554f392852399a5e
