# DataForge — CI/CD Pipelines

This folder contains all pipeline definitions for DataForge:

```
pipelines/
├── Jenkinsfile                      ← Jenkins declarative pipeline
├── README.md                        ← this file
└── .github/
    └── workflows/
        ├── ci-cd.yml                ← GitHub Actions pipeline
        └── README.md                ← GitHub Actions guide
```

---

## Jenkins

### What is Jenkins?

Jenkins is an open-source automation server. It watches your Git repository and
automatically runs a pipeline (build → test → deploy) every time code is pushed.

Unlike GitHub Actions (which runs on GitHub's servers), Jenkins runs on **your
own server** — giving you full control over the build environment, secrets, and
hardware. It's widely used in enterprises that cannot send code to external cloud
runners.

Key concepts:
- **Job / Pipeline** — a configured automation task
- **Build** — one run of a pipeline
- **Agent / Node** — the machine that executes the build steps
- **Stage** — a named phase of the pipeline (visible in the Stage View UI)
- **Jenkinsfile** — the pipeline definition stored in your repo as code

---

### Pipeline Stages

| Stage | What it does |
|---|---|
| **Checkout** | Clones the selected Git branch onto the Jenkins agent |
| **Code Quality** | Runs Maven Checkstyle (reports violations, doesn't fail) |
| **Build** | Compiles JAR with `mvn package`, builds Docker image |
| **Test** | Runs all 27 JUnit 5 tests, publishes results in Jenkins UI |
| **Security Scan** | Trivy scans the Docker image for HIGH/CRITICAL CVEs |
| **Deploy to Staging** | `docker-compose up -d` — only when ENVIRONMENT=staging |
| **Deploy to Production** | `kubectl apply` with manual approval gate — only when ENVIRONMENT=prod |

---

### How to Create a Jenkins Pipeline Job

**Prerequisites:**
- Jenkins running (Docker: `docker run -p 8080:8080 jenkins/jenkins:lts`)
- Plugins installed: Git, Pipeline, Docker Pipeline, JUnit

**Steps:**

1. Open Jenkins at `http://localhost:8080`

2. Click **New Item**

3. Enter name: `DataForge` → select **Pipeline** → click OK

4. Under **General**, check **This project is parameterized** and add:
   - String Parameter: `BRANCH` (default: `main`)
   - Choice Parameter: `ENVIRONMENT` (choices: `dev`, `staging`, `prod`)

5. Under **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/aashika482/dataforge.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`

6. Click **Save**

7. Click **Build with Parameters** → choose branch and environment → **Build**

8. Click the build number → **Console Output** to watch live logs

---

### Running Jenkins with Docker

```bash
# Start Jenkins
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get the initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Then open `http://localhost:8080` and complete the setup wizard.

---

### Viewing Pipeline Results

- **Stage View** — visual grid showing each stage's status per build
- **Test Results** — JUnit test report (pass/fail breakdown per test class)
- **Console Output** — full raw logs for any build
- **Build History** — left sidebar showing all past builds with status icons

---

## GitHub Actions

See [.github/workflows/README.md](.github/workflows/README.md) for the GitHub
Actions pipeline documentation.

**Quick comparison:**

| Feature | Jenkins | GitHub Actions |
|---|---|---|
| Runs on | Your own server | GitHub's cloud runners |
| Config file | `Jenkinsfile` | `.github/workflows/*.yml` |
| Trigger | Push, webhook, schedule | Push, PR, schedule, manual |
| Docker support | Via plugin | Built-in |
| Cost | Free (self-hosted) | Free tier (2000 min/month) |
