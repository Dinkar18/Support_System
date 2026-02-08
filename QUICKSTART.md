# Quick Start Guide

## Step 1: Database Setup

```bash
# Install PostgreSQL (if not installed)
# macOS
brew install postgresql@14
brew services start postgresql@14

# Create database
createdb support_system

# Or using psql
psql postgres
CREATE DATABASE support_system;
\q
```

## Step 2: Configure Application

The default configuration in `application.yml` uses:
- Database: `support_system`
- Username: `postgres`
- Password: `postgres`
- Port: `5432`

If your PostgreSQL setup is different, update `src/main/resources/application.yml`

## Step 3: Build & Run

```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run
```

Application starts on: `http://localhost:8080`

## Step 4: Test APIs

### Option 1: Using Swagger UI
Open browser: `http://localhost:8080/swagger-ui.html`

### Option 2: Using Postman
1. Import `postman_collection.json`
2. Follow the requests in order

### Option 3: Using cURL

**1. Signup**
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "password123",
    "fullName": "Admin User",
    "organizationName": "My Company"
  }'
```

**2. Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "password123"
  }'
```

Copy the `accessToken` from response.

**3. Create Ticket**
```bash
curl -X POST http://localhost:8080/api/v1/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "title": "Login issue",
    "description": "Cannot login to the system",
    "priority": "HIGH"
  }'
```

**4. Get All Tickets**
```bash
curl -X GET "http://localhost:8080/api/v1/tickets?page=0&size=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**5. Add Message to Ticket**
```bash
curl -X POST http://localhost:8080/api/v1/tickets/1/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "message": "Looking into this issue",
    "isInternal": false
  }'
```

## Troubleshooting

### Database Connection Error
- Ensure PostgreSQL is running: `brew services list`
- Check database exists: `psql -l`
- Verify credentials in `application.yml`

### Port Already in Use
Change port in `application.yml`:
```yaml
server:
  port: 8081
```

### JWT Secret Warning
For production, generate a secure secret:
```bash
openssl rand -base64 64
```
Update in `application.yml`:
```yaml
jwt:
  secret: your-generated-secret-here
```

## Next Steps

1. Create an AGENT user (change role in database or add signup endpoint for agents)
2. Test ticket assignment
3. Test status transitions
4. Explore SLA deadlines in `sla_config` table

## Database Inspection

```bash
# Connect to database
psql support_system

# View tables
\dt

# View users
SELECT id, email, full_name, role FROM users;

# View tickets
SELECT id, title, status, priority FROM tickets;

# View SLA config
SELECT * FROM sla_config;

# Exit
\q
```

## Development Tips

- Use Swagger UI for interactive API testing
- Check application logs for debugging
- Database migrations are in `src/main/resources/db/migration`
- All passwords are encrypted with BCrypt
- JWT tokens expire after 1 hour (configurable)

## Project Structure Overview

```
src/main/java/com/dk/supportsystem/
├── config/              # Security & JPA configuration
├── controller/          # REST API endpoints
├── dto/                # Request/Response objects
│   ├── request/        # API request DTOs
│   └── response/       # API response DTOs
├── entity/             # Database entities
├── enums/              # Enums (Status, Priority, Role)
├── exception/          # Exception handling
├── repository/         # Database access
├── security/           # JWT & authentication
└── service/            # Business logic
```

Happy coding! 🚀
