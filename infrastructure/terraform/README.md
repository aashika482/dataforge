# DataForge — Terraform Infrastructure as Code

## What is Terraform?

Terraform is an Infrastructure as Code (IaC) tool by HashiCorp. It lets you
define your infrastructure — servers, containers, networks, databases — in
plain text configuration files, then provision everything with a single command.

**Why use Terraform instead of manual setup?**
- **Reproducible** — run `terraform apply` on any machine and get the exact same infrastructure
- **Version controlled** — infrastructure changes go through Git like any other code change
- **Safe** — `terraform plan` shows exactly what will change before anything happens
- **Destroyable** — `terraform destroy` removes everything cleanly, no orphaned resources

**Analogy:** Terraform is like an IKEA instruction manual for cloud infrastructure.
The manual (your .tf files) is the same every time, and following it always
produces the same result.

---

## What This Configuration Does

This Terraform setup uses the **Docker provider** to provision a DataForge
container locally — no cloud account or credit card required.

| File | Purpose |
|---|---|
| `main.tf` | Defines the Docker image reference and container |
| `variables.tf` | Configurable inputs (app name, version, port) |
| `outputs.tf` | Values printed after apply (URL, container name) |

**Resources created:**

| Resource | Type | Details |
|---|---|---|
| `docker_image.dataforge` | Docker image | References `dataforge:latest` locally |
| `docker_container.dataforge_app` | Docker container | Named `dataforge-terraform`, port 7072 |

The container runs on port **7072** (not 7070) to avoid conflicting with the
docker-compose instance.

---

## Prerequisites

1. Docker Desktop must be running
2. The DataForge image must be built:
   ```bash
   docker-compose build
   ```
3. Terraform must be installed:
   ```bash
   # Windows (with Chocolatey)
   choco install terraform

   # macOS
   brew tap hashicorp/tap && brew install hashicorp/tap/terraform

   # Verify
   terraform --version
   ```

---

## Commands

### 1. Initialise — download the Docker provider plugin
```bash
terraform init
```

### 2. Plan — preview what will be created (no changes made)
```bash
terraform plan
```

### 3. Apply — create the resources
```bash
terraform apply
# Type "yes" when prompted
```

### 4. View outputs
```bash
terraform output
```
Expected output:
```
app_url        = "http://localhost:7072"
container_name = "dataforge-terraform"
health_url     = "http://localhost:7072/api/health"
swagger_url    = "http://localhost:7072/swagger-ui.html"
```

### 5. Destroy — remove all resources
```bash
terraform destroy
# Type "yes" when prompted
```

---

## Override Variables

```bash
# Change the app port
terraform apply -var="app_port=8070"

# Use a specific version label
terraform apply -var="app_version=2.0.0"
```

Or create a `terraform.tfvars` file:
```hcl
app_name    = "dataforge-staging"
app_version = "1.0.0"
app_port    = 7070
```

---

## State File

After `terraform apply`, Terraform creates `terraform.tfstate` — a JSON file
tracking all managed resources. **Do not delete this file** or Terraform loses
track of what it created. For team use, store state remotely (S3, Terraform Cloud).

The `.gitignore` in this project excludes `*.tfstate` to prevent accidentally
committing infrastructure state (which may contain sensitive data).
