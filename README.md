# Vaultline — Backup & Disaster Recovery Console

A runnable local educational prototype for a DevSecOps / CIAP microproject (SDG 9). It provides a dashboard to create sample backup jobs, inspect illustrative RPO/RTO estimates, search/filter jobs, and rehearse a simulated restore.

> **Prototype boundary:** this is not a production backup platform. It does not connect to AWS/S3, upload or recover real files, schedule background backups, or persist jobs to disk. Jobs are held in server memory and reset when the process restarts. RPO/RTO values are illustrative estimates only.

## Requirements

- Node.js 20 or newer
- Windows, macOS, or Linux
- No third-party npm dependencies are required. The server uses built-in Node.js modules.

## Run in VS Code (Windows)

1. Extract the ZIP.
2. In VS Code choose **File → Open Folder** and select the `vaultline` folder containing `package.json`.
3. Open **Terminal → New Terminal**.
4. Run:

   ```powershell
   node --version
   npm test
   npm start
   ```

5. Open **http://localhost:3000** in Chrome or Edge. Keep the terminal open while using the site.
6. Stop the server with **Ctrl+C**.

If port 3000 is busy, run `$env:PORT=3001; npm start` in PowerShell and open http://localhost:3001.

## Prototype features

- Responsive dashboard with job counts, data size, completed jobs and restore rehearsals.
- Create full, incremental and snapshot demo jobs.
- Choose primary-region, cross-region or archive demo targets.
- Live RPO/RTO estimate preview and validation on both client and server.
- Search jobs and filter by status.
- Simulated restore rehearsal with clear status feedback.
- REST API, health endpoint, security response headers and automated tests.

## API endpoints

- `GET /health` — health check.
- `GET /api/jobs` — list in-memory jobs.
- `POST /api/jobs` — create a validated demo job.
- `POST /api/jobs/:id/restore` — mark a job as simulated-restored.

Example create request:

```json
{"name":"Payroll DB","type":"snapshot","target":"s3-primary","sizeGb":100}
```

## Test

```sh
npm test
```

## Docker (optional)

```sh
docker build -t vaultline .
docker run --rm -p 3000:3000 vaultline
```

## Terraform / Jenkins

The Terraform and Jenkins files are deployment examples only. They require your own AWS account, credentials, configured Jenkins agents, registry, SonarQube and Trivy. Review the configuration and estimated cloud costs before using `terraform apply`. The local prototype requires no cloud credentials.
