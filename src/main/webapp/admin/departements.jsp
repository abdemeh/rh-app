<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="_layout.jspf" %>

<div class="container py-4">
  <main class="container-fluid py-4">
    <h3 class="mb-4">Départements</h3>
    <c:if test="${param.success == 'created'}">
      <div class="alert alert-success">Département ajouté avec succès.</div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
      <div class="alert alert-success">Modifications enregistrées.</div>
    </c:if>
    <c:if test="${param.success == 'deleted'}">
      <div class="alert alert-danger">Département supprimé.</div>
    </c:if>

    <!-- Create -->
    <form method="post" action="${pageContext.request.contextPath}/admin/departements" class="row g-2 mb-4">
      <input type="hidden" name="action" value="create">
      <div class="col-md-5">
        <input name="nom_departement" class="form-control" placeholder="Nom du département" required>
      </div>
      <div class="col-md-5">
        <select name="responsable_id" class="form-select">
          <option value="">— Responsable (optionnel) —</option>
          <c:forEach var="u" items="${users}">
            <option value="${u.id}"><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-2">
        <button class="btn btn-primary w-100" type="submit">Ajouter</button>
      </div>
    </form>

    <!-- List -->
    <div class="table-responsive">
      <table class="table table-striped table-hover align-middle">
        <thead class="table-dark">
        <tr>
          <th>#</th>
          <th>Nom</th>
          <th>Responsable</th>
          <th class="text-end">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="d" items="${departements}">
          <tr>
            <td>${d.id}</td>

            <!-- Inline update form per row -->
            <td style="min-width:280px">
              <form method="post" action="${pageContext.request.contextPath}/admin/departements" class="row g-2">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="${d.id}">
                <div class="col-12">
                  <input name="nom_departement" class="form-control form-control-sm" value="${d.nom}" required>
                </div>
            </td>
            <td style="min-width:260px">
              <select name="responsable_id" class="form-select form-select-sm">
                <option value="">— Aucun —</option>
                <c:forEach var="u" items="${users}">
                  <option value="${u.id}"
                          <c:if test="${d.responsable != null && d.responsable.id == u.id}">selected</c:if>>
                    <c:out value="${u.prenom}"/> <c:out value="${u.nom}"/>
                  </option>
                </c:forEach>
              </select>
            </td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button>
              </form>

              <!-- Soft delete -->
              <form method="post" action="${pageContext.request.contextPath}/admin/departements" class="d-inline">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${d.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit"
                        onclick="return confirm('Supprimer ce département ?');">Supprimer</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </main>
</div>
