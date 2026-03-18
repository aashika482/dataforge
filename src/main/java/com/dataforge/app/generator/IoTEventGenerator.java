// =============================================================================
// IoTEventGenerator.java
// Generates realistic IoT (Internet of Things) sensor event records.
//
// Simulates five common sensor types found in smart buildings / industry:
//   - Temperature Sensor  → readings in °C (15–45)
//   - Humidity Sensor     → readings in %  (20–90)
//   - Pressure Sensor     → readings in hPa (950–1050)
//   - Motion Detector     → binary 0 or 1
//   - Smart Meter         → energy reading in kWh (0–100)
//
// Device status is weighted to reflect a healthy fleet:
//   80% NORMAL, 15% WARNING, 5% CRITICAL
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
 * Spring-managed component that generates fake IoT sensor event data.
 */
@Component
public class IoTEventGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    // All supported device types in the same order as UNITS below
    private static final String[] DEVICE_TYPES = {
        "Temperature Sensor",
        "Humidity Sensor",
        "Pressure Sensor",
        "Motion Detector",
        "Smart Meter"
    };

    // Units matching DEVICE_TYPES by index
    private static final String[] UNITS = {"°C", "%", "hPa", "boolean", "kWh"};

    // Weighted statuses: 16 NORMAL, 3 WARNING, 1 CRITICAL per 20 slots
    private static final String[] STATUSES = {
        "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",
        "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",
        "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",  "NORMAL",
        "NORMAL",
        "WARNING", "WARNING", "WARNING",
        "CRITICAL"
    };

    // Building/room location templates
    private static final String[] BUILDINGS = {"Building A", "Building B", "Building C", "Warehouse 1", "Lab 2"};

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                         .withZone(ZoneId.of("UTC"));

    /**
     * Generates a list of fake IoT sensor event records.
     *
     * @param count number of events to generate (1–1000)
     * @return list of maps where each map represents one sensor event
     */
    public List<Map<String, Object>> generate(int count) {
        List<Map<String, Object>> events = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Pick a random device type and get its index for unit lookup
            int typeIndex  = random.nextInt(DEVICE_TYPES.length);
            String devType = DEVICE_TYPES[typeIndex];
            String unit    = UNITS[typeIndex];

            // Generate a realistic sensor reading for this device type
            Object value = generateValue(typeIndex);

            // Timestamp within the last 7 days
            Date pastDate   = faker.date().past(7, TimeUnit.DAYS);
            String timestamp = FORMATTER.format(pastDate.toInstant());

            // Location like "Building A - Room 304"
            String building = BUILDINGS[random.nextInt(BUILDINGS.length)];
            String location = building + " - Room " + (100 + random.nextInt(400));

            // Firmware version like "v1.3.7"
            String firmware = "v1." + random.nextInt(10) + "." + random.nextInt(10);

            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventId",         UUID.randomUUID().toString());
            event.put("deviceId",        String.format("DEV-%06d", random.nextInt(1_000_000)));
            event.put("deviceType",      devType);
            event.put("location",        location);
            event.put("timestamp",       timestamp);
            event.put("value",           value);
            event.put("unit",            unit);
            event.put("status",          STATUSES[random.nextInt(STATUSES.length)]);
            event.put("batteryLevel",    1 + random.nextInt(100));  // 1–100 %
            event.put("firmwareVersion", firmware);

            events.add(event);
        }

        return events;
    }

    /**
     * Generates a sensor reading appropriate for the given device type index.
     *
     * Index mapping (matches DEVICE_TYPES and UNITS arrays):
     *   0 → Temperature Sensor  → double 15.0–45.0 °C
     *   1 → Humidity Sensor     → double 20.0–90.0 %
     *   2 → Pressure Sensor     → double 950.0–1050.0 hPa
     *   3 → Motion Detector     → int 0 or 1
     *   4 → Smart Meter         → double 0.0–100.0 kWh
     */
    private Object generateValue(int typeIndex) {
        return switch (typeIndex) {
            case 0 -> round(15.0 + random.nextDouble() * 30.0);   // Temperature: 15–45 °C
            case 1 -> round(20.0 + random.nextDouble() * 70.0);   // Humidity: 20–90 %
            case 2 -> round(950.0 + random.nextDouble() * 100.0); // Pressure: 950–1050 hPa
            case 3 -> random.nextInt(2);                           // Motion: 0 or 1
            case 4 -> round(random.nextDouble() * 100.0);         // Smart Meter: 0–100 kWh
            default -> 0.0;
        };
    }

    /** Rounds a double to 2 decimal places for clean output. */
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
