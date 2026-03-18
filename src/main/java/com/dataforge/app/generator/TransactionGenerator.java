// =============================================================================
// TransactionGenerator.java
// Generates realistic fake financial transaction records.
//
// Each transaction simulates a bank/payment transfer with:
//   - sender and receiver account numbers
//   - amount, currency, merchant, and category
//   - a weighted status (most transactions complete successfully)
//   - a timestamp somewhere in the last 90 days
// =============================================================================

package com.dataforge.app.generator;

import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Spring-managed component that generates fake financial transaction data.
 */
@Component
public class TransactionGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    // Possible currencies for transactions
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "INR", "JPY"};

    // Spending categories
    private static final String[] CATEGORIES = {
        "Food", "Travel", "Shopping", "Healthcare", "Entertainment"
    };

    // Transaction statuses with weighted distribution
    // 70% COMPLETED, 15% PENDING, 10% FAILED, 5% REVERSED
    private static final String[] STATUSES = {
        "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED",
        "COMPLETED", "COMPLETED",
        "PENDING", "PENDING",
        "FAILED",
        "REVERSED"
    };

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                         .withZone(ZoneId.of("UTC"));

    /**
     * Generates a list of fake transaction records.
     *
     * @param count number of transactions to generate (1–1000)
     * @return list of maps where each map represents one transaction
     */
    public List<Map<String, Object>> generate(int count) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Random amount between 1.00 and 9999.99
            double rawAmount = 1.0 + (random.nextDouble() * 9998.99);
            BigDecimal amount = BigDecimal.valueOf(rawAmount).setScale(2, RoundingMode.HALF_UP);

            // Random timestamp within the last 90 days
            Date pastDate = faker.date().past(90, TimeUnit.DAYS);
            String timestamp = FORMATTER.format(pastDate.toInstant());

            Map<String, Object> tx = new LinkedHashMap<>();
            tx.put("transactionId", UUID.randomUUID().toString());
            // 10-digit account numbers
            tx.put("fromAccount",   String.format("%010d", (long)(random.nextDouble() * 9_999_999_999L)));
            tx.put("toAccount",     String.format("%010d", (long)(random.nextDouble() * 9_999_999_999L)));
            tx.put("amount",        amount.doubleValue());
            tx.put("currency",      CURRENCIES[random.nextInt(CURRENCIES.length)]);
            tx.put("merchant",      faker.company().name());
            tx.put("category",      CATEGORIES[random.nextInt(CATEGORIES.length)]);
            // Pick from weighted array — COMPLETED appears 7 out of 11 times (~64%)
            tx.put("status",        STATUSES[random.nextInt(STATUSES.length)]);
            tx.put("timestamp",     timestamp);
            tx.put("description",   "Payment to " + faker.company().name());

            transactions.add(tx);
        }

        return transactions;
    }
}
