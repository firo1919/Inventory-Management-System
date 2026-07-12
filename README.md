# 📦 Inventory Management System

A production-ready, full-stack Inventory Management System featuring a high-performance **Spring Boot 4 backend** and a modern, responsive **Next.js 15+ frontend** dashboard. Manage your catalog, category hierarchies, transactions, users, role-based controls, audits, and uploads under a unified workflow.

---

## 🏗️ Architecture Overview

The system operates as a decoupled architecture:
* **Frontend**: Next.js App Router (using React, TypeScript, Tailwind CSS) communicating via a secure server-side Next.js route proxy (`/api/v1/*`) to the backend.
* **Backend**: Spring Boot 4 REST API utilizing PostgreSQL, JPA Hibernate, and Flyway migrations, configured behind Spring Security OAuth2 resource-server mechanisms.

```text
  [Browser Client]
         │
         ▼  (Port 3000)
┌────────────────────────────────┐
│       Next.js Frontend         │
├────────────────────────────────┤
│ /api/v1/* proxy handler        │
└────────────────┬───────────────┘
                 │
                 ▼  (Port 8080)
┌────────────────────────────────┐
│     Spring Boot Backend        │
├────────────────────────────────┤
│ Security, Database, S3 Uploads │
└────────────────────────────────┘
```

---

## 🚀 Key Features

### 🔐 Authentication & Security
* **Better Auth / Spring Security JWT Integration**: Secure session management using Access and Refresh tokens.
* **Multi-Factor Admin Signups**: Administrator signups require an OTP code sent via SMTP (monitored locally via Mailhog).
* **Role-Based Guards**: Hard separation between `ADMIN` and `EMPLOYEE` permissions on both server-side security rules and client-side view states.
* **Internal Proxying**: All client API requests go through Next.js route handlers rather than exposing Java backend endpoints directly to the browser.

### 🛍️ Core Operations
* **Product Catalog**: Paginated catalog lookup, low-stock warnings, threshold filters, multi-category tags, and object-key S3-compatible image association.
* **Advanced Selectors**: Uses a reusable custom **SearchableSelect** dropdown component with debounced server-side loading to easily handle massive item registries without freezing the UI.
* **Category Tree**: Nested categories to catalog items dynamically.
* **Sales Logging**: Record transactions, auto-suggest selling price, validate availability constraints, and view exact transactional data details.
* **Inbound Restocks**: Fast log interface for catalog inventory replenishment.

### 📝 System Audits & Management
* **System Audit Log**: Automatic tracking of creation, editing, status changes, and deletion transactions across the system with Correlation ID linkage.
* **Employee Directory**: Register staff members, assign credentials/roles, and deactivate or activate account access instantly.

---

## 🛠️ Technology Stack

| Domain | Backend | Frontend |
| :--- | :--- | :--- |
| **Framework** | Spring Boot 4.x / Spring Security | Next.js 15.x / React 19 / Turbopack |
| **Languages** | Java 25 | TypeScript |
| **Database & Migration** | PostgreSQL 16+, Flyway | - |
| **Cache & Session** | Spring Data Redis | Redux Toolkit, Better Auth |
| **Storage / Assets** | AWS S3 SDK (compatible with MinIO/RustFS) | Axios & Presigned PUT uploads |
| **Developer Tools** | OpenAPI (SpringDoc), Mailhog, Docker | Tailwind CSS, Lucide icons, Sonner |

---

## ⚙️ Getting Started & Setup

Follow these steps to spin up the local development environment.

### 1. Prerequisites
* **Node.js** v18+ or v20+
* **Java SDK** v25
* **Docker & Docker Compose**

### 2. Infrastructure Setup (Docker Compose)
First, spin up database, mailing, and S3 mock services.
From the workspace root directory:
```bash
cd Backend
cp example.env .env
docker compose -f docker-compose-dev.yaml up -d
```

#### Dev Services Exposed Locally:
* **PostgreSQL**: `localhost:5432` (Database: `inventory`)
* **Mailhog UI**: `http://localhost:8025` (Inbound dev signup OTP emails)
* **RustFS S3 API**: `http://localhost:9000` (File uploads)
* **RustFS Console**: `http://localhost:9001`
* **SQL Studio**: `http://localhost:3030`

---

### 3. Running the Backend
From the `Backend` directory:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
* **API Endpoint**: `http://localhost:8080`
* **Swagger OpenAPI Docs**: `http://localhost:8080/docs` (available under dev profile)

---

### 4. Running the Frontend
First, create an environment file. In `Frontend/.env`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
BETTER_AUTH_URL=http://localhost:3000
BETTER_AUTH_SECRET=f53603ddb1c5c52f094cf7d510b66df2fcd583804b8a00893f668c27108321d6
```

Then install dependencies and start the hot-reloading dev server from the `Frontend` directory:
```bash
cd ../Frontend
npm install
npm run dev
```
* **Development Client Portal**: `http://localhost:3000`

---

## 📂 Repository Structure

```text
Inventory-Management-System/
├── Backend/                 # Java Spring Boot 4 Backend APIs
│   ├── src/main/java/       # Controller, service, and security layers
│   ├── src/main/resources/  # Properties files, Flyway schema migrations
│   ├── Dockerfile           # Backend container setup
│   └── docker-compose.yaml  # Multi-container orchestration config
│
├── Frontend/                # Next.js 15 App Router Frontend
│   ├── app/                 # Next.js pages & proxy API routes
│   ├── components/          # Reusable component library (search inputs, modals, layout)
│   ├── hooks/               # Custom context, session handlers, and lifecycle hooks
│   ├── lib/                 # apiClient interceptor logic
│   └── public/              # Static frontend assets
```

---

## 🧪 Testing

To run backend unit and integration tests:
```bash
cd Backend
./mvnw test
```

To perform frontend linting and static checks:
```bash
cd Frontend
npm run lint
npx tsc --noEmit
```
