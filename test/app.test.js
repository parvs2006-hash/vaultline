'use strict';
const test = require('node:test');
const assert = require('node:assert');
const { createApp, estimate } = require('../src/app');

let server, base;
test.before(() => new Promise((r) => {
  server = createApp().listen(0, () => { base = `http://127.0.0.1:${server.address().port}`; r(); });
}));
test.after(() => server.close());

const post = (p, body) => fetch(base + p, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

test('health endpoint', async () => {
  const r = await fetch(base + '/health');
  assert.strictEqual(r.status, 200);
  assert.strictEqual((await r.json()).status, 'ok');
});
test('security headers set', async () => {
  const r = await fetch(base + '/health');
  assert.strictEqual(r.headers.get('x-frame-options'), 'DENY');
  assert.ok(r.headers.get('content-security-policy'));
  assert.strictEqual(r.headers.get('x-powered-by'), null);
});
test('create job and restore', async () => {
  const r = await post('/api/jobs', { name: 'Payroll DB', type: 'snapshot', target: 's3-primary', sizeGb: 100 });
  assert.strictEqual(r.status, 201);
  const job = await r.json();
  assert.strictEqual(job.rpoMinutes, 15);
  const rr = await post(`/api/jobs/${job.id}/restore`, {});
  assert.strictEqual((await rr.json()).status, 'restored');
});
test('rejects bad input (boundary + injection)', async () => {
  for (const body of [
    { name: '<script>', type: 'full', target: 's3-primary', sizeGb: 10 },
    { name: 'ok name', type: 'bogus', target: 's3-primary', sizeGb: 10 },
    { name: 'ok name', type: 'full', target: 'nope', sizeGb: 10 },
    { name: 'ok name', type: 'full', target: 's3-primary', sizeGb: 0 },
    { name: 'ok name', type: 'full', target: 's3-primary', sizeGb: 5001 }
  ]) assert.strictEqual((await post('/api/jobs', body)).status, 400);
});
test('unknown job returns 404', async () => {
  assert.strictEqual((await post('/api/jobs/999/restore', {})).status, 404);
});
test('estimate model', () => {
  assert.deepStrictEqual(estimate('full', 'glacier-archive', 100), { rpoMinutes: 1440, rtoMinutes: 20 });
});
