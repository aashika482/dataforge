// =============================================================================
// GeneratorService.java
// The SERVICE layer — the brain between the controller and the generators.
//
// WHAT IS A SERVICE LAYER?
//   In a well-structured Spring Boot app, code is split into layers:
//
//   Controller  →  receives the HTTP request, validates input, sends response
//   Service     →  contains the business logic (what this file does)
//   Repository  →  talks to the database (not needed here — we generate data)
//   Generator   →  the actual data-production code
//
// WHY BOTHER WITH A SERVICE LAYER?
//   The controller should be thin — it shouldn't know *how* data is generated,
//   just *when* to generate it and *what* to return. By putting the "which
//   generator do I pick?" logic here, we keep things easy to test and change.
//
// HOW IT WORKS:
//   1. The controller calls generate(type, count).
//   2. We switch on the type string to pick the right generator.
//   3. We return the list of records back to the controller.
// =============================================================================

package com.dataforge.app.service;

import com.dataforge.app.generator.EcommerceGenerator;
import com.dataforge.app.generator.IoTEventGenerator;
import com.dataforge.app.generator.LogGenerator;
import com.dataforge.app.generator.TransactionGenerator;
import com.dataforge.app.generator.UserGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Spring-managed service that routes generation requests to the correct generator.
 *
 * @Service tells Spring to create one instance and make it available for injection.
 * We prefer constructor injection (below) over @Autowired on fields because it makes
 * dependencies explicit and the class easier to unit-test.
 */
@Service
public class GeneratorService {

    // -------------------------------------------------------------------------
    // Injected generators — one per supported data type
    // -------------------------------------------------------------------------
    private final UserGenerator        userGenerator;
    private final TransactionGenerator transactionGenerator;
    private final LogGenerator         logGenerator;
    private final IoTEventGenerator    iotEventGenerator;
    private final EcommerceGenerator   ecommerceGenerator;

    /**
     * Constructor injection.
     * Spring automatically provides the five generator beans when it builds this service.
     */
    public GeneratorService(UserGenerator        userGenerator,
                            TransactionGenerator transactionGenerator,
                            LogGenerator         logGenerator,
                            IoTEventGenerator    iotEventGenerator,
                            EcommerceGenerator   ecommerceGenerator) {
        this.userGenerator        = userGenerator;
        this.transactionGenerator = transactionGenerator;
        this.logGenerator         = logGenerator;
        this.iotEventGenerator    = iotEventGenerator;
        this.ecommerceGenerator   = ecommerceGenerator;
    }

    // -------------------------------------------------------------------------
    // Core business method
    // -------------------------------------------------------------------------

    /**
     * Generates the requested number of records for the given data type.
     *
     * @param type  one of: "users", "transactions", "logs", "iot", "ecommerce"
     * @param count number of records (1–1000, already validated by the controller)
     * @return list of records, each represented as a key-value map
     * @throws IllegalArgumentException if the type is not recognised
     */
    public List<Map<String, Object>> generate(String type, int count) {
        // toLowerCase() makes matching case-insensitive ("Users" == "users")
        return switch (type.toLowerCase()) {
            case "users"        -> userGenerator.generate(count);
            case "transactions" -> transactionGenerator.generate(count);
            case "logs"         -> logGenerator.generate(count);
            case "iot"          -> iotEventGenerator.generate(count);
            case "ecommerce"    -> ecommerceGenerator.generate(count);

            // The default branch covers any unrecognised type string.
            // The controller catches this and returns HTTP 400 to the client.
            default -> throw new IllegalArgumentException(
                "Unknown data type: " + type +
                ". Valid types are: users, transactions, logs, iot, ecommerce"
            );
        };
    }
}
