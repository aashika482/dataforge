# =============================================================================
# variables.tf — Terraform Input Variables for DataForge
#
# WHAT ARE TERRAFORM VARIABLES?
#   Variables make your Terraform configuration reusable and configurable.
#   Instead of hardcoding values like "dataforge" or "7070" directly in
#   main.tf, you define them here with a description and default value.
#
# WHY USE VARIABLES?
#   - Override defaults without editing the source files
#     (e.g. different app_name for staging vs production)
#   - Keep sensitive values out of source code
#     (pass passwords via env vars: TF_VAR_db_password=secret)
#   - Self-document what the configuration expects
#
# HOW TO OVERRIDE A DEFAULT:
#   Option 1 — command line flag:
#     terraform apply -var="app_name=dataforge-staging"
#
#   Option 2 — a .tfvars file:
#     Create terraform.tfvars:
#       app_name = "dataforge-staging"
#       app_port = 8070
#     Then run: terraform apply
#     (Terraform auto-loads terraform.tfvars if it exists)
#
#   Option 3 — environment variable:
#     export TF_VAR_app_name=dataforge-staging
#     terraform apply
# =============================================================================

# Name of the application — used in container labels and env vars
variable "app_name" {
    description = "Name of the DataForge application"
    type        = string
    default     = "dataforge"
}

# Semantic version of the application — used for labelling resources
variable "app_version" {
    description = "Version of the DataForge application"
    type        = string
    default     = "1.0.0"
}

# The port the Spring Boot application listens on inside the container
variable "app_port" {
    description = "Internal container port that Spring Boot listens on"
    type        = number
    default     = 7070

    # Validation ensures the port is in the valid range (1–65535)
    validation {
        condition     = var.app_port >= 1 && var.app_port <= 65535
        error_message = "app_port must be a valid port number (1–65535)."
    }
}
