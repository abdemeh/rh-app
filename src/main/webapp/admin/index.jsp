<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Dashboard</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
    <div class="row g-3">
        <div class="col-md-4">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="h5">Utilisateurs</div>
                <p class="display-6">${usersCount}</p>
                <a class="btn btn-outline-primary" href="<c:url value='/admin/users'/>">Gérer</a>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="h5">Départements</div>
                <p class="display-6">${deptCount}</p>
                <a class="btn btn-outline-primary" href="<c:url value='/admin/departements'/>">Gérer</a>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card shadow-sm h-100"><div class="card-body">
                <div class="h5">Congés en attente</div>
                <p class="display-6">${pendingCount}</p>
                <a class="btn btn-outline-primary" href="<c:url value='/admin/conges/pending'/>">Voir</a>
            </div></div>
        </div>
    </div>

    <div class="mt-4">
        <a class="btn btn-primary" href="<c:url value='/admin/conges/types'/>">Types de congés</a>
    </div>
</div>
</body></html>
