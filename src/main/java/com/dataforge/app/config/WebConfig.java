// =============================================================================
// WebConfig.java
// Configures Cross-Origin Resource Sharing (CORS) for the DataForge API.
//
// WHAT IS CORS?
//   Browsers enforce a "same-origin policy": a web page served from
//   http://localhost:3000 (React dev server) is NOT allowed to call an API at
//   http://localhost:7070 (Spring Boot) by default — the origins differ.
//   CORS is a mechanism that lets the *server* tell the browser: "it's OK,
//   I trust requests coming from these other origins."
//
// WHY DO WE NEED IT?
//   Our React frontend runs on port 3000 or 3001 during development and talks
//   to this backend on port 7070. Without CORS configuration the browser blocks
//   every API call and the UI shows network errors.
// =============================================================================

package com.dataforge.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration class.
 * Implements {@link WebMvcConfigurer} so we can override specific MVC settings
 * without replacing Spring's entire auto-configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers CORS mappings so the React frontend can call the API.
     *
     * We explicitly allow both port 3000 and 3001 because:
     *   - "npm start" uses 3000 by default
     *   - If 3000 is already taken, Create React App automatically falls back to 3001
     *
     * @param registry the CORS registry provided by Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            // Apply this CORS policy to every endpoint in the application
            .addMapping("/**")
            // Allow requests from the React dev server on port 3000 or 3001
            // In production, replace these with your actual deployed frontend URL
            .allowedOrigins(
                "http://localhost:3000",
                "http://localhost:3001"
            )
            // Allow the HTTP methods used by the API
            .allowedMethods("GET", "POST")
            // Allow all request headers (Content-Type, Authorization, etc.)
            .allowedHeaders("*");
    }
}
