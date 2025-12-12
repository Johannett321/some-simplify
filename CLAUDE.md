# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SOMESimplify is a full-stack multi-tenant application with a Spring Boot backend and React frontend. The project follows an API-first development approach using OpenAPI specifications as the single source of truth.

## Development Commands

### Backend (Spring Boot)
```bash
# Navigate to backend/impl directory first
cd backend/impl

# Build all modules
mvn clean install

# Run the application (from impl module)
mvn spring-boot:run

# Run tests
mvn test
```

### Frontend (React + Vite)
```bash
# Navigate to frontend directory first
cd frontend

# Install dependencies
npm install

# Generate TypeScript API client from OpenAPI spec
npm run generate:api

# Start development server (auto-generates API client via prestart hook)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter
npm run lint
```

### Database
```bash
# Start PostgreSQL container
docker-compose up -d

# Stop PostgreSQL container
docker-compose down
```

## Architecture

### Monorepo Structure

```
/
├── backend/           # Spring Boot multi-module Maven project
│   ├── api/          # OpenAPI spec and generated interfaces
│   └── impl/         # Implementation with business logic
├── frontend/         # React + Vite application
└── docker-compose.yml
```

### API-First Development

The OpenAPI specification (`backend/api/src/main/resources/api.yaml`) is the contract between frontend and backend:

- **Backend**: Generates Java interfaces and DTOs that controllers implement
- **Frontend**: Generates TypeScript Axios client for API calls
- **Workflow**: When adding endpoints, update the OpenAPI spec first, then regenerate code on both sides

The spec is split into separate files:
- Main spec: `api.yaml`
- Path definitions: `paths/auth/*.yaml`, `paths/user/*.yaml`, `paths/tenant/*.yaml`
- Schema definitions: `schemas/user/*.yaml`, `schemas/tenant/*.yaml`

After updating the OpenAPI spec:
1. Backend: Run `mvn clean install` to regenerate interfaces
2. Frontend: Run `npm run generate:api` to regenerate TypeScript client

### Backend Architecture (Spring Boot 3.1.2 + Java 21)

**Multi-Module Maven Project:**
- Parent POM coordinates two modules: `api` and `impl`
- API module generates interfaces from OpenAPI spec
- Impl module contains all business logic and depends on API module

**Key Technologies:**
- Spring Data JPA with PostgreSQL
- Spring Security with JWT authentication
- Multi-tenancy support (discriminator-based)
- MapStruct for DTO-entity mapping
- Lombok for boilerplate reduction

**Package Structure:**
```
com.somesimplify.somesimplify/
├── config/          # Security, app config, email config
├── filter/          # JwtAuthenticationFilter
├── multitenancy/    # Tenant context, interceptor, resolver
├── model/           # JPA entities (User, Tenant, etc.)
├── repository/      # Spring Data repositories
├── service/         # Business logic
├── rest/            # API implementations (*ApiImpl)
├── mapper/          # MapStruct mappers
└── exception/       # Custom exceptions
```

**Authentication:**
- JWT tokens stored in HttpOnly cookies
- `JwtAuthenticationFilter` validates tokens on each request
- Supports username/password and OAuth2 (Google, Facebook)
- BCrypt password hashing

**Multi-Tenancy:**
- Header-based: `X-Tenant-ID` header identifies tenant
- `TenantInterceptor` validates user access to requested tenant
- `TenantContext` stores tenant ID in ThreadLocal
- Hibernate `TenantIdentifierResolver` filters queries by tenant
- Many-to-many: Users can belong to multiple tenants

### Frontend Architecture (React 19 + TypeScript)

**Technology Stack:**
- React 19.1.0 with TypeScript 5.8.3
- Vite 7.0.4 for build tooling
- React Router 7.7.0 for routing
- Tailwind CSS 3.4.13 + ShadCN UI components
- React Hook Form + Zod for form validation
- Axios for API calls (generated client)

**State Management:**
- React Context API (no Redux/Zustand)
- `UserProvider` manages authenticated user state
- `TenantProvider` manages current tenant selection
- Custom hooks: `useUser()`, `useTenant()`

**Project Structure:**
```
frontend/src/
├── api/             # Generated OpenAPI client (AuthApi, UserApi, TenantApi)
├── components/      # React components
│   ├── auth/       # Login, register, tenant picker
│   └── ui/         # ShadCN UI components
├── providers/       # UserProvider, TenantProvider
├── hooks/           # useUser, useTenant
├── layouts/         # MainLayout with Sidebar
├── utils/           # Utility functions
└── config/          # API configuration
```

**API Integration:**
- Generated TypeScript Axios client from OpenAPI spec
- Base URL: `http://localhost:8080`
- Cookie-based authentication (`axios.defaults.withCredentials = true`)
- Usage: `new UserApi(apiConfig).getUser()`

## Important Patterns

### Naming Conventions

