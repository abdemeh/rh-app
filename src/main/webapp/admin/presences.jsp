<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/admin/_layout.jspf" %>
<div class="container py-4">
    <main class="container-fluid py-4">
        <h3 class="mb-4">Présences — Administration</h3>
        <c:set var="ctx" value="${pageContext.request.contextPath}"/>

        <!-- Qui est en ligne ? -->
        <div class="card">
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
                        <c:set var="threshold" value="5"/>
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
                                            online = diffMin <= 5;
                                        }
                                        pageContext.setAttribute("onlineFlag", online);
                                    %>
                                    <c:choose>
                                        <c:when test="${onlineFlag}"><span class="badge bg-success">En ligne</span></c:when>
                                        <c:otherwise><span class="badge bg-secondary">Hors ligne</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><%= (pageContext.getAttribute("ls")!=null ? ((java.time.LocalDateTime)pageContext.getAttribute("ls")).toString().replace('T',' ') : "-") %></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="form-text">Considéré “en ligne” si actif dans les 5 dernières minutes.</div>
            </div>
        </div>

        <!-- Filtres -->
        <form class="row g-3 align-items-end mt-4" method="get" action="${ctx}/admin/presences">
            <div class="col-md-4">
                <label class="form-label">Utilisateur</label>
                <select class="form-select" name="userId" required>
                    <c:forEach var="u" items="${users}">
                        <option value="${u.id}" <c:if test="${u.id == selectedUserId}">selected</c:if>>
                                ${u.prenom} ${u.nom} — ${u.email}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label">Mois</label>
                <input type="month" class="form-control" name="month" value="${selectedMonth}" required/>
            </div>
            <div class="col-md-5 d-flex gap-2">
                <button class="btn btn-primary" type="submit">Afficher</button>
                <a class="btn btn-outline-secondary"
                   href="${ctx}/admin/presences/pdf?userId=${selectedUserId}&month=${selectedMonth}" target="_blank">
                    Télécharger PDF
                </a>
            </div>
        </form>

        <!-- Récapitulatif du mois en 2 colonnes -->
        <div class="card mb-4">
            <div class="card-body">
                <h5 class="card-title mb-3">Récapitulatif du mois</h5>

                <div class="row">
                    <!-- Colonne gauche -->
                    <div class="col-md-6">
                        <div class="table-responsive">
                            <table class="table table-striped table-sm align-middle">
                                <thead>
                                <tr>
                                    <th>Jour</th>
                                    <th>Sessions</th>
                                    <th>Total (h:mm)</th>
                                    <th>Total (min)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="entry" items="${leftDays}">
                                    <c:set var="day" value="${entry.key}"/>
                                    <c:set var="agg" value="${entry.value}"/>
                                    <c:set var="mins" value="${agg.totalMinutes}"/>
                                    <c:set var="h" value="${mins / 60}"/>
                                    <c:set var="m" value="${mins % 60}"/>
                                    <tr>
                                        <td><c:out value="${day}"/></td>
                                        <td><c:out value="${agg.closedIntervals}"/></td>
                                        <td>
                                            <c:out value="${h}"/>:
                                            <fmt:formatNumber value="${m}" minIntegerDigits="2" />
                                        </td>
                                        <td><c:out value="${mins}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Colonne droite -->
                    <div class="col-md-6">
                        <div class="table-responsive">
                            <table class="table table-striped table-sm align-middle">
                                <thead>
                                <tr>
                                    <th>Jour</th>
                                    <th>Sessions</th>
                                    <th>Total (h:mm)</th>
                                    <th>Total (min)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="entry" items="${rightDays}">
                                    <c:set var="day" value="${entry.key}"/>
                                    <c:set var="agg" value="${entry.value}"/>
                                    <c:set var="mins" value="${agg.totalMinutes}"/>
                                    <c:set var="h" value="${mins / 60}"/>
                                    <c:set var="m" value="${mins % 60}"/>
                                    <tr>
                                        <td><c:out value="${day}"/></td>
                                        <td><c:out value="${agg.closedIntervals}"/></td>
                                        <td>
                                            <c:out value="${h}"/>:
                                            <fmt:formatNumber value="${m}" minIntegerDigits="2" />
                                        </td>
                                        <td><c:out value="${mins}"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Total global du mois -->
                <div class="mt-2">
                    <c:set var="mt" value="${monthTotalMinutes}"/>
                    <c:set var="mh" value="${mt / 60}"/>
                    <c:set var="mm" value="${mt % 60}"/>
                    <div class="alert alert-dark mb-0">
                        <strong>Total du mois :</strong>
                        <c:out value="${mh}"/>:<fmt:formatNumber value="${mm}" minIntegerDigits="2" />
                        ( ${mt} minutes )
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>