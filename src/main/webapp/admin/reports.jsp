<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/admin/_layout.jspf" %>
<div class="container py-4">
  <main class="container-fluid py-4">
    <h3 class="mb-4">Rapports</h3>

    <c:if test="${not empty error}">
      <div class="alert alert-danger" role="alert">${error}</div>
    </c:if>

    <!-- A) PDF — Congés & Rémunération par utilisateur/période -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title mb-3">État PDF : Congés & Rémunération par utilisateur</h5>

        <form method="get" action="${pageContext.request.contextPath}/admin/reports" target="_blank" class="row g-3">
          <input type="hidden" name="action" value="pdf"/>

          <div class="col-md-4">
            <label class="form-label">Utilisateur</label>
            <select name="user_id" class="form-select" required>
              <option value="">— Sélectionner —</option>
              <c:forEach var="u" items="${users}">
                <option value="${u.id}">
                  <c:out value="${u.prenom}"/> <c:out value="${u.nom}"/> — <c:out value="${u.email}"/>
                </option>
              </c:forEach>
            </select>
          </div>

          <div class="col-md-3">
            <label class="form-label">Du</label>
            <input type="date" class="form-control" name="start_date" required/>
          </div>

          <div class="col-md-3">
            <label class="form-label">Au</label>
            <input type="date" class="form-control" name="end_date" required/>
          </div>

          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-primary w-100" type="submit">Générer le PDF</button>
          </div>
        </form>
      </div>
    </div>

    <div class="row g-4 mb-4">
      <!-- B) PDF — Employés par Département -->
      <div class="col-lg-6">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title mb-3">PDF : Employés par Département</h5>
            <form method="get" action="${pageContext.request.contextPath}/admin/reports" target="_blank" class="row g-3">
              <input type="hidden" name="action" value="pdf_users_by_dept"/>
              <div class="col-md-8">
                <label class="form-label">Département</label>
                <select name="dept_id" class="form-select" required>
                  <option value="">— Sélectionner —</option>
                  <c:forEach var="d" items="${departements}">
                    <option value="${d.id}"><c:out value="${d.nom}"/></option>
                  </c:forEach>
                </select>
              </div>
              <div class="col-md-4 d-flex align-items-end">
                <button class="btn btn-outline-primary w-100" type="submit">Générer PDF</button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- C) PDF — Employés par Poste -->
      <div class="col-lg-6">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title mb-3">PDF : Employés par Poste</h5>
            <form method="get" action="${pageContext.request.contextPath}/admin/reports" target="_blank" class="row g-3">
              <input type="hidden" name="action" value="pdf_users_by_poste"/>
              <div class="col-md-8">
                <label class="form-label">Poste</label>
                <select name="poste_id" class="form-select" required>
                  <option value="">— Sélectionner —</option>
                  <c:forEach var="p" items="${postes}">
                    <option value="${p.id}"><c:out value="${p.intitule}"/></option>
                  </c:forEach>
                </select>
              </div>
              <div class="col-md-4 d-flex align-items-end">
                <button class="btn btn-outline-primary w-100" type="submit">Générer PDF</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- D) PDF — Salaires d’un mois donné -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title mb-3">PDF : Salaires — mois donné</h5>
        <form method="get" action="${pageContext.request.contextPath}/admin/reports" target="_blank" class="row g-3">
          <input type="hidden" name="action" value="pdf_salaries_month"/>
          <div class="col-md-3">
            <label class="form-label">Mois (yyyy-MM)</label>
            <input type="month" class="form-control" name="month" required/>
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-outline-primary w-100" type="submit">Générer PDF</button>
          </div>
        </form>
        <div class="form-text mt-2">
          Le PDF affiche le <strong>total à payer</strong> et le <strong>détail par employé</strong> (jours & montant).
        </div>
      </div>
    </div>

  </main>
</div>
