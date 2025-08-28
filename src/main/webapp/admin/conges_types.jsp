<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Admin – Types de congés</title>
<%@ include file="/admin/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
  <h4>Types de congés</h4>
  <form method="post" class="row g-2 my-3">
    <input type="hidden" name="id" value=""/>
    <div class="col-md-2"><input class="form-control" name="code" placeholder="Code" required></div>
    <div class="col-md-4"><input class="form-control" name="libelle" placeholder="Libellé" required></div>
    <div class="col-md-2"><input class="form-control" name="maxJoursAn" placeholder="Max/an (ex: 25)"></div>
    <div class="col-md-2">
      <input class="form-control" name="approvalLevels" value="1" type="number" min="1" max="5">
    </div>
    <div class="col-md-1 form-check mt-2">
      <input class="form-check-input" type="checkbox" name="requiresDoc" id="reqdoc">
      <label class="form-check-label" for="reqdoc">Doc</label>
    </div>
    <div class="col-md-1"><button class="btn btn-primary w-100">Save</button></div>
  </form>

  <div class="table-responsive">
    <table class="table table-sm">
      <thead><tr><th>#</th><th>Code</th><th>Libellé</th><th>Max/an</th><th>Niveaux</th><th>Doc</th></tr></thead>
      <tbody>
      <c:forEach var="t" items="${types}">
        <tr>
          <td>${t.id}</td><td>${t.code}</td><td>${t.libelle}</td>
          <td>${t.maxJoursAn}</td><td>${t.approvalLevels}</td>
          <td><c:out value="${t.requiresDoc}"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</div>
</body></html>
