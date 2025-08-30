<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="fr"><head>
<meta charset="utf-8"><title>Présences</title>
<%@ include file="/secure/_layout.jspf" %>
</head><body class="bg-body-tertiary">
<div class="container py-4">
    <div class="card shadow-sm">
        <div class="card-body">
            <h5 class="card-title">Présences aujourd'hui</h5>
            <div class="table-responsive">
                <table class="table table-sm">
                    <thead><tr><th>Employé</th><th>Entrée</th><th>Sortie</th><th>Statut</th></tr></thead>
                    <tbody>
                    <c:forEach var="r" items="${rows}">
                        <tr>
                            <td>${r[0]} ${r[1]}</td>
                            <td><c:out value="${r[3]}"/></td>
                            <td><c:out value="${r[4]}"/></td>
                            <td><c:out value="${r[5]}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr><td colspan="4" class="text-muted">Aucune donnée.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body></html>
