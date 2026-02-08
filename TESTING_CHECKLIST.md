# Testing Checklist ✅

Use this checklist to verify Phase 1 is working correctly.

## Pre-requisites
- [ ] PostgreSQL installed and running
- [ ] Database `support_system` created
- [ ] Java 25 installed
- [ ] Maven installed

## Build & Run
- [ ] `mvn clean install` completes successfully
- [ ] `mvn spring-boot:run` starts without errors
- [ ] Application runs on http://localhost:8080
- [ ] Swagger UI accessible at http://localhost:8080/swagger-ui.html

## Database Migrations
- [ ] Check Flyway migrations ran successfully (check logs)
- [ ] Verify tables created:
  ```sql
  psql support_system
  \dt
  -- Should show: organizations, users, tickets, ticket_messages, sla_config, flyway_schema_history
  ```

## API Testing - Authentication

### 1. Signup (First User - Admin)
- [ ] POST `/api/v1/auth/signup`
  ```json
  {
    "email": "admin@company.com",
    "password": "password123",
    "fullName": "Admin User",
    "organizationName": "My Company"
  }
  ```
- [ ] Response contains `accessToken` and `refreshToken`
- [ ] User created in database with role ADMIN
- [ ] Organization created in database

### 2. Signup (Second User - Agent)
- [ ] POST `/api/v1/auth/signup`
  ```json
  {
    "email": "agent@company.com",
    "password": "password123",
    "fullName": "Agent User",
    "organizationName": "My Company"
  }
  ```
- [ ] User created with same organization
- [ ] Manually update role to AGENT in database:
  ```sql
  UPDATE users SET role = 'AGENT' WHERE email = 'agent@company.com';
  ```

### 3. Login
- [ ] POST `/api/v1/auth/login`
  ```json
  {
    "email": "admin@company.com",
    "password": "password123"
  }
  ```
- [ ] Response contains valid tokens
- [ ] Copy `accessToken` for next requests

### 4. Invalid Login
- [ ] Try wrong password - should get 400 error
- [ ] Try non-existent email - should get 400 error

## API Testing - Tickets

### 5. Create Ticket
- [ ] POST `/api/v1/tickets` (with Authorization header)
  ```json
  {
    "title": "Cannot login to dashboard",
    "description": "Getting 404 error when trying to access dashboard",
    "priority": "HIGH"
  }
  ```
- [ ] Ticket created with status OPEN
- [ ] SLA config created automatically
- [ ] Check database:
  ```sql
  SELECT * FROM tickets;
  SELECT * FROM sla_config;
  ```

### 6. Create Multiple Tickets
- [ ] Create ticket with priority URGENT
- [ ] Create ticket with priority MEDIUM
- [ ] Create ticket with priority LOW
- [ ] Verify different SLA deadlines

### 7. Get All Tickets
- [ ] GET `/api/v1/tickets?page=0&size=10`
- [ ] Returns paginated list
- [ ] All tickets belong to user's organization

### 8. Get Ticket by ID
- [ ] GET `/api/v1/tickets/1`
- [ ] Returns ticket details
- [ ] Includes creator info

### 9. Filter Tickets
- [ ] GET `/api/v1/tickets?status=OPEN`
- [ ] Returns only OPEN tickets
- [ ] GET `/api/v1/tickets?assignedTo=2`
- [ ] Returns tickets assigned to agent

### 10. Assign Ticket
- [ ] PUT `/api/v1/tickets/1/assign`
  ```json
  {
    "agentId": 2
  }
  ```
- [ ] Ticket assigned to agent
- [ ] Status changed to IN_PROGRESS (if was OPEN)

### 11. Update Status
- [ ] PUT `/api/v1/tickets/1/status`
  ```json
  {
    "status": "IN_PROGRESS"
  }
  ```
- [ ] Status updated successfully
- [ ] Try updating to RESOLVED - check `resolvedAt` timestamp
- [ ] Try updating to CLOSED - check `closedAt` timestamp

### 12. Status Validation
- [ ] Try updating CLOSED ticket - should fail
- [ ] Verify proper error message

## API Testing - Messages

### 13. Add Message
- [ ] POST `/api/v1/tickets/1/messages`
  ```json
  {
    "message": "Looking into this issue now",
    "isInternal": false
  }
  ```
- [ ] Message created
- [ ] SLA first response marked as met

### 14. Add Internal Message
- [ ] POST `/api/v1/tickets/1/messages`
  ```json
  {
    "message": "This is an internal note",
    "isInternal": true
  }
  ```
- [ ] Internal message created