**Backend:**
- Entities: `User`, `Tenant` (no suffix)
- DTOs: `UserTO`, `TenantTO` (Transfer Object suffix)
- Commands: `CreateUserCommand`, `LoginUserCommand`
- Repositories: `UserRepository`
- Services: `UserService`
- API Implementations: `AuthApiImpl`, `UserApiImpl` (implement generated interfaces)
- Mappers: `UserMapper`, `TenantMapper` (MapStruct interfaces)

**Frontend:**
- Components: PascalCase (e.g., `LoginPage.tsx`)
- Hooks: camelCase with `use` prefix (e.g., `useUser.ts`)
- Providers: `UserProvider`, `TenantProvider`
- Contexts: `UserContext`, `TenantContext`

### Code Generation

**Lombok** (backend):
- `@Data` on entities for getters/setters/toString/equals/hashCode
- `@RequiredArgsConstructor` for dependency injection
- `@Slf4j` for logging
- Annotation processing order: Lombok → MapStruct binding → MapStruct

**MapStruct** (backend):
- Interface-based DTO ↔ Entity mapping
- Compile-time code generation
- Example: `UserMapper.INSTANCE.toTO(user)`

**OpenAPI Generator**:
- Backend: Generates Java interfaces and model classes
- Frontend: Generates TypeScript Axios client
- Both regenerate automatically during build

### Multi-Tenant Request Flow

1. Frontend sends request with `X-Tenant-ID` header
2. `TenantInterceptor` validates user has access to tenant
3. Tenant ID stored in `TenantContext` (ThreadLocal)
4. Hibernate `TenantIdentifierResolver` applies tenant filter to queries
5. ThreadLocal cleared after request completes

### Authentication Flow

1. User submits credentials to `/auth/login`
2. Backend validates and generates JWT
3. JWT set in HttpOnly cookie (not accessible to JavaScript)
4. Frontend includes cookie automatically on subsequent requests
5. `JwtAuthenticationFilter` validates JWT and loads user into SecurityContext
6. Logout expires the cookie

## Database

**PostgreSQL Configuration:**
- Database: `somesimplifydb`
- User: `somesimplifyuser`
- Port: 5432
- Connection details in `backend/impl/src/main/resources/application-dev.properties`
- DDL: `spring.jpa.hibernate.ddl-auto=update` (auto-creates tables)

**Multi-Tenancy:**
- Discriminator-based (single database with tenant_id column)
- Not schema-based or database-based separation

## Development Workflow

1. **Initial Setup:**
   ```bash
   ./init.sh  # Customize template (renames packages, updates references)
   ```

2. **Daily Development:**
   ```bash
   # Terminal 1: Start database
   docker-compose up -d

   # Terminal 2: Start backend
   cd backend/impl && mvn spring-boot:run

   # Terminal 3: Start frontend
   cd frontend && npm run dev
   ```

3. **Adding New Endpoints:**
   - Update OpenAPI spec: `backend/api/src/main/resources/api.yaml`
   - Rebuild backend: `mvn clean install`
   - Regenerate frontend client: `npm run generate:api`
   - Implement controller in `backend/impl/src/.../rest/*ApiImpl.java`
   - Use generated API in frontend: `new SomeApi(apiConfig).someMethod()`

## Environment Configuration

**Backend** (`application-dev.properties`):
- JWT secret key
- Database credentials
- CORS allowed origins (default: `http://localhost:5173`)
- AWS S3 configuration (optional)
- Stripe API key (optional)
- Email SMTP settings (optional)

**Frontend**:
- API base URL in `src/config/ApiConfig.ts`
- Environment detection in `src/utils/EnvironmentManager.ts`

## Testing

**Backend:**
- JUnit tests in `src/test/java`
- Spring Security Test support included
- Run: `mvn test`

**Frontend:**
- ESLint for code quality
- Run: `npm run lint`

## Key Dependencies

**Backend:**
- Spring Boot 3.1.2
- Spring Security + OAuth2
- PostgreSQL 42.7.5
- JWT (jjwt 0.11.5)
- MapStruct 1.6.3
- Lombok 1.18.30
- Stripe 26.1.0
- AWS S3 SDK 2.32.9
- Apache PDFBox 3.0.5

**Frontend:**
- React 19.1.0
- Vite 7.0.4
- TypeScript 5.8.3
- Tailwind CSS 3.4.13
- React Router 7.7.0
- React Hook Form 7.62.0
- Zod 4.1.5
- Axios 1.11.0
- ShadCN UI (Radix UI components)
- Framer Motion 12.23.12

## Notes

- Application supports Norwegian language in some UI text (e.g., route `/opprett-foretak` for "create tenant")
- Cookie-based authentication requires `withCredentials: true` on Axios requests
- Frontend dev server runs on port 5173, backend on port 8080
- CORS is configured to allow requests between these ports during development
