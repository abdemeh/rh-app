<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/secure/_layout.jspf" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="container py-4">
  <main class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="m-0">
        Calendrier partagé — ${yearMonth.month} ${yearMonth.year}
      </h3>
      <div class="btn-group">
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/calendar?month=${prevMonth}">« ${prevMonth}</a>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/calendar?month=${nextMonth}">${nextMonth} »</a>
      </div>
    </div>

    <c:if test="${param.success == 'created'}">
      <div class="alert alert-success">Événement ajouté.</div>
    </c:if>

    <div class="row g-4">
      <!-- Calendar grid -->
      <div class="col-lg-9">
        <div class="table-responsive">
          <table class="table table-bordered align-middle mb-0">
            <thead class="table-light">
            <tr>
              <th>Lun</th><th>Mar</th><th>Mer</th><th>Jeu</th><th>Ven</th><th>Sam</th><th>Dim</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="week" items="${weeksIso}">
              <tr>
                <c:forEach var="dIso" items="${week}">
                  <!-- inMonth = same yyyy-MM as monthStr -->
                  <c:set var="inMonth" value="${fn:substring(dIso,0,7) == monthStr}" />
                  <c:set var="isToday" value="${dIso == todayIso}" />
                  <td class="${inMonth ? '' : 'text-muted'}" style="vertical-align: top; min-width:130px; cursor:pointer"
                      onclick="selectDay('${dIso}')">
                    <div class="d-flex justify-content-between">
                      <span class="${isToday ? 'badge bg-primary' : ''}">${fn:substring(dIso,8,10)}</span>
                      <span class="small">${inMonth ? '' : fn:substring(dIso,5,7)}</span>
                    </div>

                    <ul class="list-unstyled mb-0 mt-2" style="max-height:110px; overflow:auto;">
                      <c:forEach var="e" items="${eventsByDateStr[dIso]}">
                        <li class="small">
                          <strong><c:out value="${e.titre}"/></strong>
                          <span class="text-muted">—
                            <c:choose>
                              <c:when test="${e.creePar != null}">
                                <c:out value="${e.creePar.prenom}"/> <c:out value="${e.creePar.nom}"/>
                              </c:when>
                              <c:otherwise>Anonyme</c:otherwise>
                            </c:choose>
                          </span>
                        </li>
                      </c:forEach>
                    </ul>
                  </td>
                </c:forEach>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>

        <!-- Day details panel -->
        <div class="card mt-3">
          <div class="card-body">
            <h5 id="dayTitle" class="card-title">Sélectionnez un jour…</h5>
            <div id="dayEvents"></div>
          </div>
        </div>
      </div>

      <!-- Sidebar: Add + Upcoming -->
      <div class="col-lg-3">
        <div class="card mb-3">
          <div class="card-body">
            <h5 class="card-title">Ajouter une note/événement</h5>
            <form method="post" action="${pageContext.request.contextPath}/calendar/save">
              <input type="hidden" name="month" value="${monthStr}">
              <div class="mb-2">
                <label class="form-label">Date</label>
                <input type="date" name="date_evenement" id="date_evenement" class="form-control"
                       value="${todayIso}" required>
              </div>
              <div class="mb-2">
                <label class="form-label">Titre</label>
                <input type="text" name="titre" class="form-control" required>
              </div>
              <div class="mb-2">
                <label class="form-label">Description</label>
                <textarea name="description" class="form-control" rows="3"></textarea>
              </div>
              <button class="btn btn-primary w-100" type="submit">Ajouter</button>
            </form>
          </div>
        </div>

        <div class="card">
          <div class="card-body">
            <h5 class="card-title">À venir (14 jours)</h5>
            <c:choose>
              <c:when test="${empty upcomingVM}">
                <div class="text-muted">Rien à venir.</div>
              </c:when>
              <c:otherwise>
                <ul class="list-unstyled mb-0">
                  <c:forEach var="u" items="${upcomingVM}">
                    <li class="mb-2">
                      <span class="badge bg-secondary"><c:out value="${u.dateLabel}"/></span>
                      <div><strong><c:out value="${u.title}"/></strong></div>
                      <div class="text-muted small"><c:out value="${u.creator}"/></div>
                    </li>
                  </c:forEach>
                </ul>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </div>
  </main>
  <div class="container py-4">
<!-- Hidden templates for per-day details -->
<div id="hidden-day-templates" class="d-none">
  <c:forEach var="week" items="${weeksIso}">
    <c:forEach var="dIso" items="${week}">
      <div id="tpl-${dIso}">
        <c:choose>
          <c:when test="${empty eventsByDateStr[dIso]}">
            <div class="text-muted">Aucun événement ce jour.</div>
          </c:when>
          <c:otherwise>
            <c:forEach var="e" items="${eventsByDateStr[dIso]}">
              <div class="mb-2">
                <strong><c:out value="${e.titre}"/></strong>
                <div class="text-muted small">
                  par
                  <c:choose>
                    <c:when test="${e.creePar != null}">
                      <c:out value="${e.creePar.prenom}"/> <c:out value="${e.creePar.nom}"/>
                    </c:when>
                    <c:otherwise>Anonyme</c:otherwise>
                  </c:choose>
                </div>
                <div><c:out value="${e.description}"/></div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </c:forEach>
  </c:forEach>
</div>

<script>
  function selectDay(isoDate){
    const title = document.getElementById('dayTitle');
    const listDiv = document.getElementById('dayEvents');
    title.textContent = "Événements du " + isoDate;

    const tpl = document.getElementById('tpl-' + isoDate);
    listDiv.innerHTML = tpl ? tpl.innerHTML : '<div class="text-muted">Aucun événement ce jour.</div>';

    const dateInput = document.getElementById('date_evenement');
    if (dateInput) dateInput.value = isoDate;
  }
</script>
