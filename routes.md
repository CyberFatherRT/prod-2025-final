# Users (/users)

### POST /login

Request:
```json
{
    "email": "string",
    "password": "string"
}
```

Response:
```json
{
    "token": "jwt"
}

/// jwt
{
    "exp": "unix_timestamp",
    "id": "uuid",
    "role": "enum('admin', 'students', 'guest', 'verified_guest')"
}
```

---

### POST /register

Request:
```json
{
    "name": "string",
    "surname": "string",
    "email": "string",
    "password": "string",
    "company_id": "uuid"
}
```

Response:
```json
{
    "token": "jwt"
}
jwt - {
    "exp": "unix_timestamp",
    "id": "uuid",
    "role": "enum('admin', 'students', 'guest', 'verified_guest')"
}
```

---

### GET /profile (auth)
Response:
```json
{
    "username": "string",
    "email": "string",
    "avatar": "string",
    "role": "enum('admin', 'students', 'guest', 'verified_guest')",
    "pending_verification": "boolean",
    "company_id": "uuid"
}
```

### PATCH /profile

### DELETE /

### POST /upload_document

# Booking (/booking)

### GET /list_booking_by_space/{space_id}
For admin, add sensitive info (like user_id who booked)

### GET /list_booking_by_item/{item_id}
For admin, add sensitive info (like user_id who booked)

### GET /get_booking_by_id/{booking_id}

# Admin (/admin)

### POST /verify_guest (auth)
Request:
```json
{
    "user_id": "uuid"
}
```

Response: \
Status Code - 200, 404, 400

### PATCH /user/{user_id}

### DELETE /user/{user_id}

### POST /verify_qr
