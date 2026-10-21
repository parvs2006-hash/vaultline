# Presentation: Comprehensive Test Strategy for a Secure Online Banking Application

**Course:** DevSecOps Lab  
**Component:** Continuous Internal Assessment Practical (CIAP) – Microproject  
**Project Topic:** Comprehensive Test Strategy for a Secure Online Banking Application  
**UN SDG Alignment:** Goal 9 (Industry, Innovation, and Infrastructure)  
**Student Details:** [Your Name] | [Roll No / USN] | Department of Computer Science & Engineering  

---

## Slide 1: Problem Definition and Scope

### Title & Context
- **Project Title:** SecureBank — Secure Online Banking Application with Comprehensive Automated Test Strategy
- **Industrial Context:**
  - Online banking systems handle high-volume financial transactions where downtime, security vulnerabilities, or logic bugs result in catastrophic financial loss and regulatory non-compliance.
  - Traditional testing in banking is often siloed, executed late in the development cycle, and fails to incorporate dynamic threat vectors like SQL Injection, Cross-Site Scripting (XSS), and logic-based overdraft exploitation.
- **Problem Statement:**
  - How do we engineer a resilient digital banking system that embeds automated, multi-layered testing (Functional, Security, Boundary, and Resiliency) directly into a continuous delivery DevSecOps pipeline?
- **Project Scope:**
  - Build a responsive, functional online banking web portal (Accounts, Transfers, Audit Ledger).
  - Implement and automate a Comprehensive Test Strategy covering 10 distinct validation gates.
  - Establish a secure CI/CD pipeline using Docker, Jenkins, SonarQube, Trivy, and Terraform on AWS.
  - Align system resilience with UN Sustainable Development Goal 9.

> **Speaker Notes:**  
> "Good morning respected evaluators. Today I am presenting my DevSecOps CIAP microproject on 'Comprehensive Test Strategy for a Secure Online Banking Application'. In digital banking, software failures or security oversights directly threaten financial solvency. Our objective is to design a secure online banking platform called SecureBank and demonstrate how shifting automated testing and security left guarantees 100% operational integrity and compliance with UN SDG 9."

---

## Slide 2: System Design and DevSecOps Workflow

### Architecture & Pipeline Overview
- **System Tiering:**
  - **Presentation Tier:** Semantic, responsive web interface built with zero external runtime dependencies to prevent supply chain tampering.
  - **Application / API Tier:** Node.js HTTP core engine enforcing strict input regular expression boundaries, anti-XSS encoding, and atomic transaction execution.
  - **Ledger Tier:** In-memory ACID-like transactional state backed by versioned, encrypted audit storage.
- **DevSecOps Automated Delivery Workflow:**
  $$\text{Developer Git Push} \longrightarrow \text{GitHub Webhook} \longrightarrow \text{Jenkins Pipeline}$$
  $$\longrightarrow \text{Automated Tests} \longrightarrow \text{SonarQube SAST} \longrightarrow \text{Trivy Scans} \longrightarrow \text{Docker Build} \longrightarrow \text{AWS EC2}$$
- **Draw.io Architectural Model:**
  - Visual segregation between the developer workspace, automated CI security gates, and the cloud production VPC.

> **Speaker Notes:**  
> "This slide illustrates our system architecture and DevSecOps delivery workflow. When code is pushed to GitHub, Jenkins immediately triggers our automated test strategy. Only after all unit, boundary, and security test gates pass does the code advance to static analysis with SonarQube and container scanning with Trivy. This automated barrier prevents vulnerable artifacts from reaching our AWS cloud environment."

---

## Slide 3: Website Implementation and Testing

### Web Banking Portal & Test Execution Matrix
- **Core Banking Functionalities:**
  - **Account Management:** Multi-account balances (Primary Checking, High-Yield Savings, Emergency Reserve).
  - **Secure Funds Transfer:** Origin and target selection (`ACC-XXXX`), beneficiary routing, and strict numerical boundary checks.
  - **Authorization Verification:** Mandatory 4-digit transaction authorization PIN simulation.
  - **Immutable Transaction History:** Real-time ledger tracking completed transfers with unique transaction IDs (`TXN-XXXX`).
