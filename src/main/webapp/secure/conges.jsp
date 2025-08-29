<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!doctype html>
<html lang="fr"><head>
    <meta charset="utf-8">
    <title>Mes congés</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>:root{ --bs-primary:#24af9c; }</style>
</head>
<body class="bg-body-tertiary">
<nav class="navbar" style="background:#24af9c;">
    <div class="container">
        <a class="navbar-brand text-white" href="<c:url value='/'/>">GestRH</a>
        <div class="d-flex gap-2">
            <a class="btn btn-sm btn-light" href="<c:url value='/secure/presences'/>">Présences</a>
            <a class="btn btn-sm btn-outline-light" href="<c:url value='/auth/logout'/>">Logout</a>
        </div>
    </div>
</nav>

<div class="container py-4">
    <h4 class="mb-3">Demander un congé</h4>

    <c:if test="${param.ok == '1'}">
        <div class="alert alert-success">Demande enregistrée.</div>
    </c:if>
    <c:if test="${param.err == '1'}">
        <div class="alert alert-danger">Impossible d’enregistrer la demande. Vérifiez les champs.</div>
    </c:if>

    <!-- CREATE FORM (posts to /secure/conges/create) -->
    <form method="post" action="<c:url value='/secure/conges/create'/>" class="row g-3 mb-4">
        <div class="col-md-4">
            <label class="form-label">Type</label>
            <select name="typeId" class="form-select" required>
                <c:forEach var="t" items="${types}">
                    <option value="${t.id}">${t.libelle}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3">
            <label class="form-label">Du</label>
            <input type="date" name="dateDebut" class="form-control" required>
        </div>
        <div class="col-md-3">
            <label class="form-label">Au</label>
            <input type="date" name="dateFin" class="form-control" required>
        </div>
        <div class="col-md-8">
            <label class="form-label">Motif (optionnel)</label>
            <input type="text" name="motif" class="form-control" placeholder="Motif…">
        </div>
        <div class="col-12">
            <button class="btn btn-primary">Envoyer la demande</button>
            <a class="btn btn-outline-secondary ms-2" href="<c:url value='/secure/reports/my-leaves.csv'/>">Télécharger mon historique (CSV)</a>
        </div>
    </form>

    <h5 class="mb-2">Mes demandes</h5>
    <div class="table-responsive">
        <table class="table table-sm align-middle">
            <thead><tr>
                <th>#</th><th>Type</th><th>Du</th><th>Au</th><th>Jours</th><th>Statut</th><th>Motif</th>
            </tr></thead>
            <tbody>
            <c:forEach var="c" items="${conges}">
                <tr>
                    <td>${c.id}</td>
                    <td>${c.type.libelle}</td>
                    <td>${c.dateDebut}</td>
                    <td>${c.dateFin}</td>
                    <td>${c.nbJours}</td>
                    <td>
                        <c:choose>
                            <c:when test="${c.statut=='approuve'}"><span class="badge bg-success">Approuvé</span></c:when>
                            <c:when test="${c.statut=='rejete'}"><span class="badge bg-danger">Rejeté</span></c:when>
                            <c:otherwise><span class="badge bg-warning text-dark">En attente</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${c.motif}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty conges}">
                <tr><td colspan="7" class="text-muted">Aucune demande.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>
</body></html>
