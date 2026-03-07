<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrierung</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="auth-login-page">
<jsp:useBean id="registerBean" class="app.beans.RegisterBean" scope="request" />
<div class="auth-login-shell">
<header class="auth-login-header">
    <img class="auth-login-logo"
         src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
         alt="vorn">
</header>
<a class="subtle-back-link" href="<%= request.getContextPath() %>/">← Zurück</a>
<main class="auth-login-main">
<div class="form-card auth-login-panel">
    <h1 class="auth-card-title">Registrierung</h1>
    <p class="auth-subtitle">Erstelle einen Account, um deinen Wochenplan zu starten.</p>
    <% if (registerBean.getInfo() != null && !registerBean.getInfo().isBlank()) { %>
    <div class="info">${registerBean.info}</div>
    <% } %>
    <% if (registerBean.getError() != null && !registerBean.getError().isBlank()) { %>
    <div class="error">${registerBean.error}</div>
    <% } %>

    <form class="auth-login-form" method="post" action="<%= request.getContextPath() %>/register">
        <label for="displayName">Anzeigename</label>
        <input id="displayName" type="text" name="displayName" value="${registerBean.displayName}" required>

        <label for="email">E-Mail</label>
        <input id="email" type="email" name="email" value="${registerBean.email}" required>

        <label for="password">Passwort</label>
        <input id="password" type="password" name="password" minlength="8" required>

        <label for="passwordConfirm">Passwort wiederholen</label>
        <input id="passwordConfirm" type="password" name="passwordConfirm" minlength="8" required>

        <div class="form-actions">
            <button class="btn btn-primary" type="submit">Registrieren</button>
            <a href="<%= request.getContextPath() %>/login">Zum Login</a>
        </div>
    </form>
</div>
</main>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
