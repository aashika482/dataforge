// =============================================================================
// GenerationResponse.java
// Represents the JSON body that the API sends back after generating data.
//
// Example response:
// {
//   "type": "users",
//   "count": 5,
//   "format": "json",
//   "generationTimeMs": 42,
//   "data": [ { "id": "...", "firstName": "John", ... }, ... ]
// }
//
// The 'data' field is typed as Object so it can hold:
//   - List<Map<String,Object>> for JSON responses
//   - a plain String for CSV / SQL / XML responses
// =============================================================================

package com.dataforge.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model returned by the /api/generate endpoint.
 *
 * Lombok annotations used:
 *   @Data           - generates getters, setters, equals, hashCode, toString
 *   @Builder        - enables the fluent builder pattern: GenerationResponse.builder()...build()
 *   @NoArgsConstructor - generates a no-arg constructor (required by Jackson for JSON serialization)
 *   @AllArgsConstructor - generates a constructor with all fields (required by @Builder)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {

    /** The data type that was generated (e.g. "users", "transactions") */
    private String type;

    /** Number of records actually generated */
    private int count;

    /** Format of the data returned ("json", "csv", "sql", "xml") */
    private String format;

    /** How long the generation took in milliseconds — useful for benchmarking */
    private long generationTimeMs;

    /**
     * The generated data payload.
     * - For JSON: a List<Map<String, Object>> (serialized as a JSON array)
     * - For CSV / SQL / XML: a plain String
     */
    private Object data;
}
