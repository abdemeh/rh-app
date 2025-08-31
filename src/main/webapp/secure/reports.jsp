<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="/secure/_layout.jspf" %>
<div class="container py-4">
  <main class="container-fluid py-4">
    <h3 class="mb-4">Mes rapports</h3>

    <c:if test="${not empty error}">
      <div class="alert alert-danger" role="alert">${error}</div>
    </c:if>

    <!-- =================== A) Mes congés & rémunération (PDF) =================== -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title mb-3">État PDF : Mes congés & rémunération</h5>

        <form method="get" action="${pageContext.request.contextPath}/secure/reports" target="_blank" class="row g-3">
          <input type="hidden" name="action" value="pdf_my_conges_period"/>

          <div class="col-md-3">
            <label class="form-label">Du</label>
            <input type="date" class="form-control" name="start_date" required/>
          </div>
          <div class="col-md-3">
            <label class="form-label">Au</label>
            <input type="date" class="form-control" name="end_date" required/>
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-primary w-100" type="submit">Générer mon PDF</button>
          </div>
        </form>

        <div class="form-text mt-2">
          Calcule les jours de congé approuvés, les heures estimées (8h/j) et la rémunération associée sur la période.
        </div>
      </div>
    </div>

    <!-- =================== B) Mon salaire & congés d’un mois (PDF) =================== -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title mb-3">PDF : Mon salaire & rémunération des congés (mois donné)</h5>

        <form method="get" action="${pageContext.request.contextPath}/secure/reports" target="_blank" class="row g-3">
          <input type="hidden" name="action" value="pdf_my_salary_month"/>
          <div class="col-md-3">
            <label class="form-label">Mois (yyyy-MM)</label>
            <input type="month" class="form-control" name="month" required/>
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-outline-primary w-100" type="submit">Générer mon PDF</button>
          </div>
        </form>

        <div class="form-text mt-2">
          Affiche le salaire du mois (prorata selon dates d’entrée/sortie), le total de jours de congé approuvés ce mois, et leur rémunération estimée.
        </div>
      </div>
    </div>

  </main>
</div>