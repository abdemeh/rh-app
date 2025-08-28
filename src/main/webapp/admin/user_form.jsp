<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Utilisateur</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4><c:if test="${u.id==null}">Créer</c:if><c:if test="${u.id!=null}">Editer</c:if> un utilisateur</h4>
  <form method="post" action="<c:url value='/admin/users/save'/>" class="row g-3">
    <input type="hidden" name="id" value="${u.id}"/>
    <div class="col-md-6">
      <label class="form-label">Prénom</label>
      <input class="form-control" name="prenom" value="${u.prenom}" required>
    </div>
    <div class="col-md-6">
      <label class="form-label">Nom</label>
      <input class="form-control" name="nom" value="${u.nom}" required>
    </div>
    <div class="col-md-6">
      <label class="form-label">Email</label>
      <input class="form-control" type="email" name="email" value="${u.email}" required>
    </div>
    <div class="col-md-6">
      <label class="form-label">Mot de passe (laisser vide pour ne pas changer)</label>
      <input class="form-control" type="password" name="password">
    </div>
    <div class="col-md-6">
      <label class="form-label">Statut</label>
      <select name="statut" class="form-select">
        <option ${u.statut=='actif'?'selected':''}>actif</option>
        <option ${u.statut=='inactif'?'selected':''}>inactif</option>
        <option ${u.statut=='suspendu'?'selected':''}>suspendu</option>
      </select>
    </div>
    <div class="col-md-6">
      <label class="form-label">Département</label>
      <select name="departementId" class="form-select">
        <option value="">(aucun)</option>
        <c:forEach var="d" items="${depts}">
          <option value="${d.id}" ${u.departement != null && u.departement.id == d.id ? 'selected' : ''}>${d.nom}</option>
        </c:forEach>
      </select>
    </div>
    <div class="col-12 d-flex gap-2">
      <button class="btn btn-primary">Enregistrer</button>
      <a class="btn btn-secondary" href="<c:url value='/admin/users'/>">Annuler</a>
    </div>
  </form>
</div>
</body></html>
