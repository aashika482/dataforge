// =============================================================================
// DataGeneratorControllerTest.java
// Integration tests for the DataGeneratorController REST endpoints.
//
// WHAT IS MockMvc?
//   MockMvc is a Spring Test utility that lets you simulate HTTP requests
//   against your controllers *without* starting a real network socket.
//   It runs entirely in-memory, so tests are fast but still exercise the
//   full request-handling pipeline: request parsing → validation → controller
//   → serialisation → response.
//
// @SpringBootTest  — loads the full application context
// @AutoConfigureMockMvc — creates and injects a MockMvc instance automatically
//
// HOW TO READ A MockMvc TEST:
//   mockMvc.perform(get("/api/health"))   ← "send a GET to /api/health"
//          .andExpect(status().isOk())    ← "assert the response status is 200"
//          .andExpect(jsonPath("$.status").value("UP")); ← "assert a JSON field"
//
//   jsonPath uses dot notation: "$.status" means the top-level "status" field.
//   "$.data.length()" means the length of the "data" array in the response.
// =============================================================================

package com.dataforge.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the REST API layer.
 * MockMvc simulates HTTP calls without starting a real TCP server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DataGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // POST /api/generate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/generate with valid request returns HTTP 200")
    void testGenerateReturns200() throws Exception {
        String requestBody = """
                {
                  "type": "users",
                  "count": 5,
                  "format": "json"
                }
                """;

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/generate with type 'users' and count 5 returns 5 records")
    void testGenerateReturnsCorrectCount() throws Exception {
        String requestBody = """
                {
                  "type": "users",
                  "count": 5,
                  "format": "json"
                }
                """;

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                // $.count is the count field in GenerationResponse
                .andExpect(jsonPath("$.count").value(5))
                // $.data is the array of generated records
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    // -------------------------------------------------------------------------
    // GET /api/schema/templates
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/schema/templates returns 200 with non-empty body")
    void testSchemaTemplatesReturns200() throws Exception {
        mockMvc.perform(get("/api/schema/templates"))
                .andExpect(status().isOk())
                // The body should contain entries for all 5 data types
                .andExpect(jsonPath("$.users").exists())
                .andExpect(jsonPath("$.transactions").exists())
                .andExpect(jsonPath("$.logs").exists())
                .andExpect(jsonPath("$.iot").exists())
                .andExpect(jsonPath("$.ecommerce").exists());
    }

    // -------------------------------------------------------------------------
    // GET /api/health
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/health returns 200 with status UP")
    void testHealthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("DataForge"));
    }

    // -------------------------------------------------------------------------
    // GET /api/export/{format}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/export/json with type users and count 5 returns 200")
    void testExportJsonReturns200() throws Exception {
        mockMvc.perform(get("/api/export/json")
                .param("type",  "users")
                .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/export/csv returns content-type text/csv")
    void testExportCsvReturnsCsvContentType() throws Exception {
        mockMvc.perform(get("/api/export/csv")
                .param("type",  "users")
                .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    // -------------------------------------------------------------------------
    // Validation / error cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/generate with count 0 returns HTTP 400 (validation failure)")
    void testGenerateWithCountZeroReturns400() throws Exception {
        // count=0 violates @Min(1) on GenerationRequest — Spring returns 400
        String requestBody = """
                {
                  "type": "users",
                  "count": 0,
                  "format": "json"
                }
                """;

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/generate with invalid type returns HTTP 400")
    void testGenerateWithInvalidTypeReturns400() throws Exception {
        // "invalid_type" is not one of the 5 supported types — controller returns 400
        String requestBody = """
                {
                  "type": "invalid_type",
                  "count": 5,
                  "format": "json"
                }
                """;

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
