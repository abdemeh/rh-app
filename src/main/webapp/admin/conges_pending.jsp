<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  if (session == null || session.getAttribute("userId") == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  java.util.Set roles = (java.util.Set) session.getAttribute("roles");
  if (roles == null || !roles.contains("ADMIN")) {
    response.sendError(403); return;
  }
%>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Congés en attente</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4>Congés en attente</h4>
  <div class="table-responsive">
    <table class="table table-sm align-middle">
      <thead><tr><th>#</th><th>Employé</th><th>Type</th><th>Du</th><th>Au</th><th>Jours</th><th>Action</th></tr></thead>
      <tbody>
      <c:forEach var="c" items="${list}">
        <tr>
          <td>${c.id}</td>
          <td>${c.utilisateur.prenom} ${c.utilisateur.nom}</td>
          <td>${c.type.libelle}</td>
          <td>${c.dateDebut}</td>
          <td>${c.dateFin}</td>
          <td>${c.nbJours}</td>
          <td>
            <form method="post" action="<c:url value='/admin/conges/approve'/>" class="d-inline">
              <input type="hidden" name="id" value="${c.id}">
              <button class="btn btn-sm btn-outline-success" name="action" value="approuve">Approuver</button>
              <button class="btn btn-sm btn-outline-danger"  name="action" value="rejete">Rejeter</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty list}">
        <tr><td colspan="7" class="text-muted">Aucune demande en attente.</td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</div>
</body></html>
