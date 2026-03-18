# =============================================================================
# Dockerfile — DataForge Multi-Stage Build
#
# WHAT IS A MULTI-STAGE BUILD?
#   A Docker image is built in layers. In a naive single-stage build you would
#   install Maven, the JDK, all build tools, and all source files — and all of
#   that would end up in the final image that runs in production. That makes the
#   image huge and increases the attack surface.
#
#   A multi-stage build uses multiple FROM instructions. Each stage can copy
#   files from a previous stage. Only the final stage becomes the actual image.
#
# WHY WE USE TWO STAGES:
#   Stage 1 (build)   — has Maven + full JDK. Compiles the code and produces
#                        a fat JAR. This stage is discarded afterward.
#   Stage 2 (runtime) — only has the JRE (no compiler, no Maven, no source).
#                        It copies just the JAR from Stage 1.
#
# RESULT: a production image that is ~200 MB instead of ~600 MB, with no
# build tooling exposed to the outside world.
# =============================================================================


# -----------------------------------------------------------------------------
# Stage 1 — BUILD
# Uses the official Maven image which already includes Maven 3.9 and JDK 17.
# This stage compiles the code and packages it into an executable JAR.
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# --- Dependency caching trick ---
# Copy ONLY pom.xml first and download all dependencies.
# Docker caches each layer. As long as pom.xml doesn't change, this layer is
# reused on the next build — saving several minutes of download time.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source code (changes more often than pom.xml)
COPY src ./src

# Build the fat JAR. -DskipTests skips test execution during image build
# (tests are run separately in the CI pipeline).
RUN mvn clean package -DskipTests -B


# -----------------------------------------------------------------------------
# Stage 2 — RUNTIME
# Uses a minimal JRE-only Alpine image. Alpine Linux is ~5 MB; the full JRE
# adds ~180 MB. The result is far smaller than a JDK-based image.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Metadata label — visible via "docker inspect dataforge"
LABEL maintainer="Aashika M"

WORKDIR /app

# Copy only the compiled JAR from the build stage — nothing else comes with it
COPY --from=build /app/target/*.jar app.jar

# Tell Docker (and human readers) that the app listens on port 7070.
# This does NOT publish the port — that is done in docker-compose.yml.
EXPOSE 7070

# ENTRYPOINT is the command that runs when the container starts.
# Using the exec form (JSON array) avoids a shell wrapper and correctly
# forwards OS signals (SIGTERM) so Spring Boot can shut down gracefully.
ENTRYPOINT ["java", "-jar", "app.jar"]
