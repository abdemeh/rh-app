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
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<style>
  .pdf-frame { width:100%; height:480px; border:1px solid #dee2e6; border-radius:.5rem; background:#fff; }
</style>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4>Congés en attente</h4>

  <div class="table-responsive">
    <table class="table table-sm align-middle">
      <thead>
      <tr>
        <th>#</th>
        <th>Employé</th>
        <th>Type</th>
        <th>Du</th>
        <th>Au</th>
        <th>Jours</th>
        <th>Justificatif</th>
        <th>Action</th>
      </tr>
      </thead>
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
            <c:choose>
              <c:when test="${not empty c.justificatifPath}">
                <!-- Open in new tab -->
                <a class="btn btn-sm btn-outline-secondary" target="_blank"
                   href="<c:url value='/file/conge?id=${c.id}'/>">Ouvrir</a>
                <!-- Toggle inline preview -->
                <button class="btn btn-sm btn-outline-primary"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#preview-${c.id}"
                        aria-expanded="false"
                        aria-controls="preview-${c.id}">
                  Prévisualiser
                </button>
              </c:when>
              <c:otherwise>
                <span class="badge text-bg-secondary">Aucun</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <form method="post" action="<c:url value='/admin/conges/approve'/>" class="d-inline">
              <input type="hidden" name="id" value="${c.id}">
              <button class="btn btn-sm btn-outline-success" name="action" value="approuve">Approuver</button>
              <button class="btn btn-sm btn-outline-danger"  name="action" value="rejete">Rejeter</button>
            </form>
          </td>
        </tr>

        <!-- Inline PDF preview row (collapsible) -->
        <c:if test="${not empty c.justificatifPath}">
          <tr class="collapse" id="preview-${c.id}">
            <td colspan="8">
              <div class="pdf-frame">
                <iframe class="pdf-frame"
                        src="<c:url value='/file/conge?id=${c.id}'/>#toolbar=1&navpanes=0"
                        title="Justificatif ${c.id}">
                </iframe>
              </div>
            </td>
          </tr>
        </c:if>
      </c:forEach>

      <c:if test="${empty list}">
        <tr><td colspan="8" class="text-muted">Aucune demande en attente.</td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</div>
</body></html>
