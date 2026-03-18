// =============================================================================
// LogGenerator.java
// Generates realistic application log entries for testing log pipelines,
// dashboards (Kibana, Grafana), and monitoring tools (Nagios, Prometheus).
//
// Log levels are weighted to reflect a healthy production system:
//   60% INFO  — normal operations
//   20% WARN  — something unusual but not breaking
//   15% ERROR — something went wrong
//    5% DEBUG — verbose diagnostic info
//
// Messages are tailored to the level so the data looks believable.
// =============================================================================

package com.dataforge.app.generator;

import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Spring-managed component that generates fake application log entries.
 */
@Component
public class LogGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    // Microservice names that might appear in a realistic system
    private static final String[] SERVICES = {
        "auth-service", "payment-service", "user-service",
        "api-gateway", "notification-service"
    };

    // HTTP methods for request logs
    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE"};

    // Weighted log levels: 12 INFO, 4 WARN, 3 ERROR, 1 DEBUG out of 20 slots
    private static final String[] LEVELS = {
        "INFO",  "INFO",  "INFO",  "INFO",  "INFO",  "INFO",
        "INFO",  "INFO",  "INFO",  "INFO",  "INFO",  "INFO",
        "WARN",  "WARN",  "WARN",  "WARN",
        "ERROR", "ERROR", "ERROR",
        "DEBUG"
    };

    // Status codes grouped by log level for realistic pairing
    private static final Map<String, int[]> STATUS_CODES = new HashMap<>();
    static {
        STATUS_CODES.put("INFO",  new int[]{200, 201});
        STATUS_CODES.put("WARN",  new int[]{400, 401, 403, 404});
        STATUS_CODES.put("ERROR", new int[]{500, 503});
        STATUS_CODES.put("DEBUG", new int[]{200, 201, 400});
    }

    // Realistic log messages keyed by level
    private static final Map<String, String[]> MESSAGES = new HashMap<>();
    static {
        MESSAGES.put("INFO", new String[]{
            "Request processed successfully",
            "User authenticated",
            "Database connection established",
            "Cache hit for key: session_%s",
            "Scheduled job completed in %dms"
        });
        MESSAGES.put("WARN", new String[]{
            "High memory usage detected: %d%%",
            "Slow query detected (%dms)",
            "Retry attempt %d for external API call",
            "JWT token expiring soon for user %s",
            "Connection pool running low: %d remaining"
        });
        MESSAGES.put("ERROR", new String[]{
            "NullPointerException in method processPayment()",
            "Database connection timeout after %dms",
            "Failed to send notification: SMTP error",
            "Unauthorized access attempt from IP %s",
            "Service %s is unreachable"
        });
        MESSAGES.put("DEBUG", new String[]{
            "Entering method: validateToken()",
            "SQL query: SELECT * FROM users WHERE id = %s",
            "Request headers: Content-Type=application/json",
            "Response payload size: %d bytes"
        });
    }

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                         .withZone(ZoneId.of("UTC"));

    /**
     * Generates a list of fake log entries.
     *
     * @param count number of log entries to generate (1–1000)
     * @return list of maps where each map represents one log line
     */
    public List<Map<String, Object>> generate(int count) {
        List<Map<String, Object>> logs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String level   = LEVELS[random.nextInt(LEVELS.length)];
            String service = SERVICES[random.nextInt(SERVICES.length)];

            // Recent timestamp — within the last 24 hours, entries are roughly sequential
            Date pastDate  = faker.date().past(1, TimeUnit.DAYS);
            String timestamp = FORMATTER.format(pastDate.toInstant());

            // Pick a status code appropriate for this log level
            int[] codes       = STATUS_CODES.get(level);
            int statusCode    = codes[random.nextInt(codes.length)];

            // Pick a message template and fill in any %s / %d placeholders
            String[] msgTemplates = MESSAGES.get(level);
            String msgTemplate    = msgTemplates[random.nextInt(msgTemplates.length)];
            String message        = formatMessage(msgTemplate);

            // HTTP method and userId are null for ~30% of logs (system/background tasks)
            boolean isHttpLog = random.nextDouble() > 0.3;

            Map<String, Object> log = new LinkedHashMap<>();
            log.put("logId",          UUID.randomUUID().toString());
            log.put("timestamp",      timestamp);
            log.put("level",          level);
            log.put("service",        service);
            log.put("message",        message);
            log.put("traceId",        generateTraceId());
            log.put("userId",         isHttpLog ? UUID.randomUUID().toString() : null);
            log.put("httpMethod",     isHttpLog ? HTTP_METHODS[random.nextInt(HTTP_METHODS.length)] : null);
            log.put("statusCode",     statusCode);
            log.put("responseTimeMs", 10 + random.nextInt(1991)); // 10–2000 ms

            logs.add(log);
        }

        return logs;
    }

    /**
     * Fills placeholder tokens in a message template with random values.
     * Handles %d (integer) and %s (string) format specifiers.
     */
    private String formatMessage(String template) {
        // Replace each %d with a random number and %s with a random short string
        return template
            .replace("%d", String.valueOf(random.nextInt(1000)))
            .replace("%s", faker.internet().uuid().substring(0, 8));
    }

    /**
     * Generates a random 32-character hex trace ID (like those used by Jaeger/Zipkin).
     */
    private String generateTraceId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }
}
