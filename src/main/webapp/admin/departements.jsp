<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Départements</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4>Départements</h4>
  <form method="post" class="d-flex gap-2 my-3">
    <input class="form-control" name="nom" placeholder="Nouveau département..." required>
    <button class="btn btn-primary">Ajouter</button>
  </form>
  <ul class="list-group">
    <c:forEach var="d" items="${depts}">
      <li class="list-group-item d-flex justify-content-between align-items-center">
          ${d.nom}
        <!-- (Add edit/delete later) -->
      </li>
    </c:forEach>
  </ul>
</div>
</body></html>
