const state = { patients: [], queue: [], doctors: [], departments: [], products: [], invoices: [], suppliers: [], budgets: [] };
const $ = (selector) => document.querySelector(selector);
const initials = (name = '') => name.split(' ').map(part => part[0]).slice(0, 2).join('').toUpperCase() || '--';
const safe = (value, fallback = '—') => value === null || value === undefined || value === '' ? fallback : value;

async function api(path, options = {}) {
  const response = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!response.ok) throw new Error(`Request failed (${response.status})`);
  return response.status === 204 ? null : response.json();
}

function renderQueue(target = '#queue-list') {
  const list = $(target);
  if (!state.queue.length) { list.innerHTML = '<div class="empty-state">No patients waiting for triage.</div>'; return; }
  list.innerHTML = state.queue.slice(0, 5).map(item => {
    const patient = state.patients.find(record => record.patientId === item.patientId);
    const name = patient?.fullName || `Patient #${item.patientId || '—'}`;
    const priority = (item.triagePriority || 'NORMAL').toLowerCase();
    return `<div class="queue-item"><div class="queue-avatar">${initials(name)}</div><div><span class="queue-name">${name}</span><span class="queue-detail">${safe(item.symptoms, 'Assessment pending')}</span></div><span class="priority ${priority}">${priority}</span></div>`;
  }).join('');
}

function renderPatients() {
  const query = ($('#patient-search')?.value || '').toLowerCase();
  const records = state.patients.filter(patient => `${patient.fullName} ${patient.phone}`.toLowerCase().includes(query));
  $('#patients-table').innerHTML = records.length ? records.map(patient => `<tr><td class="patient-cell">${safe(patient.fullName)}</td><td>#${safe(patient.patientId)}</td><td>${safe(patient.dateOfBirth)}</td><td>${safe(patient.phone)}</td><td>${safe(patient.bloodGroup)}</td></tr>`).join('') : '<tr><td colspan="5" class="empty-state">No patient records found.</td></tr>';
}

function populatePatients() { $('#triage-patient').innerHTML = '<option value="">Select patient</option>' + state.patients.map(patient => `<option value="${patient.patientId}">${patient.fullName}</option>`).join(''); }

function populateOperations() {
  $('#doctor-department').innerHTML = '<option value="">No department</option>' + state.departments.map(department => `<option value="${department.departmentId}">${department.name}</option>`).join('');
  $('#invoice-patient').innerHTML = '<option value="">Select patient</option>' + state.patients.map(patient => `<option value="${patient.patientId}">${patient.fullName}</option>`).join('');
  $('#invoice-product').innerHTML = '<option value="">Select item</option>' + state.products.map(product => `<option value="${product.productId}">${product.name} - ${money(product.unitPrice)}</option>`).join('');
  $('#payment-invoice').innerHTML = '<option value="">Select invoice</option>' + state.invoices.filter(invoice => invoice.status !== 'PAID').map(invoice => `<option value="${invoice.invoiceId}">#${invoice.invoiceId} - ${money(invoice.totalAmount)}</option>`).join('');
}

function money(value) { return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(value) || 0); }
function departmentName(department) { return department?.name || 'Unassigned'; }

function renderOperations() {
  $('#doctor-count').textContent = `${state.doctors.length} records`;
  $('#product-count').textContent = `${state.products.length} records`;
  $('#doctors-table').innerHTML = state.doctors.length ? state.doctors.map(doctor => `<tr><td class="patient-cell">${safe(doctor.fullName)}</td><td>${safe(doctor.specialization)}</td><td>${departmentName(doctor.department)}</td><td>${safe(doctor.phone)}</td></tr>`).join('') : '<tr><td colspan="4" class="empty-state">No doctors registered.</td></tr>';
  $('#products-table').innerHTML = state.products.length ? state.products.map(product => `<tr><td class="patient-cell">${safe(product.name)}</td><td>${safe(product.category)}</td><td>${money(product.unitPrice)}</td><td>${product.isService ? 'Service' : 'Product'}</td></tr>`).join('') : '<tr><td colspan="4" class="empty-state">No services or products found.</td></tr>';
}

