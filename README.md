# DataForge - Synthetic Test Data Generator

Student Name: Aashika M
Registration No: 23FE10CSE00482
Course: CSE3253 DevOps [PE6]
Semester: VI (2025-2026)
Project Type: Test Data Generator
Difficulty: Intermediate

---

## Project Overview

### Problem Statement
Developers and QA engineers frequently need large volumes of realistic test data to validate applications, run load tests, and simulate production scenarios. Creating this data manually is time-consuming and error-prone. DataForge solves this by generating synthetic, schema-aware test data on demand across multiple data types and export formats.

### Objectives
- [ ] Build a Spring Boot REST API that generates realistic synthetic test data using Java DataFaker
- [ ] Support 5 data types: Users, Transactions, Logs, IoT Events, and Ecommerce Orders
- [ ] Export generated data in JSON, CSV, SQL INSERT, and XML formats
- [ ] Provide a React web UI as the user interface
- [ ] Containerize the application using Docker and orchestrate with Kubernetes
- [ ] Implement a full CI/CD pipeline using GitHub Actions and Jenkins
- [ ] Set up monitoring with Nagios and Spring Boot Actuator

### Key Features
- Generate up to 1,000 rows of realistic fake data per request
- 5 built-in data type templates: Users, Transactions, Logs, IoT Events, Ecommerce Orders
- Export in 4 formats: JSON, CSV, SQL INSERT statements, and XML
- Interactive React web UI with live preview of first 10 rows before download
- Auto-generated API documentation via Swagger UI at /swagger-ui.html
- Dockerized with multi-stage builds for a minimal production image
- Full CI/CD pipeline with automated testing and Trivy security scanning

---

## Technology Stack

### Core Technologies
- Programming Language: Java 17
- Framework: Spring Boot 3
- Build Tool: Maven 3.9
- Frontend: React 18, Axios

### DevOps Tools
- Version Control: Git
- CI/CD: Jenkins + GitHub Actions
- Containerization: Docker
- Orchestration: Kubernetes
- Configuration Management: Puppet
- Infrastructure as Code: Terraform
- Monitoring: Nagios + Spring Boot Actuator
- Security Scanning: Trivy

---

## Getting Started

### Prerequisites
- [ ] Docker Desktop v20.10+
- [ ] Git 2.30+
- [ ] Java 17+
- [ ] Maven 3.9+
- [ ] Node.js 18+

### Installation

1. Clone the repository:
```bash
   git clone https://github.com/aashika482/dataforge.git
   cd dataforge
```

2. Build and run using Docker:
```bash
   docker-compose up --build
```

3. Access the application:
   - Web UI: http://localhost:3000
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Nagios: http://localhost:8081

### Alternative Installation (Without Docker)

Backend:
```bash
mvn clean install
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
npm install
npm start
```

---

## CI/CD Pipeline

### Pipeline Stages
1. Code Quality Check - Linting and static analysis
2. Build - Maven build and Docker image creation
3. Test - JUnit unit, integration, and Selenium tests
4. Security Scan - Trivy vulnerability scanning
5. Deploy to Staging - Automatic on push to develop branch
6. Deploy to Production - Manual approval required

---

## Testing

### Test Types
- Unit Tests: `mvn test`
- Integration Tests: `mvn verify`
- E2E Tests: Selenium-based UI tests in tests/selenium/

---

## Monitoring & Logging

### Monitoring Setup
- Nagios: HTTP health checks on port 8080
- Spring Actuator: /actuator/health and /actuator/prometheus
- Alerts: Email notifications on service down
- Logging: Structured JSON logs, 30 day retention

---

## Docker & Kubernetes
```bash
# Build image
docker build -t dataforge:latest .

# Run container
docker run -p 8080:8080 dataforge:latest

# Apply K8s manifests
kubectl apply -f infrastructure/kubernetes/

# Check deployment status
kubectl get pods,svc,deploy
```

---

## Performance Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Build Time | < 5 min | TBD |
| Test Coverage | > 80% | TBD |
| Deployment Frequency | Daily | TBD |
| Mean Time to Recovery | < 1 hour | TBD |

---

## Security

- [ ] Input validation and sanitization
- [ ] Environment-based configuration
- [ ] Regular dependency updates
- [ ] Trivy image scanning in CI pipeline
```bash
# Run security scan
trivy image dataforge:latest
```

---

## Git Branching Strategy
main
├── develop
│   ├── feature/generator-engine
│   ├── feature/react-ui
│   └── feature/ci-cd-pipeline
└── release/v1.0.0

### Commit Convention
- feat: New feature
- fix: Bug fix
- docs: Documentation
- test: Test-related
- refactor: Code refactoring
- chore: Maintenance tasks



---

## Documentation
- [User Guide](docs/user-guide.md)
- [API Documentation](docs/api-documentation.md)
- [Design Document](docs/design-document.md)
- [Project Plan](docs/project-plan.md)
