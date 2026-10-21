# Vaultline — Setup, Git, Docker & Jenkins Guide

This guide walks you through every requirement in the CIAP DevSecOps microproject brief, step by step.

---

## 1. Local Run & Verification (Website)

Vaultline is a pure Node.js application (requires Node.js 20 or newer) with zero external npm dependencies.

```powershell
# In PowerShell (c:\Users\student\OneDrive\vaultline)
node --version
npm test
npm start
```

Open your browser to: **`http://localhost:3000`**
- Test creating a demo backup job (e.g. `Production DB`, `snapshot`, `s3-primary`, `150` GB).
- Observe live RPO/RTO calculation.
- Search/filter the job list.
- Click **Test restore** to execute a simulated disaster recovery rehearsal.

---

## 2. Git & GitHub Repository Setup

### Step 2.1: Initialize and Commit Locally
```powershell
git init
git config user.name "Your Name"
git config user.email "your.email@example.com"

# Check status and add files
git status
git add .
git commit -m "feat(init): Vaultline disaster recovery console with DevSecOps pipeline"
```

### Step 2.2: Create Branching Model
```powershell
# Create dev branch (as planned in JIRA Sprint 1)
git branch dev
git checkout dev
git commit --allow-empty -m "chore: initialize dev branch"
git checkout main
```

### Step 2.3: Push to GitHub
1. Go to [GitHub.com](https://github.com) and click **New Repository**.
2. Name it `vaultline` (choose Public or Private as required by your instructor).
3. Do not initialize with README or license (we already have them).
4. Run:
```powershell
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/vaultline.git
git branch -M main
git push -u origin main
git push -u origin dev
```
*Screenshot this GitHub repository page for your project submission evidence.*

---

## 3. Docker Containerization

The [Dockerfile](../Dockerfile) uses a hardened multi-stage build on `node:20-alpine`, runs as non-root (`USER node`), and includes an automated healthcheck.

### Step 3.1: Build the Image
```powershell
docker build -t vaultline:1.0.0 -t vaultline:latest .
```

### Step 3.2: Inspect Image and Size
```powershell
docker images vaultline
```

### Step 3.3: Run the Container
```powershell
docker run -d --name vaultline-app -p 3000:3000 --restart unless-stopped vaultline:latest
```

### Step 3.4: Verify Container Health
```powershell
docker ps
curl http://localhost:3000/health
```

### Step 3.5: Stop and Clean Up
```powershell
docker stop vaultline-app
docker rm vaultline-app
```

---

## 4. Security Scans: SonarQube & Trivy

### Step 4.1: Trivy Filesystem Scan (Dependencies, Secrets, Misconfigurations)
Install Trivy or run via Docker:
```powershell
# Scan current directory
docker run --rm -v ${PWD}:/app -w /app aquasec/trivy:latest fs --scanners vuln,secret,misconfig --severity HIGH,CRITICAL .
```
Save the output table to `docs/reports/trivy-fs.txt` and take a screenshot.

### Step 4.2: Trivy Container Image Scan
```powershell
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL vaultline:latest
```
Save the output to `docs/reports/trivy-image.txt` and take a screenshot.

### Step 4.3: SonarQube Scan
If running SonarQube locally via Docker:
```powershell
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community
```
1. Open `http://localhost:9000` (default login: `admin`/`admin`).
2. Generate a project token.
3. Run `sonar-scanner` using the settings in [sonar-project.properties](../sonar-project.properties).
4. Take a screenshot of the Quality Gate status (A rating, 0 bugs, 0 vulnerabilities).

---

## 5. Jenkins CI/CD Pipeline

The [Jenkinsfile](../Jenkinsfile) defines an 8-stage declarative pipeline:
1. **Checkout**: Pulls latest commit from GitHub.
2. **Install & Test**: Runs `npm install` and Node.js unit tests with lcov code coverage.
3. **SonarQube Analysis**: Runs static code analysis (SAST) against Quality Gate.
4. **Trivy (FS & IaC)**: Scans repo files and Terraform configurations for misconfigurations and leaked secrets.
5. **Docker Build**: Builds tagged Docker image.
6. **Trivy (Image)**: Scans container image; fails pipeline if CRITICAL vulnerabilities exist.
7. **Push Image**: Pushes to Docker Hub registry.
8. **Deploy to AWS EC2**: Connects over SSH and updates the running container.
9. **Smoke Test**: Checks `http://$DEPLOY_HOST/health` for status 200 OK.

---

## 6. Terraform AWS Infrastructure

Located in [terraform/main.tf](../terraform/main.tf):
- **VPC & Subnet**: Dedicated CIDR block `10.0.0.0/16` with public internet gateway.
- **Security Group**: Restricts port 22 SSH to `admin_cidr`, opens port 80 HTTP for the web app.
- **EC2 Instance**: Amazon Linux 2023 with IMDSv2 enforced, encrypted root volume, and automated Docker bootstrapping.
- **S3 Bucket**: Versioned, encrypted (AES-256), public access completely blocked (for DR storage tier).

To apply:
```bash
cd terraform
terraform init
terraform plan -var="key_name=YOUR_KEY" -var="admin_cidr=$(curl -s ifconfig.me)/32"
terraform apply
```

---

## 7. Submission Checklist & Screenshots Required

| Item | Action | File / Location |
|---|---|---|
| **Website** | Test app at `http://localhost:3000` | Screenshot dashboard with jobs |
| **GitHub** | Push `main` and `dev` branches | Screenshot repo code & commit history |
| **Docker** | Build & run `vaultline:latest` | Screenshot `docker ps` & `docker images` |
| **Jenkins** | Run declarative pipeline | Screenshot stage view showing green stages |
| **SonarQube** | Run SAST scan | Screenshot Quality Gate & Overview |
| **Trivy** | Run filesystem and image scans | Screenshot terminal tables |
| **JIRA** | Create Scrum board from [jira_plan.md](jira_plan.md) | Screenshot Active Sprints & Backlog |
| **Diagrams** | Open [architecture.mmd](architecture.mmd) in Draw.io / Mermaid | Export PNG diagram |
| **Report** | Complete 13 sections | [docs/report.md](report.md) |
| **PPT** | Present 8 slides | [docs/presentation.md](presentation.md) |
| **Reflection** | Personalize 400-500 words for Turnitin | [docs/reflection.md](reflection.md) |
| **LinkedIn Demo** | 2-minute walkthrough video | Post on LinkedIn without showing secrets |
