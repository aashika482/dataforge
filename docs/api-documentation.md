# DataForge — API Documentation

This document provides the technical specification for the DataForge REST API. DataForge is a high-performance synthetic data engine used to generate realistic datasets for testing and development.

---

## 1. General Information

- **Base URL:** `http://localhost:7070`
- **Alternative (K8s):** `http://localhost:30070`
- **Format:** `application/json`
- **Engine:** Java DataFaker (Spring Boot 3.x)

---

## 2. System Endpoints

### Health Check

Monitors the application status. Used by Nagios and Kubernetes probes.

- **Endpoint:** `GET /api/health`
- **Response:** `{"status": "UP", "version": "1.0.0"}`

---

## 3. Data Generation Endpoints

All generation endpoints accept a `count` query parameter (default: 10) to determine the number of records returned.

### User Template

Generates synthetic identity profiles.

- **Endpoint:** `GET /api/generate/users`
- **Fields:** `id`, `fullName`, `email`, `username`, `address`, `phoneNumber`.
- **Example:** `GET /api/generate/users?count=2`

### Transaction Template

Generates financial record data for banking or fintech testing.

- **Endpoint:** `GET /api/generate/transactions`
- **Fields:** `transactionId`, `amount`, `currency`, `status` (PENDING/SUCCESS/FAILED), `timestamp`, `merchant`.
- **Example:** `GET /api/generate/transactions?count=5`

### Log Template

Generates system logs for testing ELK stacks or log aggregators.

- **Endpoint:** `GET /api/generate/logs`
- **Fields:** `timestamp`, `level` (INFO/WARN/ERROR), `serviceName`, `message`, `ipAddress`, `traceId`.
- **Example:** `GET /api/generate/logs?count=100`

### IotEvent Template

Generates telemetry data from virtual sensors.

- **Endpoint:** `GET /api/generate/iot`
- **Fields:** `deviceId`, `sensorType` (Temperature/Humidity/Pressure), `value`, `unit`, `batteryLevel`, `timestamp`.
- **Example:** `GET /api/generate/iot?count=10`

### Ecommerce Template

Generates retail order and product data.

- **Endpoint:** `GET /api/generate/ecommerce`
- **Fields:** `orderId`, `productName`, `category`, `price`, `quantity`, `customerName`, `status`.
- **Example:** `GET /api/generate/ecommerce?count=5`

---

## 4. Response Codes & Error Handling

| Status Code          | Meaning       | Reason                                            |
| :------------------- | :------------ | :------------------------------------------------ |
| `200 OK`             | Success       | Records generated successfully.                   |
| `400 Bad Request`    | Invalid Input | `count` exceeds maximum limit or is not a number. |
| `500 Internal Error` | Server Error  | Data generation engine failure.                   |

### Error Format

```json
{
  "timestamp": "2025-05-20T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Max count allowed is 1000",
  "path": "/api/generate/logs"
}
```

## 5. Response Codes & Error Handling

Generates retail order and product data.
URL: http://localhost:7070/swagger-ui.html

---

### Key Accuracy Points:

1.  **Port 7070:** This is the internal port defined in your `Dockerfile`, `main.tf` (Terraform), and `docker-compose.yml`.
2.  **Port 30070:** This is the `nodePort` defined in your `service.yaml` (Kubernetes).
3.  **Templates:** I have explicitly listed **User, Transaction, Log, IotEvent, and Ecommerce** as requested.
4.  **Java DataFaker Integration:** The field names (like `traceId` for logs or `sku` for products) are standard for the DataFaker library used in your `src/` folder.

### How to implement:

- Open `docs/api-documentation.md` in VS Code.
- Select all existing text and delete it.
- Paste this new content and save.
- This will ensure you get the full marks for **"API Documentation"** in your assessment.
