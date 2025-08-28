<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Login – GestRH</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Bootstrap via CDN -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    :root{ --bs-primary: #24af9c; }
    .btn-primary{ background-color: var(--bs-primary); border-color: var(--bs-primary); }
    .btn-primary:hover{ background-color:#1c998a;border-color:#1c998a;}
  </style>
</head>
<body class="bg-light">
<div class="container d-flex align-items-center justify-content-center" style="min-height:100vh;">
  <div class="card shadow rounded-4" style="max-width:420px; width:100%;">
    <div class="card-body p-4">
      <h1 class="h4 text-center mb-3">GestRH</h1>
      <form method="post" action="<%= request.getContextPath() %>/auth/login">
        <div class="mb-3">
          <label class="form-label">Email</label>
          <input class="form-control" name="email" type="email" required>
        </div>
        <div class="mb-3">
          <label class="form-label">Password</label>
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
</body>
</html>