function renderInvoices() {
  $('#invoice-count').textContent = `${state.invoices.length} records`;
  $('#invoices-table').innerHTML = state.invoices.length ? state.invoices.map(invoice => `<tr><td class="patient-cell">#${invoice.invoiceId}</td><td>${safe(invoice.patient?.fullName, `Patient #${invoice.patient?.patientId || '—'}`)}</td><td>${safe(invoice.invoiceDate?.slice(0, 10))}</td><td>${money(invoice.totalAmount)}</td><td><span class="status-pill ${String(invoice.status || '').toLowerCase()}">${safe(invoice.status)}</span></td></tr>`).join('') : '<tr><td colspan="5" class="empty-state">No invoices created.</td></tr>';
}

function renderReport(report, target, detailTarget, detail) {
  $(target).textContent = money(report);
  $(detailTarget).innerHTML = detail || '<span>No ledger entries yet.</span>';
}

async function loadReports() {
  const year = new Date().getFullYear();
  $('#report-year').textContent = year;
  try {
    const [profitLoss, balanceSheet, utilization] = await Promise.all([api(`/api/reports/profit-loss?year=${year}`), api('/api/reports/balance-sheet'), api(`/api/budgets/utilization?year=${year}&month=${new Date().getMonth() + 1}`)]);
    renderReport(profitLoss.netProfit, '#report-profit', '#report-pl-detail', `<span>Revenue ${money(profitLoss.totalRevenue)}</span><span>Expenses ${money(profitLoss.totalExpenses)}</span>`);
    renderReport(balanceSheet.totalAssets, '#report-assets', '#report-bs-detail', `<span>Liabilities ${money(balanceSheet.totalLiabilities)}</span><span>Equity ${money(balanceSheet.totalEquity)}</span>`);
    const allocated = utilization.reduce((total, item) => total + Number(item.allocatedAmount || 0), 0);
    const spent = utilization.reduce((total, item) => total + Number(item.spentAmount || 0), 0);
    renderReport(allocated, '#report-budget', '#report-budget-detail', `<span>Spent ${money(spent)}</span><span>${allocated ? Math.round(spent / allocated * 100) : 0}% utilized</span>`);
  } catch (error) { ['#report-profit', '#report-assets', '#report-budget'].forEach(target => $(target).textContent = 'Unavailable'); }
}

async function loadData() {
  try { state.patients = await api('/api/patients'); } catch (error) { state.patients = []; }
  try { state.queue = await api('/api/triage/queue'); } catch (error) { state.queue = []; }
  try { state.doctors = await api('/api/doctors'); } catch (error) { state.doctors = []; }
  try { state.departments = await api('/api/departments'); } catch (error) { state.departments = []; }
  try { state.products = await api('/api/products'); } catch (error) { state.products = []; }
  try { state.invoices = await api('/api/invoices'); } catch (error) { state.invoices = []; }
  $('#patient-count').textContent = state.patients.length;
  $('#queue-count').textContent = state.queue.length;
  $('#emergency-count').textContent = state.queue.filter(item => item.triagePriority === 'EMERGENCY').length;
  $('#queue-caption').textContent = state.queue.length ? `${state.queue.length} active assessment${state.queue.length === 1 ? '' : 's'}` : 'Priority queue updates live';
  renderQueue(); renderQueue('#triage-queue-list'); renderPatients(); populatePatients(); populateOperations(); renderOperations(); renderInvoices();
}

function showView(view) {
  const viewMap = ['overview-panel', 'patients-view', 'triage-view', 'operations-view', 'billing-view', 'reports-view'];
  const target = view === 'overview' ? 'overview-panel' : `${view}-view`;

  viewMap.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.classList.toggle('hidden', id !== target);
  });

  const overviewHeader = document.querySelector('.page-heading');
  const overviewSections = document.querySelectorAll('.stats-grid, .dashboard-grid, .panel.insight-panel');
  if (overviewHeader) overviewHeader.classList.toggle('hidden', view !== 'overview');
  overviewSections.forEach(element => element.classList.toggle('hidden', view !== 'overview'));

  if (view === 'reports') loadReports();

  document.querySelectorAll('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.view === view));
  const label = (view || 'overview').charAt(0).toUpperCase() + (view || 'overview').slice(1).replace('-', ' ');
  $('#page-name').textContent = label;
}

