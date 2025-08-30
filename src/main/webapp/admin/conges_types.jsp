<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="_layout.jspf" %>
<div class="container py-4">
  <main class="container-fluid py-4">
    <h3 class="mb-4">Types de congés</h3>

    <!-- Create -->
    <form method="post" action="${pageContext.request.contextPath}/admin/conges/types" class="row g-2 mb-4">
      <input type="hidden" name="action" value="create">

      <div class="col-md-2">
        <input name="code" class="form-control" placeholder="Code (ex: ANN)" required>
      </div>
      <div class="col-md-3">
        <input name="libelle" class="form-control" placeholder="Libellé (ex: Congé annuel)" required>
      </div>
      <div class="col-md-2">
        <input name="max_jours_an" type="number" min="0" class="form-control" placeholder="Max/an">
      </div>
      <div class="col-md-2">
        <input name="approval_levels" type="number" min="0" class="form-control" placeholder="Niveaux appr.">
      </div>
      <div class="col-md-1 form-check d-flex align-items-center">
        <input class="form-check-input" type="checkbox" name="requires_doc" id="requires_doc_create">
        <label class="form-check-label ms-1" for="requires_doc_create">Justif.</label>
      </div>
      <div class="col-md-1 form-check d-flex align-items-center">
        <input class="form-check-input" type="checkbox" name="actif" id="actif_create" checked>
        <label class="form-check-label ms-1" for="actif_create">Actif</label>
      </div>
      <div class="col-md-1">
        <button class="btn btn-primary w-100" type="submit">Ajouter</button>
      </div>
    </form>

    <!-- List -->
    <div class="table-responsive">
      <table class="table table-striped table-hover align-middle">
        <thead class="table-dark">
        <tr>
          <th>#</th>
          <th>Code</th>
          <th>Libellé</th>
          <th>Max/an</th>
          <th>Niveaux</th>
          <th>Justif.</th>
          <th>Actif</th>
          <th class="text-end">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="t" items="${types}">
          <tr>
            <td>${t.id}</td>
            <td><c:out value="${t.code}"/></td>
            <td><c:out value="${t.libelle}"/></td>
            <td><c:out value="${t.maxJoursAn}"/></td>
            <td><c:out value="${t.approvalLevels}"/></td>
            <td><c:out value="${t.requiresDoc ? 'Oui' : 'Non'}"/></td>
            <td><c:out value="${t.actif ? 'Oui' : 'Non'}"/></td>
            <td class="text-end">
              <!-- Delete -->
              <form method="post" action="${pageContext.request.contextPath}/admin/conges/types" class="d-inline">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${t.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit"
                        onclick="return confirm('Supprimer ce type ?');">Supprimer</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </main>
</div>
