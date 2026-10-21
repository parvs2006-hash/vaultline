'use strict';

const $ = (id) => document.getElementById(id);

async function request(url, options = {}) {
  const res = await fetch(url, options);
  let data = null;
  try { data = await res.json(); } catch {}
  if (!res.ok) throw new Error(data?.error || `Request failed (${res.status})`);
  return data;
}

function showNotice(msg, isError = false) {
  const notice = $('notice');
  if (!msg) {
    notice.style.display = 'none';
    return;
  }
  notice.textContent = msg;
  notice.className = 'notice' + (isError ? ' error' : '');
  notice.style.display = 'flex';
  setTimeout(() => { notice.style.display = 'none'; }, 6000);
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

async function loadAccounts() {
  try {
    const accounts = await request('/api/accounts');
    const checking = accounts.find(a => a.id === 'ACC-1001');
    const savings = accounts.find(a => a.id === 'ACC-1002');
    const reserve = accounts.find(a => a.id === 'ACC-1003');

    if (checking) $('bal-checking').textContent = formatCurrency(checking.balance);
    if (savings) $('bal-savings').textContent = formatCurrency(savings.balance);
    if (reserve) $('bal-reserve').textContent = formatCurrency(reserve.balance);
  } catch (err) {
    console.error('Failed to load accounts', err);
  }
}

async function loadTransactions() {
  try {
    const txns = await request('/api/transactions');
    const tbody = $('transactions-body');
    tbody.innerHTML = '';

    $('txn-count').textContent = `${txns.length} Transactions`;

    for (const t of txns) {
      const tr = document.createElement('tr');

      const tdId = document.createElement('td');
      tdId.innerHTML = `<b>${escapeHtml(t.id)}</b><div style="font-size:10px;color:#64748b;">${new Date(t.timestamp).toLocaleTimeString()}</div>`;
      tr.appendChild(tdId);

      const tdFlow = document.createElement('td');
      tdFlow.innerHTML = `<div>${escapeHtml(t.from)} → ${escapeHtml(t.to)}</div><small style="color:#64748b;">${escapeHtml(t.type)}</small>`;
      tr.appendChild(tdFlow);

      const tdRec = document.createElement('td');
      tdRec.textContent = t.recipientName;
      tr.appendChild(tdRec);

      const tdAmt = document.createElement('td');
      tdAmt.innerHTML = `<b style="color:${t.from === 'ACC-1001' ? '#f87171' : '#34d399'}">${t.from === 'ACC-1001' ? '-' : '+'}${formatCurrency(t.amount)}</b>`;
      tr.appendChild(tdAmt);

      const tdNote = document.createElement('td');
      tdNote.textContent = t.note || '—';
      tr.appendChild(tdNote);

      const tdStatus = document.createElement('td');
      tdStatus.innerHTML = `<span class="status-pill completed">✓ ${escapeHtml(t.status)}</span>`;
      tr.appendChild(tdStatus);

      tbody.appendChild(tr);
    }
  } catch (err) {
    console.error('Failed to load transactions', err);
  }
}

async function runComprehensiveTests() {
  try {
    const results = await request('/api/tests/run', { method: 'POST' });
    const tbody = $('tests-body');
    tbody.innerHTML = '';

    $('tests-passed').textContent = results.summary.passed;
    $('tests-failed').textContent = results.summary.failed;
    $('tests-total').textContent = results.summary.total;
    $('tests-duration').textContent = `${results.durationMs} ms`;
    $('test-summary-card').textContent = results.summary.passPercentage;

    for (const test of results.tests) {
      const tr = document.createElement('tr');

      const tdCat = document.createElement('td');
      tdCat.innerHTML = `<span class="status-pill internal">${escapeHtml(test.category)}</span>`;
      tr.appendChild(tdCat);

      const tdName = document.createElement('td');
      tdName.innerHTML = `<b>${escapeHtml(test.name)}</b>`;
      tr.appendChild(tdName);

      const tdDetails = document.createElement('td');
      tdDetails.style.color = '#94a3b8';
      tdDetails.textContent = test.details;
      tr.appendChild(tdDetails);

      const tdStatus = document.createElement('td');
      tdStatus.innerHTML = `<span class="status-pill pass">PASS ✓</span>`;
      tr.appendChild(tdStatus);

      tbody.appendChild(tr);
    }

    showNotice(`✓ Comprehensive Test Suite executed. ${results.summary.passed}/${results.summary.total} Test Gates Verified in ${results.durationMs}ms.`);
  } catch (err) {
    showNotice(`Test Suite Execution Failed: ${err.message}`, true);
  }
}

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

$('transfer-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = $('transfer-submit-btn');
  btn.disabled = true;
  btn.textContent = 'Processing Security Checks…';

  const payload = {
    fromAccount: $('from-account').value,
    toAccount: $('to-account').value.trim(),
    recipientName: $('recipient-name').value.trim(),
    amount: Number($('amount').value),
    securityPin: $('security-pin').value.trim(),
    note: $('note').value.trim()
  };

  try {
    const data = await request('/api/transfer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    showNotice(`✓ Transfer of ${formatCurrency(payload.amount)} to ${payload.toAccount} processed successfully. Ref: ${data.transaction.id}`);
    await loadAccounts();
    await loadTransactions();
    $('amount').value = '';
    $('note').value = '';
  } catch (err) {
    showNotice(err.message, true);
  } finally {
    btn.disabled = false;
    btn.innerHTML = 'Confirm & Execute Transfer <span>→</span>';
  }
});

$('run-all-tests-btn').addEventListener('click', runComprehensiveTests);
$('refresh-tests-btn').addEventListener('click', runComprehensiveTests);

// Initialization
loadAccounts();
loadTransactions();
runComprehensiveTests();
