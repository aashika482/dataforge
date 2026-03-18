// =============================================================================
// HealthController.java
// A simple endpoint that confirms the DataForge API is running.
//
// WHY A CUSTOM HEALTH ENDPOINT?
//   Spring Boot Actuator already provides /actuator/health, but it reports
//   low-level framework status (disk space, database connections, etc.).
//   This custom endpoint returns business-level information — the application
//   name, version, and current timestamp — which is more useful for:
//     - Frontend apps that need to confirm the backend is reachable
//     - Kubernetes liveness probes
//     - Nagios / Prometheus monitoring checks
//     - Developers doing a quick sanity check after deployment
//
// NOTE: This endpoint requires NO authentication and should always return 200.
//   If it returns anything else, the service is down.
// =============================================================================

package com.dataforge.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight controller that exposes a health-check endpoint.
 *
 * Accessible at: GET http://localhost:8080/api/health
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "API health and status check")
public class HealthController {

    /**
     * Returns the current status of the DataForge API.
     *
     * Example response:
     * {
     *   "status"      : "UP",
     *   "application" : "DataForge",
     *   "version"     : "1.0.0",
     *   "timestamp"   : "2024-01-15T10:30:00Z"
     * }
     */
    @Operation(
        summary     = "Health check",
        description = "Returns HTTP 200 with status UP when the API is running. " +
                      "Use this endpoint for liveness probes, monitoring scripts, " +
                      "and confirming the backend is reachable."
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status",      "UP");
        status.put("application", "DataForge");
        status.put("version",     "1.0.0");
        // Instant.now() gives the current UTC date and time in ISO-8601 format
        status.put("timestamp",   Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
