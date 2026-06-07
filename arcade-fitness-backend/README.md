# Arcade Fitness Planner — Backend API

**St. Mary's University Academic Submission**  
Production-hardened Node.js/Express REST API with PostgreSQL backend.

---

## Architecture Overview

```
Client (Android)  ──HTTPS──>  Nginx Reverse Proxy  ──>  Node.js/Express API  ──>  PostgreSQL 15
                                     │                                                        │
                                     └──>  Static Assets / SSL Termination                  Connection Pool (max 20)
```

---

## Compute Metrics — Baseline Hosting

| Component          | Specification                          |
|--------------------|----------------------------------------|
| **OS**             | Ubuntu 22.04.4 LTS (Jammy)             |
| **vCPUs**          | 2 vCPUs (x86_64, ~2.5 GHz)            |
| **RAM**            | 4 GB DDR4 ECC                          |
| **Storage**        | 50 GB SSD (NVMe, ~3000 IOPS)           |
| **Instance Type**  | Dedicated VPS (no noisy neighbors)     |
| **Node.js**        | v20 LTS (built on V8 11.3)            |
| **PostgreSQL**     | 15.x with `pg_stat_statements`         |
| **Reverse Proxy**  | Nginx 1.24 (SSL termination + rate limit) |
| **Process Mgr**    | PM2 (cluster mode, 2 instances)        |

### Connection & Pool Configuration

| Parameter                  | Value      | Rationale                              |
|----------------------------|------------|----------------------------------------|
| `pg.Pool` max connections  | 20         | 2 vCPU × 10 = 20 (PostgreSQL formula)  |
| `idleTimeoutMillis`        | 30,000 ms  | Free idle connections aggressively      |
| `connectionTimeoutMillis`  | 5,000 ms   | Fail fast under load spikes             |
| Nginx `worker_connections` | 1024       | Kernel socket backlog ceiling           |
| Keep-Alive timeout         | 65 s       | AWS/DO LB standard                     |

---

## Scalability & Capacity Planning

### Vertical Scaling Path (Phase 1 — 0 to 5,000 DAU)

| Metric                  | Baseline (2 vCPU / 4 GB) | Scaled (4 vCPU / 8 GB) |
|-------------------------|--------------------------|-------------------------|
| Max concurrent requests | ~450                     | ~950                    |
| PostgreSQL connections  | 20                       | 40                      |
| Estimated DAU ceiling   | 3,000                    | 8,000                   |
| Monthly cost (approx)   | ~$24 USD                 | ~$48 USD                |

**Trigger condition:** CPU sustained > 70% or p95 latency > 500 ms for 10 minutes.

### Horizontal Scaling Path (Phase 2 — 5,000 to 50,000+ DAU)

```
Load Balancer (HAProxy / AWS ALB)
        │
        ├── API Instance 1 (2 vCPU / 4 GB)
        ├── API Instance 2 (2 vCPU / 4 GB)
        └── API Instance N (auto-scale group)
                │
                └── PostgreSQL Primary ── Streaming Replication ──> Read Replica(s)
```

| Component              | Scaling Strategy                          |
|------------------------|-------------------------------------------|
| **API layer**          | Stateless horizontal — spawn behind ALB   |
| **PostgreSQL**         | Vertical first, then read replicas        |
| **Connection pooling** | PgBouncer (transaction pooling) in front  |
| **Caching**            | Redis (6.x) — session tokens, exercise catalog |
| **CDN**                | CloudFront / Fastly for static responses  |

### Caching Model

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│  Client App  │ ────> │  API Server  │ ────> │  Redis Cache │
└──────────────┘       └──────────────┘       └──────────────┘
                              │                       │
                              │ (cache miss)           │ (TTL-based)
                              v                       v
                       ┌──────────────┐       ┌──────────────┐
                       │  PostgreSQL  │       │  TTL: 300s   │
                       └──────────────┘       └──────────────┘
