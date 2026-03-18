# =============================================================================
# outputs.tf — Terraform Output Values for DataForge
#
# WHAT ARE TERRAFORM OUTPUTS?
#   After "terraform apply" finishes, Terraform prints output values to the
#   terminal. Outputs serve two purposes:
#
#   1. INFORMATION — show useful details after provisioning completes,
#      such as the URL to access the newly created service.
#
#   2. MODULE CHAINING — when one Terraform module depends on another,
#      outputs from module A can be used as inputs to module B.
#      (Not used here, but important in larger IaC setups.)
#
# HOW TO VIEW OUTPUTS AFTER APPLY:
#   terraform output              — print all outputs
#   terraform output app_url      — print only the app_url output
#   terraform output -json        — print as JSON (useful for scripting)
# =============================================================================

# The name of the running Docker container
output "container_name" {
    description = "Name of the DataForge Docker container managed by Terraform"
    value       = docker_container.dataforge_app.name
}

# The URL where the DataForge API is accessible from the host machine
output "app_url" {
    description = "URL to access the DataForge API from the host machine"
    value       = "http://localhost:7072"
}

# The URL for the Swagger UI (interactive API documentation)
output "swagger_url" {
    description = "URL for the Swagger UI interactive API documentation"
    value       = "http://localhost:7072/swagger-ui.html"
}

# The URL for the health check endpoint
output "health_url" {
    description = "URL for the DataForge health check endpoint"
    value       = "http://localhost:7072/api/health"
}
