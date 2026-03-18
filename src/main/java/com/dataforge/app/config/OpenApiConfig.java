// =============================================================================
// OpenApiConfig.java
// Configures the OpenAPI (Swagger) documentation for the DataForge API.
//
// SpringDoc auto-scans @RestController classes and builds an OpenAPI spec,
// but this file lets us customise the top-level metadata shown in Swagger UI
// (title, version, description, contact info, etc.).
//
// After starting the app, visit: http://localhost:8080/swagger-ui.html
// =============================================================================

package com.dataforge.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that registers a custom OpenAPI bean.
 * SpringDoc picks this bean up automatically and uses it to populate
 * the header section of the generated API documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the top-level metadata for the Swagger UI documentation page.
     *
     * @return a fully configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI dataForgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        // Title displayed at the top of the Swagger UI page
                        .title("DataForge API")
                        // API version shown next to the title
                        .version("1.0.0")
                        // Short description of what the API does
                        .description("Synthetic Test Data Generator API — "
                                + "generate realistic fake datasets in JSON, CSV, or XML format.")
                );
    }
}
