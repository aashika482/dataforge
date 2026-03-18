# DataForge — User Guide

Welcome to the **DataForge** User Guide. This document provides instructions on how to set up, run, and interact with the DataForge synthetic data generation engine.

---

## 1. Overview

DataForge is a Spring Boot-based microservice designed to generate realistic, non-sensitive synthetic data (Users, Products, etc.) for testing and development purposes.

---

## 2. Prerequisites

Before starting, ensure you have the following installed on your system:

- **Docker Desktop** (v20.10 or higher)
- **Git** (v2.30 or higher)
- **Java 17 JDK** (Only required if running without Docker)
- **Maven 3.8+** (Only required if building manually)

---

## 3. Installation & Setup

### Option A: Running with Docker (Recommended)

This is the fastest way to get DataForge running with all its dependencies.

1.  **Clone the Repository:**

    ```bash
    git clone https://github.com/aashika482/dataforge.git
    cd dataforge
    ```

2.  **Build and Start:**

    ```bash
    docker-compose up --build -d
    ```

3.  **Verify Status:**
    The application will be available at `http://localhost:7070`.

---

### Option B: Running via Kubernetes

If you have a local cluster (Minikube or Docker Desktop K8s) enabled:

1.  **Apply Manifests:**

    ```bash
    kubectl apply -f infrastructure/kubernetes/configmap.yaml
    kubectl apply -f infrastructure/kubernetes/deployment.yaml
    kubectl apply -f infrastructure/kubernetes/service.yaml
    ```

2.  **Access the App:**
    The service is exposed via NodePort on port **30070**.
    URL: `http://localhost:30070`

---

## 4. How to Use the API

DataForge is a "headless" service, meaning you interact with it via REST endpoints.

### Using the Interactive UI (Swagger)

For a visual way to test the data generation, navigate to:
**[http://localhost:7070/swagger-ui.html](http://localhost:7070/swagger-ui.html)**

### Manual API Calls

You can use `curl` or tools like Postman to generate data.

#### 1. Generate Fake Users

Returns a list of synthetic user profiles (Name, Email, Job Title).

- **Endpoint:** `/api/generate/users`
- **Method:** `GET`
- **Query Param:** `count` (default is 10)
- **Example:**
  ```bash
  curl "http://localhost:7070/api/generate/users?count=5"
  ```

#### 2. Generate Fake Products

Returns synthetic inventory data (Name, Price, Category).

- **Endpoint:** `/api/generate/products`
- **Method:** `GET`
- **Example:**
  ```bash
  curl "http://localhost:7070/api/generate/products?count=3"
  ```

#### 3. Health Check

Verify if the engine and its dependencies are running.

- **Endpoint:** `/api/health`
- **Example:**
  ```bash
  curl "http://localhost:7070/api/health"
  ```

---

## 5. Monitoring

If you have configured Nagios as per the project instructions:

1.  Access the Nagios Dashboard at `http://localhost:8081`.
2.  Login with `nagiosadmin` / `nagios`.
3.  Check the "Services" tab to see the real-time health of the DataForge API.

---

## 6. Troubleshooting

| Issue                                  | Solution                                                                                          |
| :------------------------------------- | :------------------------------------------------------------------------------------------------ |
| **Port 7070 already in use**           | Stop any existing services on 7070 or change the port in `docker-compose.yml`.                    |
| **Docker container exits immediately** | Check logs using `docker logs dataforge-app`. Ensure Java 17 is specified in the Dockerfile.      |
| **K8s Pods stuck in ImagePullBackOff** | Ensure `imagePullPolicy: Never` is set in `deployment.yaml` and you have built the image locally. |
| **Maven Build fails**                  | Ensure you are using JDK 17. Run `java -version` to verify.                                       |

---

## 7. Support

For technical issues or feature requests, please open an Issue in the [GitHub Repository](https://github.com/aashika482/dataforge).
