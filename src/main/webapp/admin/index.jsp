<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Admin – Dashboard</title>
    <%@ include file="/admin/_layout.jspf" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1"></script>
    <style>
        .chart-card { height: 340px; }
        .chart-card canvas { height: 260px !important; }
    </style>
</head>
<body class="bg-body-tertiary">
<div class="container py-4 mb-4">

    <!-- ====== Widgets ====== -->
    <div class="row g-3 mb-3">
        <div class="col-md-3">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="h6 text-muted mb-0">Employés</div>
                    <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/users'/>">Gérer</a>
                </div>
                <div class="display-6 fw-bold mt-2">${usersCount}</div>
            </div></div>
        </div>

        <div class="col-md-3">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="h6 text-muted mb-0">Départements</div>
                    <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/departements'/>">Gérer</a>
                </div>
                <div class="display-6 fw-bold mt-2">${deptCount}</div>
            </div></div>
        </div>

        <div class="col-md-3">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="h6 text-muted mb-0">Congés en attente</div>
                    <a class="btn btn-sm btn-outline-primary" href="<c:url value='/admin/conges/pending'/>">Afficher</a>
                </div>
                <div class="display-6 fw-bold mt-2">${pendingCount}</div>
            </div></div>
        </div>

        <div class="col-md-3">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="h6 text-muted mb-1">Aujourd'hui – Présences</div>
                <div class="display-6 fw-bold mt-2">${presencesToday}</div>
                <div class="small text-muted">
                    Ce mois&nbsp;: ${congesThisMonth} congés • Masse salariale ≈
                    <strong><c:out value="${payrollThisMonth}"/></strong>
                </div>
            </div></div>
        </div>
    </div>

    <!-- ====== Contrôles période ====== -->
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h5 class="mb-0">Statistiques</h5>
        <div class="d-flex align-items-center gap-2">
            <label class="small text-muted">Période&nbsp;:</label>
            <select id="months" class="form-select form-select-sm" style="width:auto">
                <option value="3">3 mois</option>
                <option value="6">6 mois</option>
                <option value="12" selected>12 mois</option>
                <option value="24">24 mois</option>
            </select>
        </div>
    </div>

    <!-- ====== Charts ====== -->
    <div class="row g-3">
        <!-- Line -->
        <div class="col-lg-9">
            <div class="card shadow-sm chart-card"><div class="card-body">
                <div class="d-flex align-items-center justify-content-between">
                    <div class="h6 mb-0">Activité mensuelle</div>
                    <span id="rangeLabel" class="small text-muted"></span>
                </div>
                <canvas id="lineChart" class="mt-3"></canvas>
            </div></div>
        </div>

        <!-- Doughnut -->
        <div class="col-lg-3">
            <div class="card shadow-sm chart-card"><div class="card-body">
                <div class="h6 mb-0">Répartition des congés</div>
                <canvas id="doughnutChart" class="mt-3"></canvas>
            </div></div>
        </div>

        <!-- Bar -->
        <div class="col-12">
            <div class="card shadow-sm chart-card"><div class="card-body">
                <div class="h6 mb-0">Employés par département</div>
                <canvas id="barChart" class="mt-3"></canvas>
            </div></div>
        </div>
    </div>

</div>

<script>
    const ctxLine   = document.getElementById('lineChart');
    const ctxDonut  = document.getElementById('doughnutChart');
    const ctxBar    = document.getElementById('barChart');
    const monthsSel = document.getElementById('months');
    const rangeLabel = document.getElementById('rangeLabel');

    // ✅ Toujours frapper le servlet (pas le JSP)
    const DASH_ENDPOINT = '<c:url value="/admin"/>';

    let lineChart, donutChart, barChart;

    // ✅ Bloquer toute soumission auto si le select est dans un <form>
    document.addEventListener('submit', (e) => {
        if (e.target && e.target.contains(monthsSel)) e.preventDefault();
    }, true);

    async function loadMetrics() {
        const months = monthsSel.value;

        const res = await fetch(`${DASH_ENDPOINT}?format=json&months=${months}`, {
            headers: { 'Accept': 'application/json' },
            cache: 'no-store' // ✅ pas de cache
        });

        if (!res.ok) {
            console.error('Erreur fetch metrics', res.status, await res.text());
            return;
        }
        const data = await res.json();

        rangeLabel.textContent = `Derniers ${months} mois`;

        const lineData = {
            labels: data.labels,
            datasets: [
                { label: 'Congés', data: data.series.conges, tension: .3 },
                { label: 'Présences', data: data.series.presences, tension: .3 },
                { label: 'Masse salariale (est.)', data: data.series.payroll, tension: .3 }
            ]
        };

        const donutData = {
            labels: ['Approuvé', 'Rejeté', 'En attente'],
            datasets: [{ data: [data.congesStatus.approuve, data.congesStatus.rejete, data.congesStatus.en_attente] }]
        };

        const barData = {
            labels: data.usersByDept.labels,
            datasets: [{ label: 'Employés', data: data.usersByDept.counts }]
        };

        if (lineChart)  lineChart.destroy();
        if (donutChart) donutChart.destroy();
        if (barChart)   barChart.destroy();

        lineChart  = new Chart(ctxLine,  { type: 'line',    data: lineData,  options: { responsive: true, maintainAspectRatio: false }});
        donutChart = new Chart(ctxDonut, { type: 'doughnut', data: donutData, options: { responsive: true, maintainAspectRatio: false }});
        barChart   = new Chart(ctxBar,   { type: 'bar',     data: barData,   options: { responsive: true, maintainAspectRatio: false }});
    }

    // ✅ Empêcher propagation + rechargement si un handler global existe
    monthsSel.addEventListener('change', (e) => {
        e.preventDefault();
        e.stopPropagation();
        loadMetrics();
    });

    document.addEventListener('DOMContentLoaded', () => {
        const def = '${defaultMonths}';
        if (def) {
            for (const o of monthsSel.options) if (o.value === def) o.selected = true;
        }
        loadMetrics();
    });
</script>


</body>
</html>