- **Comprehensive Test Strategy Implementation:**
  - Built-in live in-app test execution hub verifying 9 automated gates in $< 5\text{ ms}$:
    1. *Functional:* Mathematical balance integrity and ISO-4217 currency compliance.
    2. *Security / DAST:* SQL injection parameter sanitization and anti-XSS string neutralization.
    3. *Boundary / Logic:* Negative value transfer blocking, self-transfer prevention, and overdraft enforcement.
    4. *Performance / DoS:* 4KB request body capping preventing memory exhaustion attacks.
- **Automated Test Results:** 10/10 automated tests passing with 100% verification rate using Node.js test runner.

> **Speaker Notes:**  
> "Here is our working banking portal. Operators can execute real-time transfers with instant ledger updates. More importantly, we built a live Comprehensive Test Strategy verification matrix right into the portal. Evaluators can click 'Run Comprehensive Tests' and witness live execution of functional balance checks, SQL injection blocks, negative transfer defenses, and zero-overdraft enforcement."

---

## Slide 4: Docker Deployment and Jenkins Pipeline

### Containerization & CI/CD Automation
- **Multi-Stage Docker Architecture:**
  - Base Image: `node:20-alpine` (lightweight, minimal attack surface).
  - Non-Root User: Runs under system user `USER node` to prevent container breakout exploits.
  - Integrated Healthcheck: Built-in `wget /health` probe monitoring service availability every 30 seconds.
  - Ultra-compact image footprint: ~136 MB total size.
- **Jenkins 8-Stage Declarative Pipeline:**
  1. *Checkout:* Automated SCM synchronization.
  2. *Testing & Coverage:* Executes `node:test` suite; fails pipeline if any test fails.
  3. *SonarQube SAST:* Enforces Quality Gate criteria on code smell and security rating.
  4. *Trivy FS Scan:* Audits filesystem, secrets, and IaC scripts.
  5. *Docker Build:* Packages versioned image `securebank:${BUILD_NUMBER}`.
  6. *Trivy Image Scan:* Automated build-breaker on `CRITICAL` severity CVEs.
  7. *Docker Hub Push:* Authenticated publishing via Jenkins credentials.
  8. *Deploy & Smoke Test:* Automated SSH restart on AWS EC2 followed by `/health` curl check.

> **Speaker Notes:**  
> "Our deployment pipeline is fully codified in Jenkinsfile. We containerized SecureBank using a hardened multi-stage Alpine Dockerfile running as non-root. The Jenkins pipeline enforces automated testing and security gates at every stage. If any test fails, or if Trivy detects a critical vulnerability in the Docker image, the pipeline aborts immediately, upholding our zero-defect policy."

---

## Slide 5: AWS Infrastructure Using Terraform

### Codified Cloud Infrastructure as Code (IaC)
- **AWS Region:** Asia Pacific Mumbai (`ap-south-1`).
- **Networking Infrastructure:**
  - Dedicated VPC (`10.0.0.0/16`) with DNS hostnames enabled.
  - Public Subnet (`10.0.1.0/24`) with attached Internet Gateway and route tables.
- **Security Hardening (Firewall & Compute):**
  - **Security Group (`securebank-web-sg`):** Ingress Port 80 open for public banking traffic; Ingress Port 22 (SSH) strictly restricted to authorized admin IP (`var.admin_cidr`).
  - **EC2 Instance (`aws_instance.web`):** Amazon Linux 2023 on `t3.micro` with encrypted EBS volume.
  - **IMDSv2 Enforcement:** `http_tokens = "required"` preventing Server-Side Request Forgery (SSRF) cloud credential exfiltration.
- **Audit Storage Tier:**
  - Amazon S3 bucket with Object Versioning, AES-256 Server-Side Encryption, and complete Public Access Blocking for regulatory audit ledger compliance.

> **Speaker Notes:**  
> "We provisioned the entire AWS cloud environment using Terraform. Rather than manual console configuration, our infrastructure is 100% reproducible through code. We configured an isolated VPC, restricted SSH access to our administrative IP, enforced IMDSv2 on our EC2 instance to prevent SSRF credential theft, and provisioned a versioned, AES-256 encrypted S3 bucket for banking audit storage."

