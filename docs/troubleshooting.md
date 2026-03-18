# DataForge — Troubleshooting Guide

This document provides solutions to common issues encountered during the setup, deployment, and operation of the DataForge synthetic data engine.

---

## 1. Application & API Issues

### Issue: Application fails to start (Port 7070 Conflict)

- **Symptoms:** `java.net.BindException: Address already in use` in logs.
- **Cause:** Another service is already using port 7070 on your machine.
- **Solution:**
  1.  Identify the process: `lsof -i :7070` (Mac/Linux) or `netstat -ano | findstr :7070` (Windows).
  2.  Kill the process or change the port mapping in `docker-compose.yml` to `7071:7070`.

### Issue: API returns 404 for specific templates

- **Symptoms:** `GET /api/generate/nonexistent` returns 404.
- **Solution:** Ensure you are using one of the five supported templates:
  - `/api/generate/users`
  - `/api/generate/transactions`
  - `/api/generate/logs`
  - `/api/generate/iot`
  - `/api/generate/ecommerce`

---

## 2. Docker & Container Issues

### Issue: Container crashes immediately after `docker-compose up`

- **Symptoms:** Container status is `Exited (1)`.
- **Solution:** Check the logs:
  ```bash
  docker logs dataforge-app
  ```
  Common cause: Incorrect Java version or missing environment variables defined in the `.env` file.

### Issue: "Image not found" during build

- **Symptoms:** `repository dataforge not found` or `manifest for dataforge:latest not found`.
- **Solution:** Ensure you build the image locally before running it:
  ```bash
  docker-compose build --no-cache
  ```

---

## 3. Kubernetes (K8s) Issues

### Issue: Pods stuck in `ImagePullBackOff`

- **Symptoms:** `kubectl get pods` shows `ImagePullBackOff`.
- **Cause:** Kubernetes is trying to pull the image from Docker Hub instead of using your local build.
- **Solution:**
  1.  Ensure `imagePullPolicy: Never` is set in `infrastructure/kubernetes/deployment.yaml`.
  2.  If using Minikube, point your terminal to Minikube's Docker daemon before building:
      ```bash
      eval $(minikube docker-env)
      docker build -t dataforge:latest .
      ```

### Issue: Cannot access service via NodePort (30070)

- **Symptoms:** `http://localhost:30070` times out.
- **Solution:**
  - If using **Minikube**, run: `minikube service dataforge-service --url`.
  - If using **Docker Desktop**, ensure the service type is `NodePort` and port `30070` is correctly mapped in `service.yaml`.

---

## 4. CI/CD Pipeline Issues

### Issue: Jenkins Pipeline fails at "Security Scan"

- **Symptoms:** Trivy scan fails or hangs.
- **Solution:** Ensure the Jenkins user has permission to access the Docker socket. Run:
  ```bash
  sudo chmod 666 /var/run/docker.sock
  ```

### Issue: GitHub Actions "Test" stage fails

- **Symptoms:** JUnit tests fail during the workflow run.
- **Solution:** Check the "Upload test results" artifact in the GitHub Actions UI to see the specific test failures in the `surefire-reports`.

---

## 5. Monitoring (Nagios) Issues

### Issue: Nagios shows "CRITICAL - Socket Timeout"

- **Symptoms:** Status is Red even though the app is running.
- **Solution:**
  - Check if the app is listening on the IP Nagios is expecting (usually `127.0.0.1` or `localhost`).
  - Verify that the path `/api/health` is accessible via `curl`.

---

## 6. Infrastructure as Code (Terraform/Puppet)

### Issue: Terraform `init` fails

- **Symptoms:** Error: `Failed to query available provider packages`.
- **Solution:** Check your internet connection. Terraform needs to download the `kreuzwerker/docker` provider. Run `terraform init -upgrade` to force a refresh.

### Issue: Puppet `apply` fails

- **Symptoms:** Error: `Could not find resource package 'java-17-openjdk'`.
- **Solution:** Ensure your package manager (apt/yum) is updated or change the package name in `init.pp` to match your specific OS distribution.
