// =============================================================================
// UserGenerator.java
// Generates realistic fake user/person records using the DataFaker library.
//
// WHAT IS DATAFAKER?
//   DataFaker is a Java library that produces plausible-looking fake data for
//   testing and development — names, emails, phone numbers, addresses, job
//   titles, company names, and hundreds of other categories.
//   It is the successor to the popular Java Faker library and is faster and
//   more actively maintained.
//
// HOW IT WORKS:
//   1. We create a single Faker instance (stored as a field).
//   2. We call methods like faker.name().firstName() to get a fake first name.
//   3. We build a Map<String, Object> for each record and add it to a list.
//   4. The caller (GeneratorService) receives the list and formats it.
// =============================================================================

package com.dataforge.app.generator;

import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Spring-managed component that generates fake user data.
 *
 * @Component tells Spring to create one instance of this class and keep it
 * in the application context so it can be injected wherever needed.
 */
@Component
public class UserGenerator {

    // Faker instance — thread-safe for read operations, reused across calls
    private final Faker faker = new Faker();

    /**
     * Generates a list of fake user records.
     *
     * @param count number of user records to generate (1–1000)
     * @return list of maps where each map represents one user
     */
    public List<Map<String, Object>> generate(int count) {
        List<Map<String, Object>> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Generate first/last name once so we can reuse them in email/username
            String firstName = faker.name().firstName();
            String lastName  = faker.name().lastName();

            // Build a structured address as a nested map
            Map<String, Object> address = new LinkedHashMap<>();
            address.put("street",  faker.address().streetAddress());
            address.put("city",    faker.address().city());
            address.put("state",   faker.address().state());
            address.put("zipCode", faker.address().zipCode());
            address.put("country", faker.address().country());

            // Build the user record, preserving insertion order with LinkedHashMap
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("id",          UUID.randomUUID().toString());
            user.put("firstName",   firstName);
            user.put("lastName",    lastName);
            // Construct a realistic email: john.doe@example.com
            user.put("email",       firstName.toLowerCase() + "." +
                                    lastName.toLowerCase()  + "@" +
                                    faker.internet().domainName());
            user.put("phone",       faker.phoneNumber().phoneNumber());
            user.put("address",     address);
            // Random date of birth between 18 and 70 years ago
            user.put("dateOfBirth", faker.date()
                                         .birthday(18, 70)
                                         .toInstant()
                                         .atZone(ZoneId.systemDefault())
                                         .toLocalDate()
                                         .toString());
            user.put("username",    faker.internet().username());
            user.put("jobTitle",    faker.job().title());
            user.put("company",     faker.company().name());

            users.add(user);
        }

        return users;
    }
}
