<div align="center">

# 🚀 Next.js Advanced Starter Kit

### Scalable Architecture | Strict Separation of Concerns | Production-Ready

<p>
  <img src="https://img.shields.io/badge/Next.js-14+-000000?style=flat&logo=next.js&logoColor=white" />
  <img src="https://img.shields.io/badge/TypeScript-5.0+-3178C6?style=flat&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/Redux_Toolkit-Query-764ABC?style=flat&logo=redux&logoColor=white" />
  <img src="https://img.shields.io/badge/Zod-Validation-3E67B1?style=flat&logo=zod&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat" />
</p>

<p align="center">
  A highly opinionated, clean-architecture foundation for modern web applications. 
  <br>
  Designed for teams who value <b>maintainability</b> over shortcuts.
</p>

[Getting Started](#-getting-started) • [Architecture](#-architectural-decisions) • [Project Structure](#-project-structure)

</div>

---

## 📖 Overview

This is not just another "Hello World" template. This repository implements a **real-world engineering standard** for Next.js applications. 

It solves the common problems of spaghetti code, scattered API calls, and loose typing by enforcing a **Feature-Based Modular Architecture**. It is pre-configured with **RTK Query** for data fetching, **Zod** for runtime validation, and a strict separation between UI, Logic, and Data layers.

---

## 🧠 Architectural Decisions

We built this kit based on principles that ensure the codebase remains healthy as it scales from 10 to 100+ components.

| Decision | Why is this necessary? |
| :--- | :--- |
| **Feature Modules** (`/modules`) | Instead of organizing by file type (e.g., all components in one folder), we organize by **domain** (e.g., `modules/auth`). This keeps related logic co-located and easy to extract or refactor. |
| **Centralized Endpoints** | Hardcoding API URLs in components leads to "magic strings" and refactoring nightmares. We use a single registry (`constants/endpoints.ts`) to manage all API routes. |
| **Zod as Single Source of Truth** | We don't manually write TypeScript interfaces for API responses. We define **Zod Schemas** and infer TypeScript types from them. This ensures runtime validation matches compile-time types. |
| **Service Layer (RTK Query)** | The UI should never trigger `fetch()` directly. Data fetching, caching, deduplication, and loading states are handled entirely by Redux Toolkit Query, keeping components pure. |
| **Mocked Auth (Demo Mode)** | To allow immediate UI development without a backend, the auth system is fully mocked. It simulates latency, token storage, and protection logic, making it "Backend-Ready" when you need it. |

---

## 📂 Project Structure

```bash
src/
├── app/                      # Next.js App Router (Routing only)
│   ├── (public)/             # Routes accessible without login
│   ├── (protected)/          # Routes requiring authentication
│   └── layout.tsx
│
├── modules/                  # 🟢 THE CORE: Feature-based architecture
│   └── auth/                 # Example Module
│       ├── components/       # UI components specific to Auth
│       ├── hooks/            # Business logic for Auth
│       ├── schemas/          # Zod validation schemas
│       ├── types/            # TS types inferred from Zod
│       └── index.ts          # Public API for this module
│
├── services/                 # API Layer (RTK Query)
│   ├── baseApi.ts            # Network configuration (Interceptors, etc.)
│   └── auth.endpoints.ts     # Endpoint definitions
│
├── store/                    # Redux Store Configuration
├── components/               # Shared/Global UI Components (Buttons, Inputs)
├── hooks/                    # Global reusable hooks
├── utils/                    # Helpers (Token management, Formatting)
└── constants/                # App-wide constants (Routes, API paths)

```
## Centralized API Management

All backend routes are defined in one place. This allows for easy versioning and refactoring without hunting through component files.

```js
// src/constants/endpoints.ts
export const endpoints = {
  auth: {
    login: '/auth/login',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
  },
  // Add other domains here...
}
```

## 🔐 Authentication (Demo Mode)

**There is no backend connected by default.**
For development and demonstration purposes, the authentication flow is **mocked** at the hook level.

- **Login Logic:** Simulates API latency and validates against hardcoded demo credentials.
- **Storage:** Stores a fake token in `localStorage` to persist the session.
- **Protection:** Routes in `(protected)` check for this token and redirect if missing.

### 🔑 Demo Credentials
> **Email:** `demo@demo.com`  
> **Password:** `password123`

*When you are ready for a real backend, simply update the `useAuth` hook to call your actual API endpoint.*

---

## 🚀 Getting Started

### 1. Installation
Clone the repository and install dependencies:

```bash
git clone https://github.com/henabakos/nextjs-advanced-starter.git
cd nextjs-advanced-starter
npm install
```
### 2. Run Development Server
```bash
npm run dev
```
Then open `http://localhost:3000` to see the app.

# 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Next.js (App Router) |
| Language | TypeScript |
| State Management | Redux Toolkit |
| Data Fetching | RTK Query |
| Validation | Zod |
| Styling | Tailwind CSS (Recommended) |

## 📌 Intended Use

This starter kit is ideal for:

✓ **SaaS dashboards** – Build scalable, interactive dashboard interfaces  
✓ **Admin panels** – Create powerful admin interfaces with robust tooling  
✓ **Internal tools** – Develop internal applications with enterprise-grade architecture  
✓ **Learning real-world frontend architecture** – Study production-ready patterns and practices  
✓ **Teams that care about clean code** – Maintain consistent code quality across projects

## 📄 License
This project is open source and available under the [MIT License](LICENSE).

---

<div align="center">
  <p>Built with ❤️ by Hena</p>
  <p><strong>Code less, Architect more.</strong></p>
</div>