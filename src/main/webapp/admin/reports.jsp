<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Rapports</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4>Rapports</h4>

  <div class="card shadow-sm mb-3">
    <div class="card-body">
      <h5 class="card-title">Utilisateurs</h5>
      <p class="text-muted">Export CSV compatible Excel.</p>
      <a class="btn btn-primary" href="<c:url value='/admin/reports/users.csv'/>">Télécharger Users.csv</a>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-body">
      <h5 class="card-title">Congés</h5>
      <form class="row g-2" method="get" action="<c:url value='/admin/reports/leaves.csv'/>">
        <div class="col-md-3">
          <label class="form-label">Du</label>
          <input class="form-control" type="date" name="start">
        </div>
        <div class="col-md-3">
          <label class="form-label">Au</label>
          <input class="form-control" type="date" name="end">
        </div>
        <div class="col-md-3">
          <label class="form-label">Statut</label>
          <select class="form-select" name="statut">
            <option value="">(tous)</option>
            <option value="en_attente">En attente</option>
            <option value="approuve">Approuvé</option>
            <option value="rejete">Rejeté</option>
          </select>
        </div>
        <div class="col-md-3 d-flex align-items-end">
          <button class="btn btn-primary w-100">Télécharger Leaves.csv</button>
        </div>
      </form>
    </div>
  </div>

</div>
</body></html>
