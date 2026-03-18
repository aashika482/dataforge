# DataForge — GitHub Actions CI/CD Pipeline

## What is CI/CD?

**CI — Continuous Integration**
Every time a developer pushes code, the pipeline automatically:
- Compiles the project
- Runs all tests
- Checks code quality

This catches bugs within minutes instead of days. If a push breaks something,
GitHub immediately marks the commit as failed and notifies the team.

**CD — Continuous Delivery/Deployment**
After CI passes, the pipeline automatically delivers the tested build to
staging or production environments — no manual steps, no forgotten config.

---

## Pipeline Flow

```
Push to GitHub
      │
      ▼
  [1] test ──────────────────────────────────┐
      │                                       │
      ├──── [2] build ──── [4] security-scan  │
      │                          │            │
      └──── [3] code-quality     │            │
                                 ▼            │
                        [5] deploy-staging ◄──┘
                         (develop branch only)
                                 │
                                 ▼
                        [6] deploy-production
                          (main branch only)
```

---

## Jobs Explained

| Job | Runs on | Purpose |
|---|---|---|
| `test` | Every push | Runs all 27 JUnit 5 tests |
| `build` | After test passes | Compiles JAR + builds Docker image |
| `code-quality` | After test passes | Checkstyle analysis (parallel with build) |
| `security-scan` | After build | Trivy scans Docker image for CVEs |
| `deploy-staging` | `develop` branch only | Simulates staging deployment |
| `deploy-production` | `main` branch only | Simulates production deployment |

### Job 1 — test
Runs `mvn test` and uploads test reports as downloadable artifacts.
This is the gate — nothing else runs if tests fail.

### Job 2 — build
Runs `mvn clean package -DskipTests` and `docker build`.
The Docker image is tagged with the git commit SHA (e.g. `dataforge:a1b2c3d`),
so every build is uniquely traceable.

### Job 3 — code-quality
Runs Maven Checkstyle. Violations are reported but do not fail the pipeline
(`-Dcheckstyle.failOnViolation=false`) — useful during active development.

### Job 4 — security-scan
Trivy (by Aqua Security) scans the Docker image for CRITICAL and HIGH CVEs.
Results are printed in the log. `exit-code: 0` means findings are reported
but don't block the pipeline.

### Job 5 — deploy-staging
Only triggers on the `develop` branch. In a real project this would SSH into
a staging server or call a Kubernetes rolling update. Here it prints deployment
info to demonstrate the flow.

### Job 6 — deploy-production
Only triggers on `main`. Requires the `production` environment to be configured
in GitHub (Settings → Environments) where a required reviewer must approve
before the job runs — preventing accidental production deploys.

---

## How to View Pipeline Runs on GitHub

1. Go to your repository: `https://github.com/aashika482/dataforge`
2. Click the **Actions** tab
3. Click any workflow run to see its status
4. Click a job name (e.g. "Run Tests") to expand its logs
5. Click **Artifacts** at the bottom of a run to download test reports or JARs

---

## Triggering the Pipeline

The pipeline triggers automatically on:
- `git push origin main`
- `git push origin develop`
- Opening a Pull Request targeting `main`

No manual action needed — every push triggers CI.

---

## Maven Dependency Caching

The pipeline caches `~/.m2/repository` between runs using the `pom.xml` hash
as the cache key. This reduces build time from ~3 minutes to ~30 seconds on
subsequent runs where dependencies haven't changed.
