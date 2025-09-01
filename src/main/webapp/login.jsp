<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <title>Login – GestRH</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Bootstrap via CDN -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="<%= request.getContextPath() %>/css/styles.css" rel="stylesheet">
  <style>
    :root{ --bs-primary: #24af9c; }
    .btn-primary{ background-color: var(--bs-primary); border-color: var(--bs-primary); }
    .btn-primary:hover{ background-color:#1c998a;border-color:#1c998a;}
    .login-container { min-height:100vh; }
    .logo-side { background-color:#f8f9fa; }
    .logo-side img { max-width:80%; height:auto; }
  </style>
</head>
<body class="bg-light">
<div class="container-fluid login-container d-flex align-items-center">
  <div class="row flex-grow-1 shadow rounded-4 overflow-hidden mx-auto" style="max-width:900px;">

    <!-- Colonne gauche (logo) -->
    <div class="col-md-6 d-flex align-items-center justify-content-center logo-side">
      <img src="<%= request.getContextPath() %>/img/logo.png" alt="GestRH Logo" class="img-fluid">
    </div>

    <!-- Colonne droite (formulaire) -->
    <div class="col-md-6 bg-white d-flex align-items-center">
      <div class="p-4 flex-grow-1">
        <h1 class="h4 text-center mb-3">Connectez-vous</h1>
        <form method="post" action="<%= request.getContextPath() %>/auth/login">
          <div class="mb-3">
            <label class="form-label">Adresse mail</label>
            <input class="form-control" name="email" type="email" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Mot de passe</label>
            <input class="form-control" name="password" type="password" required>
          </div>
          <% if (request.getAttribute("error") != null) { %>
          <div class="alert alert-danger py-2"><%= request.getAttribute("error") %></div>
          <% } %>
          <button class="btn btn-primary w-100">Sign in</button>
        </form>
      </div>
    </div>

  </div>
</div>
</body>
</html>
