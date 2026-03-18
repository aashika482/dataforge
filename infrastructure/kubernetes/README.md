# DataForge — Kubernetes Setup

## What is Kubernetes?

Kubernetes (K8s) is a container orchestration platform. Where Docker runs a
single container on a single machine, Kubernetes manages many containers across
many machines — automatically restarting crashed containers, distributing
traffic, scaling up under load, and rolling out updates with zero downtime.

Think of Docker as running one food stall, and Kubernetes as managing an entire
food court: scheduling stalls, replacing ones that close unexpectedly, and
directing customers to the right counter.

---

## Files in this Folder

| File | Kind | Purpose |
|---|---|---|
| `configmap.yaml` | ConfigMap | Environment variables injected into pods |
| `deployment.yaml` | Deployment | Runs 2 replicas of the DataForge container |
| `service.yaml` | Service (NodePort) | Exposes the API on port 30070 |

### configmap.yaml
Stores non-sensitive configuration (port, Spring profile, app name/version)
as key-value pairs. Injected into each pod as environment variables at startup.

### deployment.yaml
Tells Kubernetes to keep 2 identical DataForge pods running at all times.
Includes:
- **Resource limits** — prevents one pod from consuming all node memory/CPU
- **Liveness probe** — auto-restarts unresponsive pods
- **Readiness probe** — removes unhealthy pods from traffic rotation

### service.yaml
Creates a stable network endpoint for the DataForge pods.
- Internal cluster access on port **80**
- External access on NodePort **30070** → `http://localhost:30070`

---

## Prerequisites

- Docker Desktop with Kubernetes enabled, **or** Minikube running
- The DataForge Docker image built locally:
  ```bash
  docker-compose build
  ```

---

## Deploy

```bash
# Apply all three manifests at once
kubectl apply -f infrastructure/kubernetes/
```

---

## Check Status

```bash
# List all pods and their status
kubectl get pods

# List services and their ports
kubectl get services

# List deployments and replica counts
kubectl get deployments

# Describe a pod in detail (useful for debugging)
kubectl describe pod <pod-name>

# View live logs from a running pod
kubectl logs -f <pod-name>
```

---

## Access the API

Once pods are `Running` and `Ready`:

| URL | What it shows |
|---|---|
| `http://localhost:30070/api/health` | Health check |
| `http://localhost:30070/swagger-ui.html` | Swagger UI |
| `http://localhost:30070/api/generate` | POST — generate data |

---

## Delete / Tear Down

```bash
# Remove all DataForge resources from the cluster
kubectl delete -f infrastructure/kubernetes/
```

---

## Troubleshooting

**Pods stuck in `Pending`:**
```bash
kubectl describe pod <pod-name>
# Look for "Events" section at the bottom
```

**`ImagePullBackOff` error:**
The image `dataforge-dataforge-app:latest` must be built locally first:
```bash
docker-compose build
```
Then, if using Minikube, load the image into it:
```bash
minikube image load dataforge-dataforge-app:latest
```

**Pod keeps restarting (`CrashLoopBackOff`):**
```bash
kubectl logs <pod-name> --previous
```
