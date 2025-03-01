# Users (/users)

### DELETE /

### POST /upload_document

# Booking (/booking)

### GET /list_booking_by_space/{space_id}
For admin, add sensitive info (like user_id who booked)

### GET /list_booking_by_item/{item_id}
For admin, add sensitive info (like user_id who booked)

### GET /get_booking_by_id/{booking_id}

### GET /get_qr_data

### GET /verify_qr_data

```json
{
  "booking-
}

# Admin (/admin)

### POST /verify_guest/{user_id} (auth)
Request:
path parameters

Response: \
Status Code - 200, 404, 400

### PATCH /user/{user_id}

### DELETE /user/{user_id}

### POST /verify_qr
