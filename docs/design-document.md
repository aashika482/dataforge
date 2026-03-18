# DataForge — Technical Design Document

## 1. System Overview

DataForge is a cloud-native microservice designed to generate high-fidelity synthetic data. It allows developers to simulate production-like datasets without compromising privacy or security.

### Core Objectives

- **High Availability:** Using Kubernetes to ensure zero downtime.
- **Automation:** Full CI/CD integration for seamless updates.
- **Scalability:** Stateless design allowing horizontal scaling.

---

## 2. Architecture Diagram (Logic)

The application follows a standard **Three-Tier DevOps Architecture**:

1.  **Application Tier:** Spring Boot 3.x REST API running on Java 17.
2.  **Infrastructure Tier:** Docker containers orchestrated by Kubernetes.
3.  **Automation Tier:** Jenkins and GitHub Actions managing the lifecycle.

---

## 3. Data Generation Engine

The core logic resides in the `Service Layer`, which utilizes the **Java DataFaker** library.

### Data Templates Supported:

To ensure versatility, DataForge implements five distinct generation strategies:

- **User:** Identity profiles (Names, Addresses, Contact info).
- **Transaction:** Financial records (Amounts, Currencies, Statuses).
- **Log:** System telemetry (Error levels, IP addresses, Trace IDs).
- **IotEvent:** Sensor data (Temperature, Battery, Device status).
- **Ecommerce:** Order details (SKUs, Pricing, Customer data).

**Design Choice:** The engine is **Stateless**. It does not require a database (PostgreSQL/MongoDB) to store records; data is generated on-the-fly and streamed to the client, maximizing performance and reducing infrastructure costs.

---

## 4. DevOps Implementation (IaC & CI/CD)

### Infrastructure as Code (IaC)

- **Terraform:** Used to provision the local Docker environment. It manages the lifecycle of the `dataforge-app` container, ensuring the development environment is identical for all team members.
- **Puppet:** Acts as the configuration manager. The Puppet manifest (`init.pp`) ensures the host OS has the correct Java 17 runtime, directory structures, and environment variables, preventing "Configuration Drift."

### CI/CD Pipelines

We implement a **Dual-Pipeline Strategy**:

1.  **GitHub Actions:** Handles the "Shift-Left" logic—running unit tests and security scans (Trivy) immediately upon code push.
2.  **Jenkins:** Handles the "Continuous Delivery" logic—managing environment-specific deployments (Staging vs. Production) with manual approval gates.

### Orchestration (Kubernetes)

The application is deployed as a Kubernetes `Deployment`:

- **Replicas:** 2 pods are maintained for high availability.
- **Probes:** Liveness and Readiness probes monitor `/api/health` to automatically restart failing containers.
- **Service:** A `NodePort` service exposes the app on port `30070`.

---

## 5. Monitoring & Reliability

**Nagios** is integrated as the primary monitoring solution. It is configured to perform three specific checks:

1.  **HTTP Check:** Pings `/api/health` to ensure the Spring context is active.
2.  **Template Check:** Verifies the availability of the `/api/schema/templates` endpoint.
3.  **TCP Check:** Monitors port `7070` for connectivity.

---

## 6. Security Design

- **Container Security:** The CI/CD pipeline includes a **Trivy Scan** stage that analyzes the Docker image for Critical and High vulnerabilities before deployment.
- **Network Security:** In production, the app is isolated within a Kubernetes Namespace, with only the `NodePort` service exposed to external traffic.
- **Data Privacy:** By design, DataForge creates 100% synthetic data, ensuring that no PII (Personally Identifiable Information) ever enters the test environment.
