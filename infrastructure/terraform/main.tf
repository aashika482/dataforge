# =============================================================================
# main.tf — Terraform Infrastructure as Code for DataForge
#
# WHAT IS TERRAFORM?
#   Terraform is an Infrastructure as Code (IaC) tool by HashiCorp. Instead of
#   manually clicking through cloud consoles or running Docker commands to
#   provision infrastructure, you write configuration files (.tf) that describe
#   what resources you need. Terraform figures out the order to create them and
#   applies the changes.
#
# KEY CONCEPTS:
#   Provider  — a plugin that connects Terraform to a platform (AWS, Docker, K8s)
#   Resource  — a specific infrastructure object (a Docker container, an S3 bucket)
#   State     — Terraform tracks what it created in a "state file" (terraform.tfstate)
#               so it knows what exists and what changed between runs
#   Plan      — "terraform plan" previews changes before applying them
#   Apply     — "terraform apply" actually creates/modifies resources
#
# THIS FILE:
#   Uses the Docker provider (kreuzwerker/docker) to provision a DataForge
#   container locally — no cloud account needed. Perfect for local development
#   and demonstrating IaC concepts in the DevOps course.
#
# WORKFLOW:
#   terraform init    — downloads the Docker provider plugin
#   terraform plan    — preview what will be created
#   terraform apply   — create the resources
#   terraform destroy — tear everything down
# =============================================================================

# -----------------------------------------------------------------------------
# Terraform Configuration Block
# Declares which providers this configuration requires and their versions.
# The version constraint "~> 3.0" means "3.x but not 4.x or above".
# -----------------------------------------------------------------------------
terraform {
    required_providers {
        docker = {
            source  = "kreuzwerker/docker"
            version = "~> 3.0"
        }
    }
}

# -----------------------------------------------------------------------------
# Provider Configuration
# Tells Terraform how to connect to Docker.
# On Linux/Mac the socket is at unix:///var/run/docker.sock.
# On Windows with Docker Desktop it uses a named pipe.
# Leaving it empty uses the DOCKER_HOST environment variable or the default.
# -----------------------------------------------------------------------------
provider "docker" {}

# -----------------------------------------------------------------------------
# Resource 1: Docker Image
#
# Pulls (or references) the DataForge Docker image.
# kind: "docker_image" manages a Docker image on the local Docker daemon.
#
#   name         — image name and tag to use
#   keep_locally — true means Terraform will NOT delete the image when you
#                  run "terraform destroy" (useful for locally-built images
#                  you want to keep for other purposes)
# -----------------------------------------------------------------------------
resource "docker_image" "dataforge" {
    # References the image built by docker-compose build
    name         = "dataforge:latest"

    # Keep the image locally even after "terraform destroy"
    # (prevents accidentally removing your locally-built image)
    keep_locally = true
}

# -----------------------------------------------------------------------------
# Resource 2: Docker Container
#
# Creates and starts a DataForge container using the image above.
# kind: "docker_container" manages the full container lifecycle.
#
# Terraform creates this container AFTER the image resource is ready
# (because it references docker_image.dataforge.image_id — Terraform
# automatically infers the dependency and orders creation correctly).
# -----------------------------------------------------------------------------
resource "docker_container" "dataforge_app" {
    # Container name — visible in "docker ps"
    name = "dataforge-terraform"

    # Reference the image ID from the resource above.
    # This is an implicit dependency — Terraform creates the image first.
    image = docker_image.dataforge.image_id

    # Port mapping: external port 7072 → internal container port 7070
    # Using 7072 (not 7070) to avoid conflict with docker-compose instance
    ports {
        internal = 7070
        external = var.app_port + 2  # 7072 — offset to avoid port conflicts
    }

    # Environment variables injected into the container at runtime
    env = [
        "SERVER_PORT=7070",
        "SPRING_PROFILES_ACTIVE=terraform",
        "APP_NAME=${var.app_name}",
        "APP_VERSION=${var.app_version}",
    ]

    # Restart policy — restart the container if it exits unexpectedly
    restart = "unless-stopped"
}
