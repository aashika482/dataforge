// =============================================================================
// Jenkinsfile — DataForge Jenkins Declarative Pipeline
//
// WHAT IS A JENKINSFILE?
//   A Jenkinsfile defines a CI/CD pipeline as code. Instead of clicking
//   through the Jenkins UI to configure build steps, everything is written
//   here in Groovy DSL and stored in version control alongside the app code.
//   This means pipeline changes go through code review just like app changes.
//
// DECLARATIVE vs SCRIPTED PIPELINE:
//   Jenkins supports two syntaxes. This file uses "Declarative" — the modern,
//   structured style with a fixed top-level pipeline { } block. It is easier
//   to read and has built-in validation. Scripted pipelines use pure Groovy
//   and are more flexible but harder to maintain.
//
// HOW TO USE THIS FILE:
//   1. In Jenkins → New Item → Pipeline
//   2. Under "Pipeline" → Definition: "Pipeline script from SCM"
//   3. SCM: Git, Repository URL: https://github.com/aashika482/dataforge.git
//   4. Script Path: Jenkinsfile
//   5. Save → Build with Parameters
// =============================================================================

pipeline {

    // "agent any" tells Jenkins to run this pipeline on any available agent
    // (build node). In a multi-node setup you could specify agent { label 'docker' }
    // to target a specific node that has Docker installed.
    agent any

    // =========================================================================
    // PARAMETERS
    // Parameters let you customise each run from the Jenkins UI or API call.
    // They appear as a form before each build when using "Build with Parameters".
    // =========================================================================
    parameters {
        // Which branch to build — useful for building feature branches manually
        string(
            name:         'BRANCH',
            defaultValue: 'main',
            description:  'Git branch to build (e.g. main, develop, feature/xyz)'
        )

        // Target environment — controls which deploy stage actually runs
        choice(
            name:        'ENVIRONMENT',
            choices:     ['dev', 'staging', 'prod'],
            description: 'Deployment target environment'
        )
    }

    // =========================================================================
    // ENVIRONMENT VARIABLES
    // Defined once here, available in every stage as ${ENV_VAR} or env.ENV_VAR.
    // BUILD_ID is injected automatically by Jenkins (unique per run).
    // =========================================================================
    environment {
        APP_NAME     = 'dataforge'
        VERSION      = "${env.BUILD_ID}"
        DOCKER_IMAGE = "dataforge:${env.BUILD_ID}"
    }

    // =========================================================================
    // STAGES
    // Each stage is a named phase of the pipeline. Jenkins renders them as
    // columns in the "Stage View" UI, making it easy to see which step failed.
    // =========================================================================
    stages {

        // =====================================================================
        // Stage 1: CHECKOUT
        // Clones the repository onto the Jenkins agent workspace.
        // Uses params.BRANCH so the correct branch is always checked out.
        // =====================================================================
        stage('Checkout') {
            steps {
                // Check out the selected branch from the configured SCM
                git branch: "${params.BRANCH}",
                    url: 'https://github.com/aashika482/dataforge.git'

                echo "✔ Checked out branch: ${params.BRANCH}"
                echo "  Build ID  : ${VERSION}"
                echo "  Environment: ${params.ENVIRONMENT}"
            }
        }

        // =====================================================================
        // Stage 2: CODE QUALITY
        // Runs Maven Checkstyle to detect code style violations.
        // failOnViolation=false means violations are reported but the stage
        // does not fail — appropriate during active development.
        // =====================================================================
        stage('Code Quality') {
            steps {
                echo "Running Checkstyle code quality analysis..."
                sh 'mvn checkstyle:check -Dcheckstyle.failOnViolation=false'
                echo "✔ Code quality check completed"
            }
        }

        // =====================================================================
        // Stage 3: BUILD
        // Compiles the project, packages the fat JAR, and builds the Docker
        // image tagged with the Jenkins Build ID for full traceability.
        // -DskipTests is safe here because tests run in their own stage (Stage 4).
        // =====================================================================
        stage('Build') {
            steps {
                echo "Building Maven project..."
                sh 'mvn clean package -DskipTests'
                echo "✔ Build completed: target/*.jar"

                echo "Building Docker image: ${DOCKER_IMAGE}"
                sh "docker build -t ${DOCKER_IMAGE} ."
                echo "✔ Docker image built: ${DOCKER_IMAGE}"
            }
        }

        // =====================================================================
        // Stage 4: TEST
        // Runs the full JUnit 5 test suite (27 tests: unit + integration).
        // Runs AFTER build so the JAR is already compiled — tests are faster.
        // Jenkins automatically collects surefire XML reports for display in UI.
        // =====================================================================
        stage('Test') {
            steps {
                echo "Running JUnit 5 test suite..."
                sh 'mvn test'
                echo "✔ All tests completed"
            }
            post {
                // Publish test results in the Jenkins UI (Test Results tab)
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // =====================================================================
        // Stage 5: SECURITY SCAN
        // Trivy scans the Docker image for known CVEs (Common Vulnerabilities
        // and Exposures) at CRITICAL and HIGH severity levels.
        // --exit-code 0 means Trivy prints findings but does NOT fail the stage.
        // The Docker socket mount (/var/run/docker.sock) lets the Trivy
        // container access the host's Docker daemon to inspect local images.
        // =====================================================================
        stage('Security Scan') {
            steps {
                echo "Running Trivy security scan on ${DOCKER_IMAGE}..."
                sh """
                    docker run --rm \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy image \
                        --exit-code 0 \
                        --severity HIGH,CRITICAL \
                        ${DOCKER_IMAGE}
                """
                echo "✔ Security scan completed"
            }
        }

        // =====================================================================
        // Stage 6: DEPLOY TO STAGING
        // Only runs when ENVIRONMENT == 'staging' (controlled by the parameter).
        // docker-compose up -d starts all services in detached (background) mode.
        // =====================================================================
        stage('Deploy to Staging') {
            // "when" is a Declarative Pipeline gate — the stage is skipped entirely
            // if the condition is false. No steps run, the stage shows as skipped.
            when {
                expression { params.ENVIRONMENT == 'staging' }
            }
            steps {
                echo "Deploying ${DOCKER_IMAGE} to STAGING environment..."
                sh 'docker-compose -f docker-compose.yml up -d'
                echo "✔ Staging deployment complete"
                echo "  Staging URL: http://localhost:7070/api/health"
            }
        }

        // =====================================================================
        // Stage 7: DEPLOY TO PRODUCTION
        // Only runs when ENVIRONMENT == 'prod'.
        // "input" pauses the pipeline and waits for a human to click "Deploy Now"
        // in the Jenkins UI — this is a manual approval gate that prevents
        // accidental production deploys.
        // kubectl apply deploys the Kubernetes manifests to the cluster.
        // =====================================================================
        stage('Deploy to Production') {
            when {
                expression { params.ENVIRONMENT == 'prod' }
            }
            steps {
                // Manual approval gate — pipeline pauses here until someone approves
                input message: 'Deploy to production?', ok: 'Deploy Now'

                echo "Deploying ${DOCKER_IMAGE} to PRODUCTION environment..."
                sh 'kubectl apply -f infrastructure/kubernetes/'
                echo "✔ Production deployment complete"
                echo "  API URL: http://localhost:30070/api/health"
            }
        }

    } // end stages

    // =========================================================================
    // POST — runs after all stages, regardless of outcome
    // The "post" block handles notifications, cleanup, and reporting.
    // Conditions: always, success, failure, unstable, aborted
    // =========================================================================
    post {

        success {
            echo "╔══════════════════════════════════════════╗"
            echo "║  Pipeline SUCCEEDED!                     ║"
            echo "║  DataForge ${VERSION} is ready.          ║"
            echo "╚══════════════════════════════════════════╝"
        }

        failure {
            echo "╔══════════════════════════════════════════╗"
            echo "║  Pipeline FAILED!                        ║"
            echo "║  Check the stage logs above for details. ║"
            echo "╚══════════════════════════════════════════╝"
        }

        always {
            echo "Pipeline finished. Build: ${VERSION} | Branch: ${params.BRANCH} | Env: ${params.ENVIRONMENT}"
        }

    } // end post

} // end pipeline
