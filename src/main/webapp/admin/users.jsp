<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Utilisateurs</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h4>Utilisateurs</h4>
    <a class="btn btn-primary" href="<c:url value='/admin/users/edit'/>">Créer</a>
  </div>
  <div class="table-responsive">
    <table class="table table-sm align-middle">
      <thead><tr><th>#</th><th>Nom</th><th>Email</th><th>Statut</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="u" items="${users}">
        <tr>
          <td>${u.id}</td>
          <td>${u.prenom} ${u.nom}</td>
          <td>${u.email}</td>
          <td>${u.statut}</td>
          <td><a class="btn btn-sm btn-outline-secondary" href="<c:url value='/admin/users/edit?id=${u.id}'/>">Editer</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</div>
</body></html>
