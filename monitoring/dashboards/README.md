# DataForge — Nagios Monitoring Dashboard

## What Nagios Monitors

Nagios is an open-source monitoring system that continuously checks whether
your services are running and alerts you when they are not.

For DataForge, Nagios monitors:

| Check | What it verifies |
|---|---|
| **DataForge API Health** | `GET /api/health` returns HTTP 200 |
| **DataForge API Response** | `GET /api/schema/templates` returns HTTP 200 |
| **DataForge Port 7070** | TCP port 7070 is open and accepting connections |
| **Host Alive** | The server responds to ping |

Each check runs every **5 minutes**. After **3 consecutive failures** an email
alert is sent. A recovery email is sent when the service comes back up.

---

## Accessing the Dashboard

### When running with Docker Compose:

```bash
docker-compose up -d
```

Then open: **http://localhost:8081/nagios**

### Login Credentials

| Field | Value |
|---|---|
| Username | `nagiosadmin` |
| Password | `nagios` |

---

## Navigating the Dashboard

| Section | What it shows |
|---|---|
| **Tactical Overview** | Summary of hosts/services: OK, Warning, Critical |
| **Hosts** | List of monitored servers and their current state |
| **Services** | All service checks with current status and last check time |
| **Problems** | Only hosts/services currently in a non-OK state |
| **Alert History** | Log of all past state changes and notifications |

---

## Understanding Service Check States

| State | Colour | Meaning |
|---|---|---|
| **OK** | Green | Check passed, service is healthy |
| **WARNING** | Yellow | Check returned a warning threshold |
| **CRITICAL** | Red | Check failed — service may be down |
| **UNKNOWN** | Orange | Check could not determine service state |
| **PENDING** | Grey | Check has not run yet since Nagios started |

---

## Service Check Details

### DataForge API Health
- **Command:** `check_http -p 7070 -u /api/health`
- **Passes when:** HTTP 200 response with body containing `"status":"UP"`
- **Fails when:** Connection refused, timeout, or non-200 response
- **Meaning:** The Spring Boot application is running and healthy

### DataForge API Response
- **Command:** `check_http -p 7070 -u /api/schema/templates`
- **Passes when:** HTTP 200 response
- **Meaning:** The generator engine is initialised and API is serving requests

### DataForge Port 7070
- **Command:** `check_tcp 7070`
- **Passes when:** TCP connection to port 7070 succeeds
- **Meaning:** The server process is listening (even if app is erroring internally)

---

## Alert Emails

When a check fails, an email is sent to `aashika482@example.com` with:
- Host name and service description
- Current state (CRITICAL / WARNING / RECOVERY)
- Check output (e.g. "Connection refused")
- Timestamp of the failure

Configure alert recipients in `monitoring/alerts/email-alerts.cfg`.
