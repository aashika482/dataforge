// =============================================================================
// DataForgeApplication.java
// Entry point for the DataForge Spring Boot application.
// This class bootstraps the entire application: it starts the embedded Tomcat
// server, loads all Spring beans, and begins listening for HTTP requests.
// =============================================================================

package com.dataforge.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration       : marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration : tells Spring Boot to auto-configure beans
 *                               based on dependencies on the classpath
 *   - @ComponentScan       : scans this package (and sub-packages) for
 *                            @Component, @Service, @Repository, @Controller, etc.
 */
@SpringBootApplication
public class DataForgeApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps and launches the application
        SpringApplication.run(DataForgeApplication.class, args);
    }
}