```

**Cache targets:** Exercise list (TTL 300 s), workout templates (TTL 600 s), user profile (TTL 60 s).  
**Invalidation:** On write to corresponding table, delete cache key.

### Data Transfer Protection

| Layer        | Mechanism                                      |
|--------------|-------------------------------------------------|
| **In transit** | TLS 1.3 (Nginx — Let's Encrypt / certbot)     |
| **At rest**    | PostgreSQL TDE (transparent data encryption)   |
| **Passwords**  | bcryptjs cost factor 10                        |
| **Tokens**     | JWT RS256 (7d expiry, no refresh token yet)    |
| **Headers**    | `helmet` middleware (CSP, HSTS, X-Frame-Options) |

---

## Infrastructure Budget Projection

| User Tier   | DAU     | API Instances | DB Tier       | Est. Monthly Cost |
|-------------|---------|---------------|---------------|-------------------|
| Launch      | 500     | 1 × 2 vCPU    | 2 vCPU / 4 GB | ~$24              |
| Growth      | 3,000   | 2 × 2 vCPU    | 2 vCPU / 4 GB | ~$60              |
| Scale       | 10,000  | 4 × 2 vCPU    | 4 vCPU / 8 GB | ~$180             |
| Enterprise  | 50,000+ | 8+ × 2 vCPU   | 8 vCPU / 32 GB + replica | ~$600+  |

*Costs estimated on DigitalOcean / Vultr equivalent tier pricing as of 2026.*  
*Redis and PgBouncer run colocated on API instances until 10,000 DAU.*

---

## API Route Reference

| Method | Endpoint                    | Auth  | Description                          |
|--------|-----------------------------|-------|--------------------------------------|
| POST   | `/api/auth/register`        | No    | Create account + return JWT          |
| POST   | `/api/auth/login`           | No    | Authenticate + return JWT            |
| GET    | `/api/workouts`             | JWT   | List user workouts                   |
| GET    | `/api/workouts/:id`         | JWT   | Get workout by ID                    |
| POST   | `/api/workouts`             | JWT   | Create workout template              |
| PUT    | `/api/workouts/:id`         | JWT   | Update workout                       |
| DELETE | `/api/workouts/:id`         | JWT   | Delete workout                       |
| GET    | `/api/exercises`            | JWT   | List exercises (?muscle_group=)      |
| GET    | `/api/set-records/workout/:workoutId` | JWT | Sets for a workout         |
| GET    | `/api/set-records/session/:sessionId` | JWT | Sets for a session         |
| POST   | `/api/set-records`          | JWT   | Log a set record                     |
| PUT    | `/api/set-records/:id`      | JWT   | Update set record                    |
| DELETE | `/api/set-records/:id`      | JWT   | Delete set record                    |
| GET    | `/api/profiles`             | JWT   | Get user profile                     |
| PUT    | `/api/profiles`             | JWT   | Update user profile                  |
| GET    | `/api/goals`                | JWT   | List user goals                      |
| POST   | `/api/goals`                | JWT   | Create goal                          |
| PUT    | `/api/goals/:id`            | JWT   | Update goal                          |
| DELETE | `/api/goals/:id`            | JWT   | Delete goal                          |
| GET    | `/api/sessions`             | JWT   | List workout sessions                |
| GET    | `/api/sessions/:id`         | JWT   | Get session by ID                    |
| POST   | `/api/sessions`             | JWT   | Start a workout session              |
| PUT    | `/api/sessions/:id/complete`| JWT   | Complete a session (duration/end)    |
| DELETE | `/api/sessions/:id`         | JWT   | Delete a session                     |
| POST   | `/api/sync`                 | JWT   | Batch sync offline queue operations  |

---

## Local Development

```bash
# 1. Install dependencies
npm install

# 2. Configure environment (edit .env with your PostgreSQL credentials)
cp .env .env.local

# 3. Create database and run schema
psql -U fitness_user -d arcade_fitness_db -f database/schema.sql

# 4. Start development server
npm run dev
```

---

## Project Structure

```
arcade-fitness-backend/
├── server.js              # Entry point — mounts middleware + routes
├── package.json           # Dependencies and scripts
├── .env                   # Environment variables (not committed)
├── config/
│   └── db.js              # pg.Pool connection manager
├── database/
│   └── schema.sql         # 3NF schema + 25 exercise seeds + indexes
├── middleware/
│   ├── auth.js            # JWT Bearer token validation
│   └── errorHandler.js    # Centralized error formatting
├── controllers/           # Business logic (8 modules)
│   ├── authController.js
│   ├── workoutController.js
│   ├── exerciseController.js
│   ├── setRecordController.js
│   ├── profileController.js
│   ├── goalController.js
│   ├── sessionController.js
│   └── syncController.js
└── routes/                # Express router definitions (8 modules)
    ├── auth.js
    ├── workouts.js
    ├── exercises.js
    ├── set-records.js
    ├── profiles.js
    ├── goals.js
    ├── sessions.js
    └── sync.js
```

---

## Database Schema (7 Tables — 3NF)

| Table              | Purpose                                        |
|--------------------|-------------------------------------------------|
| `users`            | Authentication credentials                      |
| `user_profiles`    | Body metrics (height, weight targets)           |
| `workouts`         | Routine architecture templates                  |
| `exercises`        | 25 seeded movements by muscle group             |
| `set_records`      | Per-set weight, reps, RPE logs                  |
| `goals`            | Daily/weekly milestone targets                  |
| `workout_sessions` | Historical execution logs with duration         |
| `sync_queue`       | Immutable server-side audit trail for sync ops  |

---

## Security Checklist

- [x] bcryptjs cost factor 10 for password hashing
- [x] JWT Bearer token validation with expiration handling
- [x] Helmet security headers (CSP, HSTS, X-Frame-Options)
- [x] CORS whitelist-configurable
- [x] Production error stack-trace suppression
- [x] SQL injection protection via parameterized queries
- [x] Request body size limit (1 MB)
- [x] Rate limiting ready (Nginx layer)
