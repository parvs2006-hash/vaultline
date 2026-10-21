'use strict';
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');

const MAX_JOBS = 200;
const MAX_BODY_BYTES = 2048;
const ALLOWED_TYPES = ['full', 'incremental', 'snapshot'];
const ALLOWED_TARGETS = ['s3-primary', 's3-cross-region', 'glacier-archive'];
const PUBLIC_DIR = path.resolve(__dirname, '..', 'public');
const MIME = { '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.json': 'application/json; charset=utf-8', '.svg': 'image/svg+xml' };

function sendJson(res, status, data) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(data));
}
function readJson(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    let bytes = 0;
    req.on('data', chunk => {
      bytes += chunk.length;
      if (bytes > MAX_BODY_BYTES) {
        reject(Object.assign(new Error('Request body too large'), { statusCode: 413 }));
        req.destroy();
        return;
      }
      body += chunk.toString('utf8');
    });
    req.on('end', () => {
      if (!body.trim()) return resolve({});
      try { resolve(JSON.parse(body)); }
      catch { reject(Object.assign(new Error('Invalid JSON body'), { statusCode: 400 })); }
    });
    req.on('error', reject);
  });
}
function serveStatic(req, res, pathname) {
  const requested = pathname === '/' ? 'index.html' : decodeURIComponent(pathname).replace(/^\/+/, '');
  const file = path.resolve(PUBLIC_DIR, requested);
  if (file !== PUBLIC_DIR && !file.startsWith(PUBLIC_DIR + path.sep)) return sendJson(res, 403, { error: 'Forbidden' });
  fs.readFile(file, (err, content) => {
    if (err) return sendJson(res, 404, { error: 'Not found' });
    res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
    if (req.method === 'HEAD') return res.end();
    res.end(content);
  });
}
function createApp() {
  const jobs = [];
  let nextId = 1;
  const server = http.createServer(async (req, res) => {
    res.setHeader('Content-Security-Policy', "default-src 'self'; style-src 'self'; script-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Frame-Options', 'DENY');
    res.setHeader('Referrer-Policy', 'no-referrer');
    res.setHeader('Cache-Control', 'no-store');
    const url = new URL(req.url, 'http://localhost');
    const pathname = url.pathname;

    if (req.method === 'GET' && pathname === '/health') return sendJson(res, 200, { status: 'ok', uptime: process.uptime() });
    if (req.method === 'GET' && pathname === '/api/jobs') return sendJson(res, 200, jobs);
    if (req.method === 'POST' && pathname === '/api/jobs') {
      let body;
      try { body = await readJson(req); }
      catch (err) { if (!res.headersSent && !res.writableEnded) sendJson(res, err.statusCode || 400, { error: err.message || 'Invalid request' }); return; }
      const { name, type, target, sizeGb } = body || {};
      const cleanName = typeof name === 'string' ? name.trim() : '';
      if (!/^[A-Za-z0-9 _.-]{3,40}$/.test(cleanName)) return sendJson(res, 400, { error: 'Name must be 3-40 characters: letters, digits, space, _ . -' });
      if (!ALLOWED_TYPES.includes(type)) return sendJson(res, 400, { error: 'Invalid backup type' });
      if (!ALLOWED_TARGETS.includes(target)) return sendJson(res, 400, { error: 'Invalid target' });
      const size = Number(sizeGb);
      if (!Number.isFinite(size) || size < 1 || size > 5000) return sendJson(res, 400, { error: 'Size must be between 1 and 5000 GB' });
      if (jobs.length >= MAX_JOBS) return sendJson(res, 429, { error: 'Job limit reached' });
      const job = { id: nextId++, name: cleanName, type, target, sizeGb: size, status: 'completed', createdAt: new Date().toISOString(), ...estimate(type, target, size) };
      jobs.push(job);
      return sendJson(res, 201, job);
    }
    const restoreMatch = pathname.match(/^\/api\/jobs\/(\d+)\/restore$/);
    if (req.method === 'POST' && restoreMatch) {
      const job = jobs.find(j => j.id === Number(restoreMatch[1]));
      if (!job) return sendJson(res, 404, { error: 'Job not found' });
      job.status = 'restored';
      job.restoredAt = new Date().toISOString();
      return sendJson(res, 200, job);
    }
    if ((req.method === 'GET' || req.method === 'HEAD') && !pathname.startsWith('/api/')) return serveStatic(req, res, pathname);
    return sendJson(res, 404, { error: 'Not found' });
  });
  return { listen: (...args) => server.listen(...args), close: (...args) => server.close(...args), address: () => server.address() };
}

// Educational estimate only; this prototype does not connect to real cloud storage.
function estimate(type, target, sizeGb) {
  const rpoMin = { full: 1440, incremental: 60, snapshot: 15 }[type];
  const speed = { 's3-primary': 50, 's3-cross-region': 30, 'glacier-archive': 5 }[target];
  return { rpoMinutes: rpoMin, rtoMinutes: Math.max(1, Math.ceil(sizeGb / speed)) };
}
module.exports = { createApp, estimate };
