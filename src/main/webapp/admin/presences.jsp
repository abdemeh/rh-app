<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/admin/_layout.jspf" %>

<main class="container-fluid py-4">
    <h3 class="mb-4">Présences — Vue administrateur</h3>

    <c:set var="ctx" value="${pageContext.request.contextPath}" />

    <!-- A) Présence du jour -->
    <div class="card mb-4">
        <div class="card-body">
            <h5 class="card-title mb-3">Aujourd’hui</h5>
            <div class="table-responsive">
                <table class="table table-sm align-middle">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Employé</th>
                        <th>Email</th>
                        <th>Entrée</th>
                        <th>Sortie</th>
                        <th>Durée (min)</th>
                        <th>Statut</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${users}" varStatus="s">
                        <c:set var="p" value="${todayByUser[u.id]}"/>
                        <tr>
                            <td>${s.index+1}</td>
                            <td><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/></td>
                            <td><c:out value="${u.email}"/></td>
                            <td><c:out value="${p != null ? p.heureEntree : ''}"/></td>
                            <td><c:out value="${p != null ? p.heureSortie : ''}"/></td>
                            <td><c:out value="${p != null ? p.dureeMinutes : ''}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${p == null}"><span class="badge bg-secondary">—</span></c:when>
                                    <c:when test="${empty p.heureSortie && not empty p.heureEntree}"><span class="badge bg-success">En cours</span></c:when>
                                    <c:otherwise><span class="badge bg-dark">Clôturé</span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- B) Totaux du mois -->
    <div class="card">
        <div class="card-body">
            <h5 class="card-title mb-3">Totaux du mois (${mLabel})</h5>
            <div class="table-responsive">
                <table class="table table-striped table-sm align-middle">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Employé</th>
                        <th>Email</th>
                        <th>Jours pointés</th>
                        <th>Minutes totales</th>
                        <th>Heures (approx.)</th>
                        <th>Bonus (indicatif)</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${users}" varStatus="s">
                        <c:set var="agg" value="${monthAgg[u.id]}"/>
                        <c:set var="bonus" value="${bonusMap[u.id]}"/>
                        <tr>
                            <td>${s.index+1}</td>
                            <td><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/></td>
                            <td><c:out value="${u.email}"/></td>
                            <td><c:out value="${agg != null ? agg.days : 0}"/></td>
                            <td><c:out value="${agg != null ? agg.minutes : 0}"/></td>
                            <td>
                                <c:out value="${agg != null ? (agg.minutes/60) : 0}"/> h
                            </td>
                            <td><c:out value="${bonus}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="form-text">
                Bonus indicatif : atteint si ≥ 20 jours × 8h (9600 minutes). Ajuste la règle dans le servlet si besoin.
            </div>
        </div>
    </div>
</main>
