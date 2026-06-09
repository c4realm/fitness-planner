# Arcade Fitness – API Testing Guide (Postman)

Base URL: `http://<YOUR_IP>:3000/api/`

All endpoints return a standard envelope: `{ "status": "success"|"error", "data": { ... }, "message": "..." }`

## 1. Health Check

Verify the server is running.

- **Method:** `GET`
- **URL:** `{{baseUrl}}/health`
- **Headers:** None
- **Body:** None
- **Expected response (200):**
  ```json
  { "status": "ok" }
  ```

---

## 2. Register

Create a new account.

- **Method:** `POST`
- **URL:** `{{baseUrl}}/auth/register`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "test@example.com",
    "password": "password123",
    "gender": "Male"
  }
  ```
- **Expected response (201):**
  ```json
  {
    "status": "success",
    "data": {
      "user": { "user_id": 1, "email": "test@example.com" },
      "token": "eyJhbGciOiJIUzI1NiIs..."
    }
  }
  ```

---

## 3. Login

Authenticate and receive a JWT token.

- **Method:** `POST`
- **URL:** `{{baseUrl}}/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "test@example.com",
    "password": "password123"
  }
  ```
- **Expected response (200):**
  ```json
  {
    "status": "success",
    "data": {
      "user": { "user_id": 1, "email": "test@example.com" },
      "token": "eyJhbGciOiJIUzI1NiIs..."
    }
  }
  ```

**Note:** Copy the `token` value — all subsequent requests require it in the `Authorization` header.

---

## 4. Authenticated Requests Setup

For every request below, add this header:

```
Authorization: Bearer <your-jwt-token>
```

In Postman, create a **Collection** and set this as a collection-level header, or use a **Variable** (`{{token}}`) to store the token from the login response.

---

## 5. User Profile

### Get Profile

- **Method:** `GET`
- **URL:** `{{baseUrl}}/user/profile`
- **Headers:** `Authorization: Bearer {{token}}`

### Update Profile

- **Method:** `PUT`
- **URL:** `{{baseUrl}}/user/profile`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "full_name": "John Doe",
    "age": 25,
    "gender": "Male",
    "fitness_level": "intermediate"
  }
  ```

---

## 6. Workouts

### List All Workouts

- **Method:** `GET`
- **URL:** `{{baseUrl}}/workouts`
- **Headers:** `Authorization: Bearer {{token}}`

### Get Workout by ID

- **Method:** `GET`
- **URL:** `{{baseUrl}}/workouts/1`
- **Headers:** `Authorization: Bearer {{token}}`

### Create Workout

- **Method:** `POST`
- **URL:** `{{baseUrl}}/workouts`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "name": "Upper Body Power",
    "description": "Chest, shoulders, and triceps",
    "duration_minutes": 45,
    "exercises": [
      { "exercise_id": 1, "sets": 3, "reps": 10 },
      { "exercise_id": 2, "sets": 3, "reps": 12 }
    ]
  }
  ```

### Update Workout

- **Method:** `PUT`
- **URL:** `{{baseUrl}}/workouts/1`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:** Same structure as create

### Delete Workout

- **Method:** `DELETE`
- **URL:** `{{baseUrl}}/workouts/1`
- **Headers:** `Authorization: Bearer {{token}}`

---

## 7. Exercises

### List All Exercises

- **Method:** `GET`
- **URL:** `{{baseUrl}}/exercises`
- **Headers:** `Authorization: Bearer {{token}}`

### Create Exercise

- **Method:** `POST`
- **URL:** `{{baseUrl}}/exercises`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "name": "Bench Press",
    "muscle_group": "Chest",
    "description": "Barbell bench press"
  }
  ```

---

## 8. Workout Sessions

### List All Sessions

- **Method:** `GET`
- **URL:** `{{baseUrl}}/sessions`
- **Headers:** `Authorization: Bearer {{token}}`

### Create Session

