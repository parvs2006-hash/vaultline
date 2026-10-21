'use strict';
const test = require('node:test');
const assert = require('node:assert');
const { createApp, runComprehensiveTestStrategy } = require('../src/app');

let appInstance, server, base;

test.before(() => new Promise((resolve) => {
  appInstance = createApp();
  server = appInstance.listen(0, () => {
    base = `http://127.0.0.1:${server.address().port}`;
    resolve();
  });
}));

test.after(() => server.close());

const post = (path, body) =>
  fetch(base + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

// 1. Health & System Integrity Tests
test('health check returns ok with SDG 9 metadata', async () => {
  const res = await fetch(base + '/health');
  assert.strictEqual(res.status, 200);
  const data = await res.json();
  assert.strictEqual(data.status, 'ok');
  assert.ok(data.sdgAlignment.includes('SDG 9'));
});

// 2. Security Headers Test
test('security headers are strictly enforced', async () => {
  const res = await fetch(base + '/health');
  assert.strictEqual(res.headers.get('x-frame-options'), 'DENY');
  assert.strictEqual(res.headers.get('x-content-type-options'), 'nosniff');
  assert.ok(res.headers.get('content-security-policy'));
});

// 3. Functional Accounts Test
test('retrieve all active accounts with initial balances', async () => {
  const res = await fetch(base + '/api/accounts');
  assert.strictEqual(res.status, 200);
  const accounts = await res.json();
  assert.strictEqual(accounts.length, 3);
  assert.strictEqual(accounts[0].id, 'ACC-1001');
  assert.ok(accounts[0].balance > 0);
});

// 4. Functional Transfer Execution Test
test('execute valid funds transfer with balance deduction', async () => {
  const payload = {
    fromAccount: 'ACC-1001',
    toAccount: 'ACC-1002',
    recipientName: 'High-Yield Savings',
    amount: 250.00,
    securityPin: '1234',
    note: 'Weekly Savings Deposit'
  };
  const res = await post('/api/transfer', payload);
  assert.strictEqual(res.status, 201);
  const data = await res.json();
  assert.strictEqual(data.success, true);
  assert.strictEqual(data.transaction.amount, 250.00);
});

// 5. Security Test: Negative and Zero Amount Injection
test('reject negative or zero transfer amounts', async () => {
  for (const amt of [-100, 0, 'invalid']) {
    const res = await post('/api/transfer', {
      fromAccount: 'ACC-1001',
      toAccount: 'ACC-1002',
      amount: amt,
      securityPin: '1234'
    });
    assert.strictEqual(res.status, 400);
  }
});

// 6. Security Test: Overdraft / Insufficient Funds Boundary
test('reject transfer exceeding available balance', async () => {
  const res = await post('/api/transfer', {
    fromAccount: 'ACC-1001',
    toAccount: 'ACC-1002',
    amount: 35000.00,
    securityPin: '1234'
  });
  assert.strictEqual(res.status, 400);
  const data = await res.json();
  assert.ok(data.error.includes('Insufficient funds'));
});

// 7. Security Test: Self-Transfer Tampering
test('reject transfer when origin and target are identical', async () => {
  const res = await post('/api/transfer', {
    fromAccount: 'ACC-1001',
    toAccount: 'ACC-1001',
    amount: 100.00,
    securityPin: '1234'
  });
  assert.strictEqual(res.status, 400);
  const data = await res.json();
  assert.ok(data.error.includes('cannot be identical'));
});

// 8. Security Test: SQL Injection Parameter Tampering
test('reject SQL injection payload in account number', async () => {
  const res = await post('/api/transfer', {
    fromAccount: 'ACC-1001',
    toAccount: "ACC-1001' OR '1'='1",
    amount: 50.00,
    securityPin: '1234'
  });
  assert.strictEqual(res.status, 400);
});

// 9. Security Test: Invalid PIN Authorization
test('reject transfer with invalid security PIN', async () => {
  const res = await post('/api/transfer', {
    fromAccount: 'ACC-1001',
    toAccount: 'ACC-1002',
    amount: 50.00,
    securityPin: 'bad'
  });
  assert.strictEqual(res.status, 400);
});

// 10. Comprehensive Test Strategy Execution Engine
test('run in-app test strategy engine and verify 100% pass', async () => {
  const res = await post('/api/tests/run', {});
  assert.strictEqual(res.status, 200);
  const result = await res.json();
  assert.strictEqual(result.summary.failed, 0);
  assert.strictEqual(result.summary.passPercentage, '100.0%');
});