function openPatientModal() { $('#patient-modal').classList.remove('hidden'); $('#full-name').focus(); }
function closePatientModal() { $('#patient-modal').classList.add('hidden'); $('#patient-form').reset(); $('#patient-error').textContent = ''; }

document.addEventListener('click', event => { const viewButton = event.target.closest('[data-view]'); if (viewButton) showView(viewButton.dataset.view); });
$('#new-patient-button').addEventListener('click', openPatientModal); $('#patients-new-button').addEventListener('click', openPatientModal); $('#quick-patient').addEventListener('click', openPatientModal); $('#close-modal').addEventListener('click', closePatientModal); $('#refresh-button').addEventListener('click', loadData); $('#patient-search').addEventListener('input', renderPatients);
$('#patient-modal').addEventListener('click', event => { if (event.target.id === 'patient-modal') closePatientModal(); });

$('#patient-form').addEventListener('submit', async event => {
  event.preventDefault(); $('#patient-error').textContent = '';
  const patient = { fullName: $('#full-name').value, dateOfBirth: $('#date-of-birth').value || null, gender: $('#gender').value, phone: $('#phone').value, address: $('#address').value, bloodGroup: $('#blood-group').value };
  try { await api('/api/patients', { method: 'POST', body: JSON.stringify(patient) }); closePatientModal(); await loadData(); showView('patients'); } catch (error) { $('#patient-error').textContent = 'Could not create the record. Check that the API is running.'; }
});

$('#triage-form').addEventListener('submit', async event => {
  event.preventDefault(); const result = $('#triage-result');
  const value = id => document.getElementById(id).value;
  const payload = { patientId: Number(value('triage-patient')), symptoms: value('symptoms'), heartRate: Number(value('heart-rate')) || null, spo2: Number(value('spo2')) || null, systolicBp: Number(value('systolic-bp')) || null, diastolicBp: Number(value('diastolic-bp')) || null, temperatureCelsius: Number(value('temperature')) || null, respiratoryRate: Number(value('resp-rate')) || null };
  try { const response = await api('/api/triage/evaluate', { method: 'POST', body: JSON.stringify(payload) }); const priority = response.triagePriority.toLowerCase(); result.className = `triage-result ${priority}`; result.innerHTML = `<strong>${response.triagePriority}</strong><br>Score ${safe(response.triageScore)}. ${response.flaggedReasons?.join(', ') || 'No flagged risk factors.'}`; await loadData(); } catch (error) { result.className = 'triage-result emergency'; result.textContent = 'Assessment failed. Check the patient selection and API connection.'; }
});

async function submitResource(event, endpoint, payload, errorSelector, afterSave = loadData) {
  event.preventDefault(); $(errorSelector).textContent = '';
  try { await api(endpoint, { method: 'POST', body: JSON.stringify(payload) }); event.target.reset(); await afterSave(); }
  catch (error) { $(errorSelector).textContent = 'Could not save. Check the required fields and database connection.'; }
}

$('#department-form').addEventListener('submit', event => submitResource(event, '/api/departments', { name: $('#department-name').value, location: $('#department-location').value, monthlyBudget: Number($('#department-budget').value) || 0 }, '#department-error'));
$('#doctor-form').addEventListener('submit', event => submitResource(event, '/api/doctors', { fullName: $('#doctor-name').value, specialization: $('#doctor-specialization').value, phone: $('#doctor-phone').value, department: $('#doctor-department').value ? { departmentId: Number($('#doctor-department').value) } : null }, '#doctor-error'));
$('#product-form').addEventListener('submit', event => submitResource(event, '/api/products', { name: $('#product-name').value, category: $('#product-category').value, unitPrice: Number($('#product-price').value), isService: $('#product-service').value === 'true' }, '#product-error'));
$('#invoice-form').addEventListener('submit', event => submitResource(event, '/api/invoices', { patientId: Number($('#invoice-patient').value), lines: [{ productId: Number($('#invoice-product').value), quantity: Number($('#invoice-quantity').value) }] }, '#invoice-error'));
$('#payment-form').addEventListener('submit', event => submitResource(event, '/api/payments', { invoiceId: Number($('#payment-invoice').value), amount: Number($('#payment-amount').value), method: $('#payment-method').value }, '#payment-error'));
$('#reports-refresh').addEventListener('click', loadReports);

$('#today').textContent = new Intl.DateTimeFormat('en', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date());
loadData();