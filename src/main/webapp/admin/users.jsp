<%@ page contentType="text/html; charset=UTF-8" %>

<%@ include file="_layout.jspf" %>

<main class="container-fluid py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="m-0">Utilisateurs</h3>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users/edit">+ Nouvel utilisateur</a>
  </div>

  <c:choose>
    <c:when test="${empty users}">
      <div class="alert alert-info">Aucun utilisateur trouvé.</div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
          <thead class="table-dark">
          <tr>
            <th>#</th>
            <th>Nom</th>
            <th>Email</th>
            <th>Poste</th>
            <th>Département</th>
            <th>Manager</th>
            <th>Statut</th>
            <th>Contrat</th>
            <th>Date embauche</th>
            <th>Date sortie</th>
            <th>Salaire</th>
            <th class="text-end">Actions</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="u" items="${users}">
            <tr>
              <td><c:out value="${u.id}"/></td>
              <td><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/></td>
              <td><c:out value="${u.email}"/></td>

              <td><c:out value="${u.poste != null ? u.poste.intitule : '-'}"/></td>
              <td><c:out value="${u.departement != null ? u.departement.nom : '-'}"/></td>

              <td>
                <c:choose>
                  <c:when test="${u.manager != null}">
                    <c:out value="${u.manager.prenom}"/> <c:out value="${u.manager.nom}"/>
                  </c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>

              <td><c:out value="${u.statut}"/></td>
              <td><c:out value="${u.contratType}"/></td>
              <td><c:out value="${u.dateEmbauche}"/></td>
              <td><c:out value="${u.dateSortie}"/></td>
              <td>
                <c:choose>
                  <c:when test="${u.salaireBase != null}">
                    <c:out value="${u.salaireBase}"/> €
                  </c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>

              <td class="text-end">
                <a class="btn btn-sm btn-outline-primary"
                   href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}">Éditer</a>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</main>
