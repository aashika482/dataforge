# DataForge — Docker Setup

## What is Docker?

Docker is a tool that packages your application and everything it needs to run
(Java, the JAR file, environment variables, config) into a single portable unit
called a **container**. A container runs identically on any machine — your
laptop, a teammate's PC, or a cloud server — without "works on my machine" problems.

Think of it like a shipping container: the contents are always the same regardless
of which ship (server) carries it.

---

## Services in this Stack

| Service | Image | Port | Purpose |
|---|---|---|---|
| `dataforge-app` | Built from `Dockerfile` | `7070` | Spring Boot REST API |
| `dataforge-postgres` | `postgres:15-alpine` | `5432` | PostgreSQL database |
| `dataforge-nagios` | `jasonrivers/nagios` | `8081` | Monitoring dashboard |

### dataforge-app
The main DataForge backend. Built with a multi-stage Dockerfile:
- **Stage 1** compiles the Maven project into a fat JAR
- **Stage 2** copies only the JAR into a minimal JRE-Alpine image

### dataforge-postgres
A PostgreSQL relational database. Currently included for future persistence
features (generation history, saved templates). Connection details:
- Database: `dataforgedb`
- User: `dataforge`
- Password: `dataforge123`

### dataforge-nagios
Nagios Core monitoring server. Monitors service health and sends alerts.
- Dashboard: `http://localhost:8081/nagios`
- Default credentials: `nagiosadmin` / `nagios`

---

## How to Run

### Start all services (build images first)
```bash
docker-compose up --build
```

### Start in detached mode (runs in background)
```bash
docker-compose up --build -d
```

### Start only the API (skip postgres and nagios)
```bash
docker-compose up dataforge-app
```

---

## How to Stop

```bash
# Stop all services (containers are removed, volumes are kept)
docker-compose down

# Stop and also delete the postgres data volume
docker-compose down -v
```

---

## How to Check Logs

```bash
# Follow live logs for the API
docker logs -f dataforge-app

# View last 50 lines
docker logs --tail 50 dataforge-app

# Logs for all services (via compose)
docker-compose logs -f
```

---

## Useful URLs (when running)

| URL | What it shows |
|---|---|
| `http://localhost:7070/api/health` | API health check |
| `http://localhost:7070/swagger-ui.html` | Interactive API docs |
| `http://localhost:8081/nagios` | Nagios monitoring dashboard |

---

## Troubleshooting

**Port already in use:**
```bash
# Find what's using port 7070
netstat -ano | grep 7070
# Then kill the process, or change the host port in docker-compose.yml
```

**Container won't start:**
```bash
docker logs dataforge-app
```

**Rebuild from scratch (clears cached layers):**
```bash
docker-compose build --no-cache
docker-compose up
```
