# 📦 Inventory Management System - Frontend

This is the Next.js frontend application for the **Inventory Management System**. It provides a modern, responsive, and secure dashboard for administrators and employees to manage products, categories, stock levels, and audit logs.

## 🚀 Key Features

- **🔐 Robust Authentication**: Secure user login and registration powered by **Better Auth** and Spring Security, featuring:
  - Email/Password login with JWT access & refresh tokens.
  - Multi-Factor/OTP verification for administrator signup (integrated with Mailhog for local development).
  - Role-based views & route protection (Admin vs. Employee).
- **📊 Business Dashboard**: Real-time summary metrics including:
  - Total inventory valuation.
  - Low-stock counts and notification alerts.
  - Quick access to inventory analytics.
- **🛍️ Catalog Management**: Fully searchable and paginated tables for:
  - **Products**: CRUD operations, SKU tracking, stock replenishment, cost/selling price tracking, and product image uploads.
  - **Categories**: Organize products dynamically.
- **📝 System Audit Trail**: View real-time activity and operation logs tracked across the system.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Framework** | Next.js 14+ (App Router) |
| **Language** | TypeScript |
| **Styling** | Tailwind CSS |
| **Authentication** | Better Auth Client |
| **API Client** | Axios (configured with interceptors for JWT auth headers) |
| **State Management** | Redux Toolkit & React Context |

---

## 📂 Directory Structure

```bash
src/
├── app/                  # Next.js App Router Pages & Routing
│   ├── (public)/         # Public pages (Login, Register)
│   ├── (protected)/      # Authenticated dashboard, products, audit log pages
│   └── layout.tsx
├── components/           # Global reusable UI Components (cards, tables, modals)
├── constants/            # Configuration constants and endpoint paths
├── hooks/                # Custom React hooks (useAuth, useProducts, etc.)
├── services/             # Core service layers & API calls
└── utils/                # Helpers (token storage, date formatters)
```

---

## ⚙️ Getting Started

### 1. Prerequisites
- **Node.js** v18+ or v20+
- **Inventory Management Backend** running on `http://localhost:8080`

### 2. Configuration
Create a `.env` file in the root of the `Frontend/` folder:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
BETTER_AUTH_URL=http://localhost:3000
BETTER_AUTH_SECRET=f53603ddb1c5c52f094cf7d510b66df2fcd583804b8a00893f668c27108321d6
```

### 3. Installation
Install the project dependencies:
```bash
npm install
```

### 4. Running the Development Server
```bash
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) with your browser to view the application.