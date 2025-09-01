<%@ page contentType="text/html; charset=UTF-8" %>

<%@ include file="_layout.jspf" %>
<div class="container py-4">
  <main class="container-fluid py-4">
    <h3 class="mb-4"><c:out value="${u == null ? 'Créer un employé' : 'Éditer un employé'}"/></h3>

    <form method="post" action="${pageContext.request.contextPath}/admin/users/save" class="row g-3">
      <c:if test="${u != null}">
        <input type="hidden" name="id" value="${u.id}">
      </c:if>

      <div class="col-md-6">
        <label class="form-label">Prénom</label>
        <input type="text" name="prenom" class="form-control" value="${u != null ? u.prenom : ''}" required>
      </div>
      <div class="col-md-6">
        <label class="form-label">Nom</label>
        <input type="text" name="nom" class="form-control" value="${u != null ? u.nom : ''}" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">Email</label>
        <input type="email" name="email" class="form-control" value="${u != null ? u.email : ''}" required>
      </div>
      <div class="col-md-6">
        <label class="form-label">Mot de passe</label>
        <input type="password" name="mot_de_passe" class="form-control" placeholder="${u != null ? 'laisser vide pour conserver' : ''}">
      </div>

      <!-- Poste -->
      <div class="col-md-4">
        <label class="form-label">Poste</label>
        <select name="poste_id" class="form-select">
          <option value="">—</option>
          <c:forEach var="p" items="${postes}">
            <option value="${p.id}" <c:if test="${u != null && u.poste != null && u.poste.id == p.id}">selected</c:if>>
                ${p.intitule}
            </option>
          </c:forEach>
        </select>
      </div>

      <!-- Département -->
      <div class="col-md-4">
        <label class="form-label">Département</label>
        <select name="departement_id" class="form-select">
          <option value="">—</option>
          <c:forEach var="d" items="${departements}">
            <option value="${d.id}" <c:if test="${u != null && u.departement != null && u.departement.id == d.id}">selected</c:if>>
                ${d.nom}
            </option>
          </c:forEach>
        </select>
      </div>

      <!-- Manager -->
      <div class="col-md-4">
        <label class="form-label">Manager</label>
        <select name="manager_id" class="form-select">
          <option value="">—</option>
          <c:forEach var="m" items="${managers}">
            <option value="${m.id}" <c:if test="${u != null && u.manager != null && u.manager.id == m.id}">selected</c:if>>
                ${m.prenom} ${m.nom}
            </option>
          </c:forEach>
        </select>
      </div>

      <div class="col-md-4">
        <label class="form-label">Statut</label>
        <input type="text" name="statut" class="form-control" value="${u != null ? u.statut : ''}">
      </div>

      <div class="col-md-4">
        <label class="form-label">Type de contrat</label>
        <input type="text" name="contrat_type" class="form-control" value="${u != null ? u.contratType : ''}">
      </div>

      <div class="col-md-4">
        <label class="form-label">Salaire de base (€)</label>
        <input type="number" step="0.01" name="salaire_base" class="form-control"
               value="${u != null && u.salaireBase != null ? u.salaireBase : ''}">
      </div>

      <div class="col-md-6">
        <label class="form-label">Date d’embauche</label>
        <input type="date" name="date_embauche" class="form-control"
               value="<c:out value='${u != null ? u.dateEmbauche : ""}'/>">
      </div>
      <div class="col-md-6">
        <label class="form-label">Date de sortie</label>
        <input type="date" name="date_sortie" class="form-control"
               value="<c:out value='${u != null ? u.dateSortie : ""}'/>">
      </div>

      <div class="col-md-6">
        <label class="form-label">Adresse</label>
        <input type="text" name="adresse" class="form-control" value="${u != null ? u.adresse : ''}">
      </div>
      <div class="col-md-6">
        <label class="form-label">Téléphone</label>
        <input type="text" name="telephone" class="form-control" value="${u != null ? u.telephone : ''}">
      </div>

      <div class="col-12 d-flex gap-2">
        <button class="btn btn-primary" type="submit">Enregistrer</button>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/users">Annuler</a>
      </div>
    </form>
  </main>
</div>
