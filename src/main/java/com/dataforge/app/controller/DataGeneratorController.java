// =============================================================================
// DataGeneratorController.java
// The REST API controller — the front door of the DataForge backend.
//
// WHAT IS A REST API?
//   REST (Representational State Transfer) is a style for building web APIs.
//   A client (browser, Postman, frontend React app, CLI tool) sends an HTTP
//   request to a URL (called an endpoint). The server processes it and sends
//   back a response — usually JSON.
//
// HTTP METHODS used here:
//   GET  — fetching/reading data (no body required, safe to repeat)
//   POST — sending data to be processed (has a JSON body)
//
// SPRING ANNOTATIONS:
//   @RestController    — marks this class as a REST API controller; every method
//                        automatically returns JSON (no need for @ResponseBody).
//   @RequestMapping    — sets the base URL prefix for all endpoints in this class.
//   @GetMapping        — maps a GET request to a method.
//   @PostMapping       — maps a POST request to a method.
//   @Valid             — triggers Bean Validation on the incoming request body
//                        (checks @Min, @Max, @NotBlank etc. in GenerationRequest).
//   @PathVariable      — binds a URL segment like /export/{format} to a parameter.
//   @RequestParam      — binds a query string parameter like ?type=users.
//
// SPRINGDOC / SWAGGER:
//   @Operation annotations generate the interactive Swagger UI at /swagger-ui.html
// =============================================================================

package com.dataforge.app.controller;

import com.dataforge.app.export.ExportService;
import com.dataforge.app.model.GenerationRequest;
import com.dataforge.app.model.GenerationResponse;
import com.dataforge.app.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes the DataForge REST API under the /api path.
 *
 * All endpoints are documented in Swagger UI — run the app and visit:
 *   http://localhost:8080/swagger-ui.html
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Data Generator", description = "Generate and export synthetic test data")
public class DataGeneratorController {

    // -------------------------------------------------------------------------
    // Dependencies injected by Spring
    // -------------------------------------------------------------------------

    /** Delegates to the correct generator based on data type */
    private final GeneratorService generatorService;

    /** Converts generated data into JSON / CSV / SQL / XML strings */
    private final ExportService exportService;

    public DataGeneratorController(GeneratorService generatorService,
                                   ExportService exportService) {
        this.generatorService = generatorService;
        this.exportService    = exportService;
    }

    // =========================================================================
    // Endpoint 1 — POST /api/generate
    // =========================================================================

