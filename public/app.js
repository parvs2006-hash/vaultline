'use strict';
const $ = (id) => document.getElementById(id);
const form = $('job-form');
const notice = $('notice');
const submitButton = $('submit-btn');
const jobsBody = $('jobs-body');
let allJobs = [];

const typeLabels = { full: 'Full backup', incremental: 'Incremental', snapshot: 'Snapshot' };
const targetLabels = { 's3-primary': 'S3 · Primary region', 's3-cross-region': 'S3 · Cross-region', 'glacier-archive': 'Glacier · Archive' };
const rpoFor = { full: 1440, incremental: 60, snapshot: 15 };
const speedFor = { 's3-primary': 50, 's3-cross-region': 30, 'glacier-archive': 5 };
const fmtTime = (minutes) => minutes >= 60 ? `${minutes % 60 ? (minutes / 60).toFixed(1) : minutes / 60} h` : `${minutes} min`;
const fmtSize = (gb) => Number(gb).toLocaleString(undefined, { maximumFractionDigits: 1 });

async function request(url, options = {}) {
  const response = await fetch(url, options);
  let data = null;
  try { data = await response.json(); } catch { /* handled below */ }
  if (!response.ok) throw new Error(data?.error || `Request failed (${response.status})`);
  return data;
}
function showNotice(message, isError = false) {
  notice.textContent = message;
  notice.classList.toggle('error', isError);
  notice.hidden = !message;
}
function updateEstimate() {
  const type = $('type').value;
  const target = $('target').value;
  const size = Number($('size').value);
  $('rpo-preview').textContent = fmtTime(rpoFor[type] || 15);
  $('rto-preview').textContent = Number.isFinite(size) && size >= 1 ? fmtTime(Math.max(1, Math.ceil(size / (speedFor[target] || 50)))) : '—';
}
function makeCell(text, className) {
  const td = document.createElement('td');
  td.textContent = String(text);
  if (className) td.className = className;
  return td;
}
function renderStats() {
  $('total-jobs').textContent = allJobs.length;
  $('total-size').innerHTML = `${fmtSize(allJobs.reduce((sum, job) => sum + Number(job.sizeGb || 0), 0))} <small>GB</small>`;
  $('completed-jobs').textContent = allJobs.filter(job => job.status === 'completed' || job.status === 'restored').length;
  $('restored-jobs').textContent = allJobs.filter(job => job.status === 'restored').length;
  $('job-count').textContent = `${allJobs.length} ${allJobs.length === 1 ? 'job' : 'jobs'}`;
}
function renderJobs() {
  const search = $('search').value.trim().toLowerCase();
  const status = $('status-filter').value;
  const filtered = allJobs.filter(job => {
    const matchesSearch = `${job.name} ${job.type} ${job.target} ${job.id}`.toLowerCase().includes(search);
    return matchesSearch && (status === 'all' || job.status === status);
  });
  jobsBody.replaceChildren();
  for (const job of filtered) {
    const row = document.createElement('tr');
    const nameCell = document.createElement('td');
    const name = document.createElement('div'); name.className = 'job-name'; name.textContent = job.name;
    const id = document.createElement('div'); id.className = 'job-id'; id.textContent = `JOB-${String(job.id).padStart(4, '0')} · ${fmtSize(job.sizeGb)} GB`;
    nameCell.append(name, id); row.append(nameCell);

    const typeCell = document.createElement('td');
    const type = document.createElement('div'); type.className = 'type-label'; type.textContent = typeLabels[job.type] || job.type;
    const target = document.createElement('div'); target.className = 'target-label'; target.textContent = targetLabels[job.target] || job.target;
    typeCell.append(type, target); row.append(typeCell);
    row.append(makeCell(fmtTime(job.rpoMinutes)));
    row.append(makeCell(fmtTime(job.rtoMinutes)));
    const statusCell = document.createElement('td'); const badge = document.createElement('span');
    badge.className = `status${job.status === 'restored' ? ' restored' : ''}`; badge.textContent = job.status === 'restored' ? 'Restored' : 'Completed'; statusCell.append(badge); row.append(statusCell);
    const actionCell = document.createElement('td'); const button = document.createElement('button');
    button.className = 'restore-btn'; button.type = 'button'; button.textContent = job.status === 'restored' ? '✓ Rehearsed' : 'Test restore'; button.disabled = job.status === 'restored';
    button.addEventListener('click', async () => {
      button.disabled = true; button.textContent = 'Testing…';
      try { await request(`/api/jobs/${job.id}/restore`, { method: 'POST' }); await loadJobs(); showNotice(`Restore rehearsal completed for “${job.name}”. This was a simulation; no files were restored.`); }
      catch (error) { showNotice(error.message, true); button.disabled = false; button.textContent = 'Retry restore'; }
    });
    actionCell.append(button); row.append(actionCell); jobsBody.append(row);
  }
  $('empty').hidden = allJobs.length > 0;
  $('no-results').hidden = allJobs.length === 0 || filtered.length > 0;
  $('jobs-table').hidden = filtered.length === 0;
  $('table-summary').textContent = filtered.length === allJobs.length ? `Showing ${filtered.length} ${filtered.length === 1 ? 'job' : 'jobs'}` : `Showing ${filtered.length} of ${allJobs.length} jobs`;
}
async function loadJobs() {
  const data = await request('/api/jobs');
  if (!Array.isArray(data)) throw new Error('Unexpected response from jobs API');
  allJobs = data;
  renderStats(); renderJobs();
}
form.addEventListener('submit', async (event) => {
  event.preventDefault(); showNotice('');
  if (!form.reportValidity()) return;
  submitButton.disabled = true; submitButton.innerHTML = 'Creating job…';
  const payload = { name: $('name').value.trim(), type: $('type').value, target: $('target').value, sizeGb: Number($('size').value) };
  try {
    const job = await request('/api/jobs', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    await loadJobs(); form.reset(); $('size').value = '100'; updateEstimate();
    showNotice(`Backup job “${job.name}” created. Estimates are illustrative; no data was uploaded to cloud storage.`);
  } catch (error) { showNotice(error.message, true); }
  finally { submitButton.disabled = false; submitButton.innerHTML = 'Create backup job <span>→</span>'; }
});
$('type').addEventListener('change', updateEstimate);
$('target').addEventListener('change', updateEstimate);
$('size').addEventListener('input', updateEstimate);
$('search').addEventListener('input', renderJobs);
$('status-filter').addEventListener('change', renderJobs);
$('refresh').addEventListener('click', async () => {
  const button = $('refresh'); button.disabled = true; button.textContent = '↻ Refreshing';
  try { await loadJobs(); showNotice('Dashboard refreshed successfully.'); }
  catch (error) { showNotice(`Could not load jobs: ${error.message}. Check that the local server is running.`, true); }
  finally { button.disabled = false; button.textContent = '↻ Refresh'; }
});
updateEstimate();
loadJobs().catch(error => showNotice(`Could not connect to the local API: ${error.message}. Start the server with npm start, then refresh.`, true));
