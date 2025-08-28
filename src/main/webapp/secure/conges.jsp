<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>
<%
    if (request.getSession(false) == null || request.getSession(false).getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <title>Mes congés</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>:root{ --bs-primary:#24af9c; }</style>
</head>
<body class="bg-body-tertiary">

<div class="container py-4">

    <% if (request.getAttribute("success") != null) { %>
    <div class="alert alert-success"><%= request.getAttribute("success") %></div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
    <% } %>

    <h1 class="h3 mb-3">Demander un congé</h1>

    <!-- IMPORTANT: multipart/form-data, no client-side preview -->
    <form method="post"
          action="<%=request.getContextPath()%>/secure/conges/create"
          enctype="multipart/form-data"
          class="row g-3">

        <div class="col-sm-4">
            <label class="form-label">Type</label>
            <select name="typeId" id="typeId" class="form-select" required>
                <%-- 'types' is set by CongePageServlet --%>
                <%
                    java.util.List<com.gestrh.entity.CongeType> types =
                            (java.util.List<com.gestrh.entity.CongeType>) request.getAttribute("types");
                    if (types != null) {
                        for (var t : types) {
                %>
                <option value="<%= t.getId() %>" data-reqdoc="<%= t.isRequiresDoc() %>">
                    <%= t.getLibelle() %>
                </option>
                <% }} %>
            </select>
            <div class="form-text">Certains types exigent un justificatif PDF.</div>
        </div>

        <div class="col-sm-4">
            <label class="form-label">Du</label>
            <input type="date" name="du" class="form-control" required>
        </div>

        <div class="col-sm-4">
            <label class="form-label">Au</label>
            <input type="date" name="au" class="form-control" required>
        </div>

        <div class="col-12">
            <label class="form-label">Motif (optionnel)</label>
            <input type="text" name="motif" class="form-control" placeholder="Motif...">
        </div>

        <!-- File input shown/hidden based on selected type; NO preview -->
        <div class="col-12" id="justifBlock" style="display:none;">
            <label class="form-label">Justificatif (PDF)</label>
            <input type="file" name="justificatif" accept="application/pdf" class="form-control">
            <div class="form-text">PDF uniquement, 5 Mo max.</div>
        </div>

        <div class="col-12 d-flex gap-2">
            <button class="btn btn-primary" type="submit">Envoyer la demande</button>
            <a class="btn btn-outline-secondary"
               href="<%=request.getContextPath()%>/secure/my-leaves.csv">Télécharger mon historique (CSV)</a>
        </div>
    </form>

    <hr class="my-4">

    <h2 class="h5">Mes demandes</h2>
    <div class="table-responsive">
        <table class="table table-sm align-middle">
            <thead>
            <tr>
                <th>#</th><th>Type</th><th>Du</th><th>Au</th><th>Jours</th>
                <th>Statut</th><th>Motif</th><th>Justif</th>
            </tr>
            </thead>
            <tbody>
            <%
                java.util.List<Object[]> conges =
                        (java.util.List<Object[]>) request.getAttribute("conges");
                if (conges != null) {
                    for (Object[] r : conges) {
                        // Expecting: id, typeLibelle, du, au, nbJours, statut, motif, justificatifPath
                        Integer id = (Integer) r[0];
                        String typeLib = (String) r[1];
                        java.time.LocalDate du = (java.time.LocalDate) r[2];
                        java.time.LocalDate au = (java.time.LocalDate) r[3];
                        java.math.BigDecimal nb = (java.math.BigDecimal) r[4];
                        String statut = (String) r[5];
                        String motif = (String) r[6];
                        String jpath = (String) r[7];
            %>
            <tr>
                <td><%= id %></td>
                <td><%= typeLib %></td>
                <td><%= du %></td>
                <td><%= au %></td>
                <td><%= nb %></td>
                <td>
                    <span class="badge text-bg-warning"><%= "en_attente".equals(statut) ? "En attente" : statut %></span>
                </td>
                <td><%= (motif==null?"":motif) %></td>
                <td>
                    <% if (jpath != null) { %>
                    <a class="btn btn-sm btn-outline-secondary"
                       href="<%=request.getContextPath()%>/secure/files/justif?congeId=<%=id%>" target="_blank">
                        Voir
                    </a>
                    <% } else { %> Aucun <% } %>
                </td>
            </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>

<script>
    // Toggle file input ONLY (no preview)
    (function () {
        const sel = document.getElementById('typeId');
        const block = document.getElementById('justifBlock');
        function update() {
            const opt = sel.options[sel.selectedIndex];
            const req = opt && opt.getAttribute('data-reqdoc') === 'true';
            block.style.display = req ? '' : 'none';
            // When hidden, user can still submit without file
        }
        sel.addEventListener('change', update);
        update();
    })();
</script>
</body>
</html>
