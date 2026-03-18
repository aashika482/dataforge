// =============================================================================
// UserGeneratorTest.java
// Unit tests for the UserGenerator class.
//
// WHAT IS A UNIT TEST?
//   A unit test checks one small piece of code (a "unit") in isolation.
//   Here, we test UserGenerator directly — no Spring context, no HTTP, no database.
//   We just create an instance, call generate(), and assert what we expect.
//
// WHY WRITE UNIT TESTS?
//   - They run fast (milliseconds, not seconds)
//   - They catch bugs early — before code reaches production
//   - They serve as living documentation: reading the tests tells you exactly
//     what the class is supposed to do
//   - They make refactoring safer: change the internals, the tests verify
//     the output still looks right
//
// JUNIT 5 ANNOTATIONS USED:
//   @Test        — marks a method as a test case
//   @DisplayName — gives the test a human-readable name in the report
// =============================================================================

package com.dataforge.app.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserGenerator}.
 * No Spring context is loaded — the generator is instantiated directly.
 */
class UserGeneratorTest {

    // The class under test — created fresh before each test method
    private UserGenerator generator;

    @BeforeEach
    void setUp() {
        // @BeforeEach runs before every @Test method, so each test starts clean
        generator = new UserGenerator();
    }

    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generate(10) returns exactly 10 records")
    void testGenerateReturnsCorrectCount() {
        List<Map<String, Object>> result = generator.generate(10);
        assertEquals(10, result.size(), "Expected 10 user records");
    }

    @Test
    @DisplayName("each user record contains all required fields")
    void testEachUserHasRequiredFields() {
        List<Map<String, Object>> result = generator.generate(5);

        for (Map<String, Object> user : result) {
            // Assert that every required key is present in the map
            assertTrue(user.containsKey("id"),        "Missing field: id");
            assertTrue(user.containsKey("firstName"),  "Missing field: firstName");
            assertTrue(user.containsKey("lastName"),   "Missing field: lastName");
            assertTrue(user.containsKey("email"),      "Missing field: email");
            assertTrue(user.containsKey("phone"),      "Missing field: phone");
            assertTrue(user.containsKey("username"),   "Missing field: username");
            assertTrue(user.containsKey("jobTitle"),   "Missing field: jobTitle");
            assertTrue(user.containsKey("company"),    "Missing field: company");
        }
    }

    @Test
    @DisplayName("generate(1) returns exactly 1 record")
    void testGenerateWithCountOne() {
        List<Map<String, Object>> result = generator.generate(1);
        assertEquals(1, result.size(), "Expected exactly 1 user record");
    }

    @Test
    @DisplayName("generate(100) returns exactly 100 records")
    void testGenerateWithCountOneHundred() {
        List<Map<String, Object>> result = generator.generate(100);
        assertEquals(100, result.size(), "Expected 100 user records");
    }

    @Test
    @DisplayName("email field is not null and not empty for every record")
    void testEmailIsNotNullOrEmpty() {
        List<Map<String, Object>> result = generator.generate(20);

        for (Map<String, Object> user : result) {
            Object email = user.get("email");
            assertNotNull(email, "email should not be null");
            assertFalse(email.toString().isBlank(), "email should not be blank");
        }
    }
}
