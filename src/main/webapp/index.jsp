<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%
    // Auth guard
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    // Role check (set at login via SecurityUtil.loadRoles(...))
    java.util.Set roles = (java.util.Set) session.getAttribute("roles");
    boolean isAdmin = (roles != null && roles.contains("ADMIN"));
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Dashboard – GestRH</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>:root{ --bs-primary:#24af9c; }</style>
</head>
<body class="bg-body-tertiary">

<nav class="navbar navbar-expand-lg" style="background:#24af9c;">
    <div class="container">
        <a class="navbar-brand text-white" href="#">GestRH</a>
        <div class="d-flex gap-2">
            <a class="btn btn-sm btn-light" href="<%= ctx %>/secure/conges">Congés</a>
            <a class="btn btn-sm btn-light" href="<%= ctx %>/secure/presences">Présences</a>
            <% if (isAdmin) { %>
            <a class="btn btn-sm btn-warning" href="<%= ctx %>/admin/">Admin</a>
            <% } %>
            <a class="btn btn-sm btn-outline-light" href="<%= ctx %>/auth/logout">Logout</a>
        </div>
        <span class="navbar-text text-white ms-auto">
      Welcome, <%= session.getAttribute("userName") %>
    </span>
    </div>
</nav>

<div class="container py-4">
    <div class="row g-3">
        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h5 class="card-title">Congés</h5>
                    <p class="card-text">Demander un congé, voir le statut, solde…</p>
                    <a class="btn btn-outline-primary" href="<%= ctx %>/secure/conges">Ouvrir</a>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h5 class="card-title">Présences</h5>
                    <p class="card-text">Suivi des présences du jour.</p>
                    <a class="btn btn-outline-primary" href="<%= ctx %>/secure/presences">Ouvrir</a>
                </div>
            </div>
        </div>

        <% if (isAdmin) { %>
        <div class="col-md-4">
            <div class="card shadow-sm h-100 border-warning">
                <div class="card-body">
                    <h5 class="card-title">Administration</h5>
                    <p class="card-text">Utilisateurs, départements, congés en attente, types de congés…</p>
                    <a class="btn btn-warning" href="<%= ctx %>/admin/">Entrer</a>
                </div>
            </div>
        </div>
        <% } %>
    </div>
</div>
</body>
</html>
