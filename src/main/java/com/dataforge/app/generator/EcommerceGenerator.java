// =============================================================================
// EcommerceGenerator.java
// Generates realistic e-commerce order records for testing order management
// systems, analytics dashboards, and data pipelines.
//
// Each order includes:
//   - customer details (id, name, email)
//   - product details (name, category, SKU, price, quantity)
//   - order summary (total, currency, status, payment method)
//   - shipping details (address, tracking number, estimated delivery)
//
// Order status is weighted to reflect a realistic fulfilment distribution:
//   30% DELIVERED, 25% SHIPPED, 20% PROCESSING, 10% PENDING,
//   10% CANCELLED,  5% RETURNED
// =============================================================================

package com.dataforge.app.generator;

import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Spring-managed component that generates fake e-commerce order data.
 */
@Component
public class EcommerceGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    // Supported output currencies
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "INR"};

    // Payment methods common in global e-commerce
    private static final String[] PAYMENT_METHODS = {
        "Credit Card", "Debit Card", "UPI", "Net Banking", "Wallet"
    };

    // Product categories with matching product names
    private static final String[][] PRODUCT_CATALOG = {
        {"Electronics", "Wireless Headphones"},
        {"Electronics", "Bluetooth Speaker"},
        {"Electronics", "USB-C Hub"},
        {"Clothing",    "Running Shoes"},
        {"Clothing",    "Denim Jacket"},
        {"Clothing",    "Cotton T-Shirt"},
        {"Books",       "Clean Code"},
        {"Books",       "The Pragmatic Programmer"},
        {"Home",        "Air Purifier"},
        {"Home",        "Coffee Maker"},
        {"Sports",      "Yoga Mat"},
        {"Sports",      "Resistance Bands Set"},
        {"Beauty",      "Vitamin C Serum"},
        {"Beauty",      "Moisturiser SPF50"}
    };

    // Weighted order statuses
    private static final String[] STATUSES = {
        "DELIVERED",  "DELIVERED",  "DELIVERED",  "DELIVERED",  "DELIVERED",
        "DELIVERED",
        "SHIPPED",    "SHIPPED",    "SHIPPED",    "SHIPPED",    "SHIPPED",
        "PROCESSING", "PROCESSING", "PROCESSING", "PROCESSING",
        "PENDING",    "PENDING",
        "CANCELLED",  "CANCELLED",
        "RETURNED"
    };

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                         .withZone(ZoneId.of("UTC"));

    /**
     * Generates a list of fake e-commerce order records.
     *
     * @param count number of orders to generate (1–1000)
     * @return list of maps where each map represents one order
     */
    public List<Map<String, Object>> generate(int count) {
        List<Map<String, Object>> orders = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Customer info
            String firstName = faker.name().firstName();
            String lastName  = faker.name().lastName();
            String email     = firstName.toLowerCase() + "." +
                               lastName.toLowerCase()  + "@" +
                               faker.internet().domainName();

            // Pick a random product from the catalog
            String[] product  = PRODUCT_CATALOG[random.nextInt(PRODUCT_CATALOG.length)];
            String category   = product[0];
            String productName = product[1];
            int quantity      = 1 + random.nextInt(5); // 1–5 units
            double price      = round(5.0 + random.nextDouble() * 495.0); // $5–$500
            double orderTotal = round(price * quantity);

            // SKU: e.g. "ELEC-004821"
            String sku = category.substring(0, 4).toUpperCase() + "-" +
                         String.format("%06d", random.nextInt(1_000_000));

            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("name",     productName);
            productMap.put("category", category);
            productMap.put("sku",      sku);
            productMap.put("price",    price);
            productMap.put("quantity", quantity);

            // Shipping address
            Map<String, Object> shippingAddress = new LinkedHashMap<>();
            shippingAddress.put("street",  faker.address().streetAddress());
            shippingAddress.put("city",    faker.address().city());
            shippingAddress.put("state",   faker.address().state());
            shippingAddress.put("country", faker.address().country());
            shippingAddress.put("zipCode", faker.address().zipCode());

            // Order date: random day within last 60 days
            Date pastDate     = faker.date().past(60, TimeUnit.DAYS);
            String orderDate  = FORMATTER.format(pastDate.toInstant());

            // Estimated delivery: 3–7 days after order date
            int deliveryDays  = 3 + random.nextInt(5);
            String estDelivery = FORMATTER.format(
                pastDate.toInstant().plus(deliveryDays, ChronoUnit.DAYS));

            Map<String, Object> order = new LinkedHashMap<>();
            order.put("orderId",           UUID.randomUUID().toString());
            order.put("customerId",        UUID.randomUUID().toString());
            order.put("customerName",      firstName + " " + lastName);
            order.put("customerEmail",     email);
            order.put("product",           productMap);
            order.put("orderTotal",        orderTotal);
            order.put("currency",          CURRENCIES[random.nextInt(CURRENCIES.length)]);
            order.put("orderStatus",       STATUSES[random.nextInt(STATUSES.length)]);
            order.put("paymentMethod",     PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)]);
            order.put("shippingAddress",   shippingAddress);
            order.put("orderDate",         orderDate);
            order.put("estimatedDelivery", estDelivery);
            order.put("trackingNumber",    "TRK" + String.format("%010d",
                                           (long)(random.nextDouble() * 9_999_999_999L)));

            orders.add(order);
        }

        return orders;
    }

    /** Rounds a double to 2 decimal places for clean monetary output. */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
