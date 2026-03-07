<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="auth-login-page">
<jsp:useBean id="loginBean" class="app.beans.LoginBean" scope="request" />
<div class="auth-login-shell">
<header class="auth-login-header">
    <img class="auth-login-logo"
         src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
         alt="vorn">
</header>
<main class="auth-login-main">
<div class="form-card auth-login-panel">
    <h1 class="auth-card-title">Login</h1>
    <% if (loginBean.getError() != null && !loginBean.getError().isBlank()) { %>
    <div class="error">${loginBean.error}</div>
    <% } %>
    <% if (loginBean.getInfo() != null && !loginBean.getInfo().isBlank()) { %>
    <div class="info">${loginBean.info}</div>
    <% } %>

    <form class="auth-login-form" method="post" action="<%= request.getContextPath() %>/login">
        <% if (loginBean.getRedirect() != null && !loginBean.getRedirect().isBlank()) { %>
        <input type="hidden" name="redirect" value="${loginBean.redirect}">
        <% } %>
        <label for="email">E-Mail</label>
        <input id="email" type="email" name="email" value="${loginBean.email}" required>

        <label for="password">Passwort</label>
        <input id="password" type="password" name="password" required>

        <div class="form-actions">
            <button class="btn btn-primary" type="submit">Einloggen</button>
            <a href="<%= request.getContextPath() %>/register">Zur Registrierung</a>
        </div>
    </form>
</div>
</main>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
