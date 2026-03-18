// =============================================================================
// TransactionGeneratorTest.java
// Unit tests for the TransactionGenerator class.
//
// These tests verify that generated transactions have the correct structure
// and that field values fall within the expected domain (e.g. valid statuses,
// valid currency codes). This type of check is called "contract testing" —
// we are testing that the output matches a known schema.
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
 * Unit tests for {@link TransactionGenerator}.
 */
class TransactionGeneratorTest {

    private TransactionGenerator generator;

    // Valid domain values — must match what TransactionGenerator produces
    private static final Set<String> VALID_STATUSES   = Set.of("PENDING", "COMPLETED", "FAILED", "REVERSED");
    private static final Set<String> VALID_CURRENCIES  = Set.of("USD", "EUR", "GBP", "INR", "JPY");

    @BeforeEach
    void setUp() {
        generator = new TransactionGenerator();
    }

    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generate(10) returns exactly 10 transaction records")
    void testGenerateReturnsCorrectCount() {
        List<Map<String, Object>> result = generator.generate(10);
        assertEquals(10, result.size(), "Expected 10 transaction records");
    }

    @Test
    @DisplayName("each transaction has all required fields")
    void testEachTransactionHasRequiredFields() {
        List<Map<String, Object>> result = generator.generate(5);

        for (Map<String, Object> tx : result) {
            assertTrue(tx.containsKey("transactionId"), "Missing field: transactionId");
            assertTrue(tx.containsKey("amount"),        "Missing field: amount");
            assertTrue(tx.containsKey("currency"),      "Missing field: currency");
            assertTrue(tx.containsKey("status"),        "Missing field: status");
            assertTrue(tx.containsKey("merchant"),      "Missing field: merchant");
        }
    }

    @Test
    @DisplayName("status is always one of: PENDING, COMPLETED, FAILED, REVERSED")
    void testStatusIsValid() {
        // Generate a large batch to hit all possible status values
        List<Map<String, Object>> result = generator.generate(50);

        for (Map<String, Object> tx : result) {
            String status = (String) tx.get("status");
            assertTrue(VALID_STATUSES.contains(status),
                "Unexpected status value: " + status);
        }
    }

    @Test
    @DisplayName("currency is always one of: USD, EUR, GBP, INR, JPY")
    void testCurrencyIsValid() {
        List<Map<String, Object>> result = generator.generate(50);

        for (Map<String, Object> tx : result) {
            String currency = (String) tx.get("currency");
            assertTrue(VALID_CURRENCIES.contains(currency),
                "Unexpected currency value: " + currency);
        }
    }
}
