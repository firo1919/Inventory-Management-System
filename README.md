<div align="center">

# Inventory Management System

A Spring Boot backend for inventory, sales, and restock workflows with PostgreSQL, S3-compatible storage, and email notifications.

</div>

## Overview

This repository contains the backend for an inventory management system. It provides REST APIs for authentication, product and category management, employee and profile management, sales and restock operations, and file uploads.

## Features

- Auth and profile management with OAuth2 resource server support
- Product and category lifecycle, sales, and restock flows
- Employee management and admin endpoints
- Email notifications and scheduled reporting
- S3-compatible storage integration
- OpenAPI UI in dev at `/docs`
- Flyway-based database migrations

## Tech stack

- Java 25, Spring Boot 4
- PostgreSQL, Flyway, JPA
- Spring Security (OAuth2 resource server)
- SpringDoc OpenAPI UI
- AWS S3 SDK (compatible with local RustFS)

> [!NOTE]
> This repo currently contains the backend only. There is no frontend in this workspace.

## Quickstart (development)

### 1) Start infrastructure services

From [Backend](Backend), start the dev services (PostgreSQL, MailHog, RustFS, and SQL Studio):

```bash
cd Backend
cp example.env .env
docker compose -f docker-compose-dev.yaml up -d
```

Services exposed locally:

- PostgreSQL: `localhost:5432`
- MailHog UI: `http://localhost:8025`
- RustFS S3 API: `http://localhost:9000`
- RustFS Console: `http://localhost:9001`
- SQL Studio: `http://localhost:3030`

### 2) Run the API

```bash
cd Backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API runs on `http://localhost:8080`.

> [!TIP]
> OpenAPI UI is available at `http://localhost:8080/docs` when the `dev` profile is active.

## Configuration

Configuration is profile-based and lives under [Backend/src/main/resources](Backend/src/main/resources).

### Dev profile

Defaults are set in [Backend/src/main/resources/application-dev.properties](Backend/src/main/resources/application-dev.properties).

Key dev defaults include:

- PostgreSQL URL: `jdbc:postgresql://localhost:5432/inventory`
- MailHog SMTP: `localhost:1025`
- RustFS endpoint: `http://localhost:9000`

### Prod profile

Production configuration uses environment variables set via Docker or a process manager. See [Backend/docker-compose.yaml](Backend/docker-compose.yaml) for the full list.

Required variables include:

| Name                                    | Purpose              |
| --------------------------------------- | -------------------- |
| `SPRING_DATASOURCE_URL`                 | PostgreSQL JDBC URL  |
| `SPRING_DATASOURCE_USERNAME`            | Database username    |
| `SPRING_DATASOURCE_PASSWORD`            | Database password    |
| `AUTH_SECRET`                           | JWT signing secret   |
| `CORS_ORIGINS`                          | Allowed CORS origins |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY`     | S3 credentials       |
| `AWS_REGION` / `AWS_S3_ENDPOINT`        | S3 configuration     |
| `S3_BUCKET_NAME`                        | Default bucket name  |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` | SMTP configuration   |

## Project structure

```text
Inventory-Management-System/
	Backend/
		src/main/java/com/firomsa/inventory/
			v1/controller/   # REST controllers
			v1/service/      # Business logic
			repository/      # JPA repositories
			model/           # Entities and domain models
			security/        # Security configuration and JWT utilities
		src/main/resources/
			application.properties
			application-dev.properties
			application-prod.properties
```

## Testing

```bash
cd Backend
./mvnw test
```

Coverage reports are generated via JaCoCo after tests complete.

## API modules (v1)

Endpoints are grouped under controllers in [Backend/src/main/java/com/firomsa/inventory/v1/controller](Backend/src/main/java/com/firomsa/inventory/v1/controller):

- Auth, profile, and admin
- Products, categories, sales, and restock
- Employees and uploads
