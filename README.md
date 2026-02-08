# Support System - Phase 1

A production-grade customer support ticketing system built with Spring Boot.

## Features Implemented (Phase 1)

✅ **Authentication & Authorization**
- JWT-based authentication (access + refresh tokens)
- Role-based access control (ADMIN, AGENT)
- Secure password encryption

✅ **Core Ticket Management**
- Create, read, update tickets
- Ticket assignment to agents
- Status transitions (OPEN → IN_PROGRESS → WAITING → RESOLVED → CLOSED)
- Priority levels (LOW, MEDIUM, HIGH, URGENT)

✅ **Ticket Messaging**
- Add comments to tickets
- Internal vs external messages
- Message history

✅ **SLA Configuration**
- Automatic SLA deadline calculation based on priority
- First response tracking
- Resolution time tracking

✅ **Multi-tenancy**
- Organization-based isolation
- Users can only access their organization's tickets

✅ **Production-Ready Features**
- Database migrations with Flyway
- Global exception handling
- Input validation
- Audit trails (created_at, updated_at)
- Pagination and filtering
- API documentation with Swagger

## Tech Stack

- **Backend**: Spring Boot 4.0.2
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven

## Prerequisites

- Java 25
- PostgreSQL 14+
- Maven 3.8+

## Setup Instructions

### 1. Database Setup

```bash
# Create database
createdb support_system

# Or using psql
psql -U postgres
CREATE DATABASE support_system;
```

### 2. Configure Application

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/support_system
    username: your_username
    password: your_password
```

### 3. Run Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/signup` - Register new user
- `POST /api/v1/auth/login` - Login

### Tickets
- `POST /api/v1/tickets` - Create ticket
- `GET /api/v1/tickets` - List tickets (with filters)
- `GET /api/v1/tickets/{id}` - Get ticket details
- `PUT /api/v1/tickets/{id}/assign` - Assign ticket to agent
- `PUT /api/v1/tickets/{id}/status` - Update ticket status
- `POST /api/v1/tickets/{id}/messages` - Add message
- `GET /api/v1/tickets/{id}/messages` - Get messages

## Testing with Postman

### 1. Signup
```json
POST /api/v1/auth/signup
{
  "email": "admin@company.com",
  "password": "password123",
  "fullName": "Admin User",
  "organizationName": "My Company"
}
```

### 2. Login
```json
POST /api/v1/auth/login
{
  "email": "admin@company.com",
  "password": "password123"
}
```

Copy the `accessToken` from response.

### 3. Create Ticket
```json
POST /api/v1/tickets
Headers: Authorization: Bearer <your_token>
{
  "title": "Login issue",
  "description": "Cannot login to the system",
  "priority": "HIGH"
}
```

### 4. List Tickets
```
GET /api/v1/tickets?page=0&size=10&status=OPEN
Headers: Authorization: Bearer <your_token>
```

### 5. Add Message
```json
POST /api/v1/tickets/1/messages
Headers: Authorization: Bearer <your_token>
{
  "message": "Looking into this issue",
  "isInternal": false
}
```

## Database Schema

### Tables
- `organizations` - Company/tenant data
- `users` - User accounts
- `tickets` - Support tickets
- `ticket_messages` - Comments on tickets
- `sla_config` - SLA tracking per ticket

## Project Structure

```
src/main/java/com/dk/supportsystem/
├── config/          # Security, JPA configuration
├── controller/      # REST controllers
├── dto/            # Request/Response DTOs
├── entity/         # JPA entities
├── enums/          # Enums (Status, Priority, Role)
├── exception/      # Custom exceptions & handler
├── repository/     # Data access layer
├── security/       # JWT & authentication
└── service/        # Business logic
```

## Next Steps (Phase 2)

- SLA breach detection with scheduled jobs
- Analytics dashboard
- Performance metrics
- Redis caching

## Interview Talking Points

✅ "Built with clean architecture and separation of concerns"
✅ "Implemented JWT authentication with role-based access"
✅ "Used Flyway for database version control"
✅ "DTOs prevent entity exposure and allow API evolution"
✅ "Multi-tenant architecture with organization isolation"
✅ "Production-ready with exception handling and validation"
✅ "RESTful API design with proper HTTP methods"
✅ "Audit trails for compliance and debugging"

## License

MIT
