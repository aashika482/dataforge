// =============================================================================
// GenerationRequest.java
// Represents the JSON body that the client sends to the /api/generate endpoint.
//
// Example request body:
// {
//   "type": "users",
//   "count": 50,
//   "format": "json"
// }
//
// Spring's @Valid annotation will automatically reject requests where count
// is outside the 1-1000 range before the controller method even runs.
// =============================================================================

package com.dataforge.app.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for data generation.
 *
 * Lombok @Data auto-generates:
 *   - getters and setters for all fields
 *   - equals() and hashCode()
 *   - toString()
 *   - a required-args constructor
 * This keeps the class short and focused on its fields.
 */
@Data
public class GenerationRequest {

    /**
     * The type of data to generate.
     * Supported values: "users", "transactions", "logs", "iot", "ecommerce"
     */
    @NotBlank(message = "type must not be blank")
    private String type;

    /**
     * Number of rows/records to generate.
     * Must be between 1 and 1000 (enforced by Bean Validation).
     * Defaults to 10 if not provided.
     */
    @Min(value = 1, message = "count must be at least 1")
    @Max(value = 1000, message = "count must not exceed 1000")
    private int count = 10;

    /**
     * Output format for the generated data.
     * Supported values: "json", "csv", "sql", "xml"
     * Defaults to "json" if not provided.
     */
    private String format = "json";
}
