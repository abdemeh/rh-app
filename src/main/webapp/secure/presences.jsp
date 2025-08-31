<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/secure/_layout.jspf" %>
<div class="container py-4">
    <main class="container-fluid py-4">
        <h3 class="mb-4">Ma présence</h3>

        <c:set var="ctx" value="${pageContext.request.contextPath}" />
        <c:set var="threshold" value="${onlineThresholdMinutes}" />

        <!-- =================== A) Aujourd’hui : intervalles =================== -->
        <div class="card mb-4">
            <div class="card-body">
                <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                    <div>
                        <h5 class="card-title mb-1">Aujourd’hui — intervalles</h5>
                        <c:choose>
                            <c:when test="${hasOpenSession}">
                                <span class="badge bg-success">Session ouverte</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">Aucune session ouverte</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="d-flex gap-2">
                        <form method="post" action="${ctx}/secure/presences/check">
                            <input type="hidden" name="action" value="in"/>
                            <button class="btn btn-success" type="submit"
                                    <c:if test="${hasOpenSession}">disabled</c:if>
                            >Check-in</button>
                        </form>

                        <form method="post" action="${ctx}/secure/presences/check">
                            <input type="hidden" name="action" value="out"/>
                            <button class="btn btn-danger" type="submit"
                                    <c:if test="${not hasOpenSession}">disabled</c:if>
                            >Check-out</button>
                        </form>
                    </div>
                </div>

                <div class="table-responsive mt-3">
                    <table class="table table-sm align-middle">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Début</th>
                            <th>Fin</th>
                            <th>Durée (min)</th>
                            <th>Commentaire</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty todayIntervals}">
                                <tr><td colspan="5" class="text-muted">Aucun intervalle aujourd’hui</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="i" items="${todayIntervals}" varStatus="s">
                                    <tr>
                                        <td>${s.index + 1}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${i.startTime != null}">
                                                    ${i.startTime.toString().replace('T',' ')}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${i.endTime != null}">
                                                    ${i.endTime.toString().replace('T',' ')}
                                                </c:when>
                                                <c:otherwise><span class="text-success">en cours…</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <%
                                                // Affiche minutes même si la colonne générée est NULL : calcule en Java
                                                com.gestrh.entity.PresenceInterval row =
                                                        (com.gestrh.entity.PresenceInterval) pageContext.getAttribute("i");
                                                Integer mins = row.getMinutes();
                                                if (mins == null && row.getStartTime()!=null && row.getEndTime()!=null) {
                                                    long mm = java.time.Duration.between(row.getStartTime(), row.getEndTime()).toMinutes();
                                                    mins = (int)Math.max(0, mm);
                                                }
                                                if (row.getEndTime() == null) {
                                                    out.print("—");
                                                } else {
                                                    out.print(mins != null ? mins : 0);
                                                }
                                            %>
                                        </td>
                                        <td><c:out value="${i.commentaire}"/></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- =================== B) Qui est en ligne ? =================== -->
        <div class="card mb-4">
            <div class="card-body">
                <h5 class="card-title mb-3">Qui est en ligne ?</h5>
                <div class="table-responsive">
                    <table class="table table-sm align-middle">
                        <thead>
                        <tr>
                            <th>Nom</th>
                            <th>Email</th>
                            <th>Statut</th>
                            <th>Dernière activité</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="u" items="${allUsers}">
                            <c:set var="ls" value="${u.lastLogin}" />
                            <tr>
                                <td><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/></td>
                                <td><c:out value="${u.email}"/></td>
                                <td>
                                    <%
                                        Object lsObj = pageContext.getAttribute("ls");
                                        java.time.LocalDateTime ldt = null;
                                        if (lsObj instanceof java.time.LocalDateTime) ldt = (java.time.LocalDateTime) lsObj;
                                        boolean online = false;
                                        if (ldt != null) {
                                            long diffMin = java.time.Duration.between(ldt, java.time.LocalDateTime.now()).toMinutes();
                                            Object thrObj = pageContext.getAttribute("threshold");
                                            int thr = (thrObj instanceof Number) ? ((Number)thrObj).intValue() : 5;
                                            online = diffMin <= thr;
                                        }
                                        pageContext.setAttribute("onlineFlag", online);
                                    %>
                                    <c:choose>
                                        <c:when test="${onlineFlag}">
                                            <span class="badge bg-success">En ligne</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Hors ligne</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><%= (ldt!=null ? ldt.toString().replace('T',' ') : "-") %></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="form-text">Considéré “en ligne” si actif dans les ${threshold} dernières minutes.</div>
            </div>
        </div>

        <!-- =================== C) Mes 30 derniers jours : totaux par jour =================== -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title mb-3">Mes 30 derniers jours</h5>
                <div class="table-responsive">
                    <table class="table table-striped table-sm">
                        <thead>
                        <tr>
                            <th>Jour</th>
                            <th>Sessions terminées</th>
                            <th>Total</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${days}">
                            <c:set var="mins" value="${totalsByPresenceId[p.id]}"/>
                            <c:set var="cnt"  value="${countsByPresenceId[p.id]}"/>

                            <c:set var="mval" value="${empty mins ? 0 : mins}"/>
                            <c:set var="hval" value="${mval/60.0}" />
                            <c:set var="m" value="${mval mod 60}"/>

                            <tr>
                                <td><c:out value="${p.jour}"/></td>
                                <td><c:out value="${empty cnt ? 0 : cnt}"/></td>
                                <td><fmt:formatNumber value="${hval}" type="number" maxFractionDigits="2" minFractionDigits="2"/> h</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="form-text">
                    Totaux calculés à partir des <code>intervalles clôturés</code>.
                </div>
            </div>
        </div>

    </main>
</div>
<!-- Auto ping last_seen -->
<script>
    setInterval(() => {
        fetch('${ctx}/secure/ping', {method:'POST', headers:{'X-Requested-With':'fetch'}})
            .catch(() => {});
    }, 60000);
</script>