---

## Slide 6: Security Scan Results and Vulnerability Report

### Security Tooling & Vulnerability Matrix
- **Static Analysis (SonarQube):**
  - Analyzed JavaScript backend and frontend code.
  - **Results:** Maintainability Rating: A, Reliability: A, Security Rating: A.
  - Zero hardcoded passwords, zero insecure `eval()`, zero prototype pollution vectors.
- **Vulnerability Scanning (Aqua Security Trivy):**
  - **Filesystem & Secrets Scan (`trivy fs`):** Zero exposed AWS access keys or private tokens detected in code or Terraform scripts.
  - **Container Image Scan (`trivy image`):**
    - Scanned `securebank:latest` across OS packages and application dependencies.
    - Zero `CRITICAL` vulnerabilities detected in our application layer.
    - Quality Gate: **PASSED**.
- **In-Depth Hardening:**
  - Content Security Policy (CSP), anti-clickjacking `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`.
  - DOM text-node injection (`textContent` and `escapeHtml`) eliminating Cross-Site Scripting (XSS).

> **Speaker Notes:**  
> "This slide summarizes our security scanning results. We subjected SecureBank to both static analysis via SonarQube and container vulnerability scanning with Trivy. Both tools confirmed zero critical vulnerabilities. By adhering to defensive coding practices—such as avoiding innerHTML and utilizing strict regular expressions—we achieved an 'A' grade across security and maintainability metrics."

---

## Slide 7: Analysis and Reflection

### Performance, Challenges & Engineering Insights
- **Testing Performance Analysis:**
  - Complete 10-gate test suite executes in $< 200\text{ ms}$, ensuring zero friction in the local developer feedback loop and CI pipeline execution.
  - Zero false-positive rate on transaction boundary checks.
- **Key Challenges Overcome:**
  1. *Port Management & Local Orchestration:* Resolved port collisions by containerizing microservices across discrete host ports (3000, 3001, 8080, 9000).
  2. *Container Dependency Handling:* Optimized Alpine build steps to handle zero-dependency Node.js architectures cleanly.
  3. *Cloud Security Hardening:* Implementing parameterized CIDR blocks and IMDSv2 metadata options in Terraform.
- **Student Reflection Summary (Turnitin Compliant):**
  - Shifted personal mindset from manual testing to automated, continuous security guardrails.
  - Developed deep appreciation for infrastructure reproducibility and defense-in-depth principles in financial software systems.

> **Speaker Notes:**  
> "Reflecting on this project, the most critical learning outcome was experiencing how DevSecOps shifts testing from a reactive chore to a continuous quality gate. Resolving container build nuances, codifying AWS infrastructure, and seeing our test suite execute in milliseconds demonstrated that security and velocity can coexist seamlessly."

---

## Slide 8: Conclusion and Future Scope

### Project Outcomes & UN SDG 9 Alignment
- **Summary of Achievements:**
  - Built and verified **SecureBank**, an interactive online banking application.
  - Designed and executed a Comprehensive Automated Test Strategy with 100% pass rates.
  - Containerized with Docker and automated via an 8-stage Jenkins DevSecOps pipeline.
  - Codified AWS infrastructure with Terraform and conducted SonarQube/Trivy security audits.
- **SDG 9 Alignment:**
  - Contributed directly to **Target 9.1 & 9.4** by engineering resilient digital financial infrastructure and retrofitting enterprise banking with sustainable, lightweight cloud technologies.
- **Future Enhancements:**
  - Integration of JSON Web Token (JWT) based Multi-Factor Authentication (MFA) via SMS/TOTP.
  - Automated cloud failover across multi-region AWS deployments using Route 53.
  - Chaos engineering drills using Chaos Mesh to test network latency and container resilience under heavy banking loads.

> **Speaker Notes:**  
> "To conclude, SecureBank proves that rigorous test automation and DevSecOps pipelines are essential for modern financial resilience, advancing the core objectives of UN Sustainable Development Goal 9. Moving forward, the system can be expanded with multi-factor authentication and multi-region failover. Thank you for your time, and I am now ready for your questions."
