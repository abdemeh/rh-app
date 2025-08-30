<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>

<%@ include file="/secure/_layout.jspf" %>
<div class="container py-4">
    <main class="container-fluid py-4">
        <h3 class="mb-4">Mon profil</h3>

        <c:if test="${param.ok == '1'}">
            <div class="alert alert-success">Vos informations ont été mises à jour.</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/account/profile/save" class="row g-3">
            <div class="col-md-6">
                <label class="form-label">Prénom</label>
                <input type="text" name="prenom" class="form-control" value="${u.prenom}" required>
            </div>
            <div class="col-md-6">
                <label class="form-label">Nom</label>
                <input type="text" name="nom" class="form-control" value="${u.nom}" required>
            </div>

            <div class="col-md-6">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" value="${u.email}" required>
            </div>
            <div class="col-md-6">
                <label class="form-label">Nouveau mot de passe</label>
                <input type="password" name="mot_de_passe" class="form-control" placeholder="Laisser vide pour conserver">
            </div>

            <div class="col-md-6">
                <label class="form-label">Adresse</label>
                <input type="text" name="adresse" class="form-control" value="${u.adresse}">
            </div>
            <div class="col-md-6">
                <label class="form-label">Téléphone</label>
                <input type="text" name="telephone" class="form-control" value="${u.telephone}">
            </div>

            <div class="col-12 d-flex gap-2 mt-2">
                <button class="btn btn-primary" type="submit">Enregistrer</button>
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">Retour</a>
            </div>
        </form>
    </main>
</div>
