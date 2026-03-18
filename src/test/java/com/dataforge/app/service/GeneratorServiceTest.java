// =============================================================================
// GeneratorServiceTest.java
// Integration tests for the GeneratorService class.
//
// WHAT IS AN INTEGRATION TEST?
//   Unlike a unit test (which isolates one class), an integration test loads
//   part or all of the Spring application context and verifies that components
//   work correctly *together*.
//
// @SpringBootTest loads the full application context — all beans, configs,
// and component wiring — so we can @Autowire real beans instead of mocks.
// This is slower than unit tests but catches wiring errors and interaction
// bugs that unit tests cannot detect.
//
// WHY TEST GeneratorService SEPARATELY?
//   The service contains the routing logic (which generator gets called for
//   which type). We verify every route and the error case independently of
//   the HTTP layer.
// =============================================================================

package com.dataforge.app.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GeneratorService}.
 * The full Spring context is loaded so all five generator beans are wired in.
 */
@SpringBootTest
class GeneratorServiceTest {

    // Spring injects the real GeneratorService bean (with all 5 generators wired)
    @Autowired
    private GeneratorService generatorService;

    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generate with type 'users' returns a non-empty list of maps")
    void testGenerateUsers() {
        List<Map<String, Object>> result = generatorService.generate("users", 5);
        assertNotNull(result,          "Result should not be null");
        assertFalse(result.isEmpty(),  "Result should not be empty");
        assertEquals(5, result.size(), "Expected 5 user records");
    }

    @Test
    @DisplayName("generate with type 'transactions' returns non-empty list")
    void testGenerateTransactions() {
        List<Map<String, Object>> result = generatorService.generate("transactions", 5);
        assertFalse(result.isEmpty(), "Transactions list should not be empty");
    }

    @Test
    @DisplayName("generate with type 'logs' returns non-empty list")
    void testGenerateLogs() {
        List<Map<String, Object>> result = generatorService.generate("logs", 5);
        assertFalse(result.isEmpty(), "Logs list should not be empty");
    }

    @Test
    @DisplayName("generate with type 'iot' returns non-empty list")
    void testGenerateIot() {
        List<Map<String, Object>> result = generatorService.generate("iot", 5);
        assertFalse(result.isEmpty(), "IoT events list should not be empty");
    }

    @Test
    @DisplayName("generate with type 'ecommerce' returns non-empty list")
    void testGenerateEcommerce() {
        List<Map<String, Object>> result = generatorService.generate("ecommerce", 5);
        assertFalse(result.isEmpty(), "Ecommerce orders list should not be empty");
    }

    @Test
    @DisplayName("generate with unknown type throws IllegalArgumentException")
    void testGenerateUnknownTypeThrows() {
        // assertThrows verifies that the lambda throws the expected exception type
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> generatorService.generate("unknown_type", 5),
            "Expected IllegalArgumentException for unknown type"
        );
        // Also verify the error message is informative
        assertTrue(ex.getMessage().contains("unknown_type"),
            "Exception message should mention the bad type name");
    }

    @Test
    @DisplayName("generate is case-insensitive: 'USERS' works the same as 'users'")
    void testGenerateIsCaseInsensitive() {
        // Both calls should succeed and return the same number of records
        List<Map<String, Object>> lower = generatorService.generate("users",  5);
        List<Map<String, Object>> upper = generatorService.generate("USERS",  5);
        assertEquals(lower.size(), upper.size(),
            "Case-insensitive: 'users' and 'USERS' should return same count");
    }
}