- **Method:** `POST`
- **URL:** `{{baseUrl}}/sessions`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "workout_id": 1,
    "status": "IN_PROGRESS",
    "start_timestamp": 1717000000000
  }
  ```

### Complete Session

- **Method:** `PUT`
- **URL:** `{{baseUrl}}/sessions/1/complete`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "duration_minutes": 45,
    "calories_burned": 320,
    "total_volume": 4520,
    "rating": 4,
    "notes": "Great session!"
  }
  ```

### Delete Session

- **Method:** `DELETE`
- **URL:** `{{baseUrl}}/sessions/1`
- **Headers:** `Authorization: Bearer {{token}}`

---

## 9. Set Records

### List Set Records

- **Method:** `GET`
- **URL:** `{{baseUrl}}/set-records`
- **Headers:** `Authorization: Bearer {{token}}`

### Create Set Record

- **Method:** `POST`
- **URL:** `{{baseUrl}}/set-records`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "workout_id": 1,
    "exercise_id": 1,
    "set_number": 1,
    "weight": 60,
    "reps": 10,
    "is_completed": false
  }
  ```

### Update Set Record

- **Method:** `PUT`
- **URL:** `{{baseUrl}}/set-records/1`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "weight": 65,
    "reps": 8,
    "is_completed": true
  }
  ```

### Delete Set Record

- **Method:** `DELETE`
- **URL:** `{{baseUrl}}/set-records/1`
- **Headers:** `Authorization: Bearer {{token}}`

---

## 10. Goals

### List Goals

- **Method:** `GET`
- **URL:** `{{baseUrl}}/goals`
- **Headers:** `Authorization: Bearer {{token}}`

### Create Goal

- **Method:** `POST`
- **URL:** `{{baseUrl}}/goals`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "title": "Bench 100kg",
    "description": "Reach 100kg bench press",
    "target_value": 100,
    "current_value": 60,
    "unit": "kg",
    "deadline": 1719000000000
  }
  ```

### Update Goal

- **Method:** `PUT`
- **URL:** `{{baseUrl}}/goals/1`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "current_value": 75,
    "status": "ACTIVE"
  }
  ```

### Delete Goal

- **Method:** `DELETE`
- **URL:** `{{baseUrl}}/goals/1`
- **Headers:** `Authorization: Bearer {{token}}`

---

## 11. Sync Batch

Send multiple pending changes in one request.

- **Method:** `POST`
- **URL:** `{{baseUrl}}/sync/batch`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
  ```json
  {
    "changes": [
      { "table": "workouts", "operation": "INSERT", "payload": { ... } },
      { "table": "set_records", "operation": "UPDATE", "payload": { ... } }
    ]
  }
  ```

---

## 12. Postman Collection Setup (Recommended)

1. Create a new **Collection** named "Arcade Fitness"
2. Set a **Collection Variable**:
   - `baseUrl` = `http://192.168.1.105:3000/api` (change IP to match your server)
3. Create a **Pre-request Script** (under Collection → Variables) to auto-refresh token:
   ```javascript
   // Optional: auto-refresh token logic here
   ```
4. Create a **Tests** script on the Login request to save the token:
   ```javascript
   const jsonData = pm.response.json();
   if (jsonData.data && jsonData.data.token) {
       pm.collectionVariables.set("token", jsonData.data.token);
   }
   ```
5. Add the `Authorization: Bearer {{token}}` header at the collection level so every request inherits it.

---

## 13. Common Error Responses

| HTTP Code | Meaning |
|-----------|---------|
| `400` | Bad request — missing or invalid fields |
| `401` | Unauthorized — missing or expired token |
| `403` | Forbidden — insufficient permissions |
| `404` | Not found — resource doesn't exist |
| `409` | Conflict — email already registered |
| `422` | Validation failed |
| `500` | Internal server error |

Error body format:
```json
{
  "status": "error",
  "message": "Human-readable error description"
}
```
