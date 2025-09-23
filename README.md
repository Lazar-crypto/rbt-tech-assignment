# RBT Ticket Booking System

A ticket booking system with Redis distributed locking and two-phase booking process.

## Key Features

- **Two-Phase Booking**: Reserve tickets → Complete payment
- **Redis Distributed Locks**: Prevents race conditions using sorted sets
- **External Authentication**: DummyJSON API integration with JWT
- **Mock Payment**: WireMock for payment simulation
- **Role-based Security**: ADMIN (create events) / USER (book tickets)

### How Redis Locking Works

- **Key**: `event:{eventId}:holds` 
- **Value**: Sorted Set (ZSET) with ticketIds as members
- **Score**: `now + 600` (expiration timestamp)
- **Cleanup**: Automatic removal of expired holds (score < now)

## 🚀 Quick Start

```bash
git clone <repository-url>
cd rbt-ticket-booking
docker-compose up -d
```

**Access**: http://localhost:8080 | **Docs**: http://localhost:8080/docs

## 👥 Test Users

**Admins** (create events): `emilys`, `michaelw`, `sophiab` (password: `{username}pass`)  
**Users** (book tickets): `miar`, `jamesd`, `emmaj` (password: `{username}pass`)

*Complete list in `docs/dummy-users.txt`*

## 🧪 Complete Test Flow

### 1. Login as Admin
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "sophiab",
  "password": "sophiabpass"
}
```

### 2. Create Test Event (Returns eventId: 13)
```http
POST http://localhost:8080/api/events
Content-Type: application/json
Authorization: Bearer <admin-token>

{
  "venueId": 2,
  "performerId": 2,
  "name": "Test Concert",
  "description": "Test concert description", 
  "startTime": "2025-12-20T15:00:00+02:00",
  "totalTickets": 1000,
  "maxPerRequest": 5,
  "ticketPrice": 50.00
}
```

### 3. Check Available Events
```http
GET http://localhost:8080/public/events?page=2
Content-Type: application/json
```
**Note**: `remainingTickets` = Available tickets in PostgreSQL - Currently held tickets in Redis

### 4. Login as User
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "miar", 
  "password": "miarpass"
}
```

### 5. Reserve 5 Tickets (Phase 1)
```http
POST http://localhost:8080/api/booking/reserve
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "eventId": 13,
  "quantity": 5
}
```
**Result**: 5 tickets held in Redis for 10 minutes. Check `/public/events` → `remainingTickets: 995`

### 6. Cancel Reservation (Optional)
```http
POST http://localhost:8080/api/booking/finalize
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "eventId": 13,
  "ticketIds": [13449914, 13449915, 13449916, 13449917, 13449918],
  "confirmed": false,
  "paymentRef": "will-succeed",
  "idempotencyKey": "cancel-key-123"
}
```
**Result**: Redis holds released. Check `/public/events` → `remainingTickets: 1000`

### 7. Reserve Again & Test Payment Failure
```http
POST http://localhost:8080/api/booking/reserve
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "eventId": 13,
  "quantity": 5
}

POST http://localhost:8080/api/booking/finalize
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "eventId": 13,
  "ticketIds": [new-ticket-ids],
  "confirmed": true,
  "paymentRef": "will-fail",
  "idempotencyKey": "fail-key-123"
}
```
**Result**: Payment fails, tickets remain held in Redis (can retry)

### 8. Complete Successful Payment
```http
POST http://localhost:8080/api/booking/finalize
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "eventId": 13,
  "ticketIds": [same-ticket-ids],
  "confirmed": true,
  "paymentRef": "will-succeed", 
  "idempotencyKey": "success-key-123"
}
```
**Result**: Tickets moved to BOOKED status, Redis holds released, idempotency key cached (5 min TTL)

### 9. Test Idempotency
```http
POST http://localhost:8080/api/booking/finalize
Content-Type: application/json
Authorization: Bearer <user-token>

# Same request as step 8
```
**Result**: Returns cached response without processing payment again

## 📋 Requirements Compliance

✅ **Event Management**: Admin creates events with capacity limits  
✅ **Ticket Booking**: Two-phase process with distributed locking  
✅ **Capacity Control**: Prevents overselling, respects max-per-request  
✅ **Security**: JWT auth with external DummyJSON validation  
✅ **Public API**: Open endpoint for event listing  
✅ **Dockerization**: Complete Docker Compose setup