    /**
     * Main generation endpoint.
     *
     * The client sends a JSON body like:
     *   { "type": "users", "count": 50, "format": "json" }
     *
     * Spring automatically deserialises the body into a GenerationRequest object,
     * and @Valid triggers validation (count must be 1–1000, type must not be blank).
     *
     * Returns a GenerationResponse containing the generated data plus metadata.
     */
    @Operation(
        summary     = "Generate synthetic data",
        description = "Generates 1–1000 records of the specified type in the requested format. " +
                      "Supported types: users, transactions, logs, iot, ecommerce. " +
                      "Supported formats: json, csv, sql, xml."
    )
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody GenerationRequest request) {
        // Record start time so we can report how long generation took
        long startTime = System.currentTimeMillis();

        try {
            // Ask the service layer to produce the raw data
            List<Map<String, Object>> rawData =
                generatorService.generate(request.getType(), request.getCount());

            // Convert to the requested output format
            Object outputData = formatData(rawData, request.getFormat(), request.getType());

            long elapsedMs = System.currentTimeMillis() - startTime;

            // Build and return the response wrapper
            GenerationResponse response = GenerationResponse.builder()
                    .type(request.getType())
                    .count(rawData.size())
                    .format(request.getFormat())
                    .generationTimeMs(elapsedMs)
                    .data(outputData)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Unknown type → return HTTP 400 Bad Request with a helpful message
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // Endpoint 2 — GET /api/schema/templates
    // =========================================================================

    /**
     * Returns example schemas (field names) for all five supported data types.
     *
     * Useful for clients that want to know what fields to expect before
     * making a generation request. No data is generated here.
     */
    @Operation(
        summary     = "Get schema templates",
        description = "Returns the available data types and the fields each type produces. " +
                      "Use this to preview what a generation request will return."
    )
    @GetMapping("/schema/templates")
    public ResponseEntity<Map<String, Object>> getSchemaTemplates() {
        Map<String, Object> templates = new LinkedHashMap<>();

        templates.put("users", buildTemplate("users",
            "id", "firstName", "lastName", "email", "phone",
            "address", "dateOfBirth", "username", "jobTitle", "company"));

        templates.put("transactions", buildTemplate("transactions",
            "transactionId", "fromAccount", "toAccount", "amount",
            "currency", "merchant", "category", "status", "timestamp", "description"));

        templates.put("logs", buildTemplate("logs",
            "logId", "timestamp", "level", "service", "message",
            "traceId", "userId", "httpMethod", "statusCode", "responseTimeMs"));

        templates.put("iot", buildTemplate("iot",
            "eventId", "deviceId", "deviceType", "location", "timestamp",
            "value", "unit", "status", "batteryLevel", "firmwareVersion"));

        templates.put("ecommerce", buildTemplate("ecommerce",
            "orderId", "customerId", "customerName", "customerEmail", "product",
            "orderTotal", "currency", "orderStatus", "paymentMethod",
            "shippingAddress", "orderDate", "estimatedDelivery", "trackingNumber"));

        return ResponseEntity.ok(templates);
    }

    // =========================================================================
    // Endpoint 3 — POST /api/schema/validate
    // =========================================================================

    /**
     * Validates a GenerationRequest without actually generating any data.
     *
     * Always returns HTTP 200. The result is in the body:
     *   { "valid": true,  "message": "Request is valid." }
     *   { "valid": false, "message": "Unknown data type: ..." }
     */
    @Operation(
        summary     = "Validate a generation request",
        description = "Checks that the type and format values in the request are supported. " +
                      "Always returns HTTP 200 — check the 'valid' field in the response body."
    )
    @PostMapping("/schema/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody GenerationRequest request) {
        // Valid data types accepted by GeneratorService
        List<String> validTypes   = Arrays.asList("users", "transactions", "logs", "iot", "ecommerce");
        List<String> validFormats = Arrays.asList("json", "csv", "sql", "xml");

        Map<String, Object> result = new LinkedHashMap<>();

        // Check type
        if (request.getType() == null || request.getType().isBlank()) {
            result.put("valid",   false);
            result.put("message", "type must not be blank.");
            return ResponseEntity.ok(result);
        }
        if (!validTypes.contains(request.getType().toLowerCase())) {
            result.put("valid",   false);
            result.put("message", "Unknown type: '" + request.getType() +
                                  "'. Valid types are: " + validTypes);
            return ResponseEntity.ok(result);
        }

        // Check count
        if (request.getCount() < 1 || request.getCount() > 1000) {
            result.put("valid",   false);
            result.put("message", "count must be between 1 and 1000. Got: " + request.getCount());
            return ResponseEntity.ok(result);
        }

        // Check format
        if (request.getFormat() != null && !validFormats.contains(request.getFormat().toLowerCase())) {
            result.put("valid",   false);
            result.put("message", "Unknown format: '" + request.getFormat() +
                                  "'. Valid formats are: " + validFormats);
            return ResponseEntity.ok(result);
        }

        result.put("valid",   true);
        result.put("message", "Request is valid. Ready to generate " +
                              request.getCount() + " " + request.getType() + " records.");
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // Endpoint 4 — GET /api/export/{format}
    // =========================================================================

    /**
     * Exports generated data directly as a downloadable file.
     *
     * URL examples:
     *   GET /api/export/csv?type=users&count=100
     *   GET /api/export/sql?type=transactions&count=50
     *   GET /api/export/xml?type=logs&count=25
     *   GET /api/export/json?type=ecommerce&count=10
     *
     * The response Content-Type and Content-Disposition headers are set so
     * that browsers prompt a file download for CSV and SQL.
     *
     * @param format one of: json, csv, sql, xml
     * @param type   data type (default: "users")
     * @param count  number of records (default: 10)
     */
    @Operation(
        summary     = "Export data in a specific format",
        description = "Generates data and returns it as a downloadable file in the requested format. " +
                      "CSV and SQL responses include a Content-Disposition header for file download."
    )
    @GetMapping("/export/{format}")
    public ResponseEntity<String> export(
            @PathVariable String format,
            @RequestParam(defaultValue = "users") String type,
            @RequestParam(defaultValue = "10")    int    count) {

        // Clamp count to valid range (query params bypass @Valid)
        count = Math.max(1, Math.min(count, 1000));

        List<Map<String, Object>> rawData;
        try {
            rawData = generatorService.generate(type, count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error: " + e.getMessage());
        }

        // Determine export format and build the appropriate response
        return switch (format.toLowerCase()) {

            case "json" -> {
                String body = exportService.toJson(rawData);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            }

            case "csv" -> {
                String body = exportService.toCsv(rawData);
                yield ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=dataforge-export.csv")
                        .body(body);
            }

            case "sql" -> {
                // Use the data type as the SQL table name
                String body = exportService.toSql(rawData, type.toLowerCase());
                yield ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=dataforge-export.sql")
                        .body(body);
            }

            case "xml" -> {
                // rootElement = plural type name, itemElement = singular (best effort)
                String rootElement = type.toLowerCase();
                String itemElement = singularise(type.toLowerCase());
                String body = exportService.toXml(rawData, rootElement, itemElement);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(body);
            }

            default -> ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Unknown format: '" + format +
                          "'. Valid formats are: json, csv, sql, xml");
        };
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Converts raw data into the requested format string (or keeps it as a List for JSON).
     * Used by the POST /api/generate endpoint.
     */
    private Object formatData(List<Map<String, Object>> rawData, String format, String type) {
        if (format == null) return rawData;
        return switch (format.toLowerCase()) {
            case "csv"  -> exportService.toCsv(rawData);
            case "sql"  -> exportService.toSql(rawData, type.toLowerCase());
            case "xml"  -> exportService.toXml(rawData, type.toLowerCase(),
                                               singularise(type.toLowerCase()));
            default     -> rawData;  // "json" or unrecognised → return raw list
        };
    }

    /**
     * Builds a schema template map for one data type.
     */
    private Map<String, Object> buildTemplate(String type, String... fields) {
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("type",   type);
        template.put("fields", Arrays.asList(fields));
        return template;
    }

    /**
     * Converts a plural type name to a singular form for XML item elements.
     * Handles the five specific types DataForge supports.
     */
    private String singularise(String type) {
        return switch (type) {
            case "users"        -> "user";
            case "transactions" -> "transaction";
            case "logs"         -> "log";
            case "iot"          -> "event";
            case "ecommerce"    -> "order";
            default             -> "record";
        };
    }
}