### 15. Get Messages
- [ ] GET `/api/v1/tickets/1/messages`
- [ ] Returns all messages in chronological order
- [ ] Includes user info for each message

## Security Testing

### 16. Authentication Required
- [ ] Try accessing `/api/v1/tickets` without token - should get 401
- [ ] Try with invalid token - should get 401
- [ ] Try with expired token - should get 401

### 17. Organization Isolation
- [ ] Create second organization (signup with different org name)
- [ ] Login as user from org 2
- [ ] Try accessing tickets from org 1 - should fail
- [ ] Verify users can only see their org's data

### 18. Role-Based Access
- [ ] Try assigning ticket to non-AGENT user - should fail
- [ ] Verify proper error message

## Validation Testing

### 19. Invalid Input
- [ ] Try creating ticket without title - should get validation error
- [ ] Try signup with invalid email - should get validation error
- [ ] Try signup with short password - should get validation error
- [ ] Verify error response format

## Database Verification

### 20. Check Data Integrity
```sql
-- Connect to database
psql support_system

-- Check organizations
SELECT * FROM organizations;

-- Check users
SELECT id, email, full_name, role, organization_id FROM users;

-- Check tickets
SELECT id, title, status, priority, created_by, assigned_to FROM tickets;

-- Check messages
SELECT id, ticket_id, message, is_internal FROM ticket_messages;

-- Check SLA
SELECT ticket_id, first_response_deadline, resolution_deadline, 
       first_response_met, resolution_met FROM sla_config;

-- Check audit timestamps
SELECT id, title, created_at, updated_at FROM tickets;
```

### 21. Check Indexes
```sql
\d tickets
\d users
-- Verify indexes exist
```

## Performance Testing

### 22. Pagination
- [ ] Create 20+ tickets
- [ ] Test pagination: `?page=0&size=5`
- [ ] Test pagination: `?page=1&size=5`
- [ ] Verify correct results

### 23. Sorting
- [ ] Test sort by created date: `?sortBy=createdAt&sortDir=DESC`
- [ ] Test sort by priority: `?sortBy=priority&sortDir=ASC`

## Documentation Testing

### 24. Swagger UI
- [ ] Open http://localhost:8080/swagger-ui.html
- [ ] All endpoints visible
- [ ] Try executing requests from Swagger
- [ ] Verify request/response schemas

### 25. Postman Collection
- [ ] Import `postman_collection.json`
- [ ] Set `accessToken` variable
- [ ] Run all requests in sequence
- [ ] Verify all pass

## Error Handling

### 26. Global Exception Handler
- [ ] Try accessing non-existent ticket - should get 404
- [ ] Try invalid request - should get 400
- [ ] Verify error response format:
  ```json
  {
    "timestamp": "...",
    "status": 404,
    "error": "Not Found",
    "message": "Ticket not found",
    "path": "/api/v1/tickets/999"
  }
  ```

## Final Verification

### 27. Complete Flow
- [ ] Signup as admin
- [ ] Create 3 tickets
- [ ] Signup as agent (update role in DB)
- [ ] Login as admin
- [ ] Assign tickets to agent
- [ ] Add messages to tickets
- [ ] Update ticket status to RESOLVED
- [ ] Verify SLA tracking
- [ ] Check all data in database

### 28. Code Quality
- [ ] No compilation errors
- [ ] No warnings in logs
- [ ] Clean code structure
- [ ] Proper naming conventions
- [ ] Comments where needed

## Interview Preparation

### 29. Can You Explain?
- [ ] Why use DTOs instead of entities?
- [ ] How does JWT authentication work?
- [ ] Why Flyway for migrations?
- [ ] How is multi-tenancy implemented?
- [ ] What's the purpose of BaseEntity?
- [ ] How does SLA calculation work?
- [ ] Why separate request/response DTOs?
- [ ] How is security configured?

### 30. Demo Ready
- [ ] Can run application from scratch
- [ ] Can explain database schema
- [ ] Can walk through API flow
- [ ] Can show Swagger documentation
- [ ] Can explain design decisions

---

## Success Criteria

✅ All checkboxes above are checked
✅ Application runs without errors
✅ All APIs work as expected
✅ Database schema is correct
✅ Security is working
✅ Documentation is complete
✅ Ready for Phase 2

## If Something Fails

1. Check application logs
2. Verify database connection
3. Check Flyway migration status
4. Verify JWT secret is set
5. Check PostgreSQL is running
6. Review error messages carefully

## Next Steps After Verification

1. Commit code to Git
2. Push to GitHub
3. Add project to resume
4. Prepare demo for interviews
5. Start Phase 2 planning

---

**Congratulations!** 🎉 You've completed Phase 1 of a production-grade support system!
