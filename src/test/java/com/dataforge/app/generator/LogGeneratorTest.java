// =============================================================================
// LogGeneratorTest.java
// Unit tests for the LogGenerator class.
//
// Log generators are especially important to test because they use weighted
// random selection (e.g. 60% INFO, 20% WARN …). These tests confirm that:
//   1. The structure is correct (required fields present)
//   2. The domain values are constrained (only valid log levels)
//
// We generate a large batch (100 records) so that the random selection has
// enough samples to exercise all branches, reducing flakiness.
// =============================================================================

package com.dataforge.app.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LogGenerator}.
 */
class LogGeneratorTest {

    private LogGenerator generator;

    // The only valid log levels the generator should ever produce
    private static final Set<String> VALID_LEVELS = Set.of("INFO", "WARN", "ERROR", "DEBUG");

    @BeforeEach
    void setUp() {
        generator = new LogGenerator();
    }

    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generate(10) returns exactly 10 log records")
    void testGenerateReturnsCorrectCount() {
        List<Map<String, Object>> result = generator.generate(10);
        assertEquals(10, result.size(), "Expected 10 log entries");
    }

    @Test
    @DisplayName("each log entry has all required fields")
    void testEachLogHasRequiredFields() {
        List<Map<String, Object>> result = generator.generate(5);

        for (Map<String, Object> log : result) {
            assertTrue(log.containsKey("logId"),     "Missing field: logId");
            assertTrue(log.containsKey("timestamp"), "Missing field: timestamp");
            assertTrue(log.containsKey("level"),     "Missing field: level");
            assertTrue(log.containsKey("service"),   "Missing field: service");
            assertTrue(log.containsKey("message"),   "Missing field: message");
        }
    }

    @Test
    @DisplayName("log level is always one of: INFO, WARN, ERROR, DEBUG")
    void testLogLevelIsValid() {
        // Use 100 records to exercise the weighted random level selection
        List<Map<String, Object>> result = generator.generate(100);

        for (Map<String, Object> log : result) {
            String level = (String) log.get("level");
            assertTrue(VALID_LEVELS.contains(level),
                "Unexpected log level: " + level);
        }
    }
}
