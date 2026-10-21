# JIRA Scrum Plan: Comprehensive Test Strategy for Secure Online Banking Application
Project Key: **BANK** | Methodology: Scrum (3 One-Week Sprints) | SDG Alignment: UN SDG 9

---

### Sprint 1: Banking Domain, Core APIs & Test Automation Suite
**Goal:** Implement banking domain models, secure transfer APIs, input sanitization, and automated unit/boundary test suites.

| Story ID | Summary | Story Points | Category |
|---|---|---|---|
| **BANK-1** | Design Responsive Online Banking Web UI (Accounts, Balances, Transfer Form) | 5 | Frontend |
| **BANK-2** | Implement REST Endpoints (`/api/accounts`, `/api/transactions`, `/api/transfer`) | 5 | Backend |
| **BANK-3** | Implement Strict Input Sanitization, Allow-list Regex & 4-digit PIN Authorization | 3 | Security |
| **BANK-4** | Develop Automated Unit & Boundary Test Suite using `node:test` (10 test gates) | 5 | Testing |
| **BANK-5** | Implement In-App Live Comprehensive Test Strategy Execution Matrix | 5 | Testing / UI |
| **BANK-6** | Setup GitHub Repository with Protected `main` and `dev` Branching Policies | 2 | Version Control |

---

### Sprint 2: Containerization, Static Code Analysis & Pipeline Security Gates
**Goal:** Containerize the banking portal with multi-stage non-root Alpine Docker, and build automated Jenkins CI/CD pipeline with SonarQube & Trivy.

| Story ID | Summary | Story Points | Category |
|---|---|---|---|
| **BANK-7** | Author Hardened Multi-Stage Dockerfile on `node:20-alpine` with `USER node` | 5 | DevOps |
| **BANK-8** | Local Container Deployment and Docker Compose Orchestration Stack | 3 | DevOps |
| **BANK-9** | Setup Jenkins Server and Configure Declarative Pipeline (`Jenkinsfile`) | 5 | CI/CD |
| **BANK-10** | Integrate Automated Unit Test & LCOV Code Coverage Stage into Jenkins | 3 | CI/CD |
| **BANK-11** | Integrate SonarQube SAST Quality Gate Analysis for Static Code Security | 5 | DevSecOps |
| **BANK-12** | Integrate Aqua Security Trivy Filesystem, Secret & Container Image Scanners | 5 | DevSecOps |

---

### Sprint 3: Cloud Infrastructure as Code, Security Audit & Project Defense
**Goal:** Provision AWS cloud infrastructure with Terraform, generate vulnerability reports, and compile final report and PPT.

| Story ID | Summary | Story Points | Category |
|---|---|---|---|
| **BANK-13** | Author Terraform IaC for AWS VPC, Public Subnet, Security Group & EC2 Host | 5 | Cloud IaC |
| **BANK-14** | Codify Versioned, AES-256 Encrypted S3 Bucket for Banking Compliance Ledger | 3 | Cloud IaC |
| **BANK-15** | Configure Automated SSH Deployment Stage to AWS EC2 in CI/CD Pipeline | 5 | CI/CD |
| **BANK-16** | Generate Draw.io Application Architecture and DevSecOps Flow Diagrams | 3 | Documentation |
| **BANK-17** | Compile Vulnerability Assessment Report (Trivy Image Scan & SonarQube SAST) | 3 | Security |
| **BANK-18** | Complete Academic Report (13 Sections) and Presentation Deck (8 Sections) | 5 | Documentation |
| **BANK-19** | Author Individual Turnitin-Ready Learning Reflection (400-500 words) | 3 | Academic |
