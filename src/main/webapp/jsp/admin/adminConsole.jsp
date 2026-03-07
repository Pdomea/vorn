<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Konsole</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="admin-programs-page admin-console-page">
<%
    String consolePlaceholderImage = request.getContextPath() + "/img/hero/landing-poster.svg";
    String programsTileImage = request.getContextPath() + "/img/admin/admin-nb.jpg";
    String trainingsTileImage = request.getContextPath() + "/img/admin/admin-cbum.jpg";
    String exercisesTileImage = request.getContextPath() + "/img/admin/admin-kb.jpg";
%>
<div class="admin-programs-shell page-container">
    <div class="admin-programs-brand-row">
        <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
            <img class="admin-programs-brand-logo"
                 src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
                 alt="vorn">
        </a>
    </div>
    <a class="subtle-back-link" href="<%= request.getContextPath() %>/home">← Zurück</a>

    <div class="page-header">
        <div>
            <h1>Admin-Konsole</h1>
        </div>
        <div class="top-actions">
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
    </div>

    <section class="admin-console-stage">
        <h2 class="admin-console-stage-title">Alle Admin-Bereiche auf einen Blick</h2>

        <div class="card admin-console-card">
            <div class="admin-console-grid">
                <a class="admin-console-tile" href="<%= request.getContextPath() %>/admin/programs">
                    <img class="admin-console-tile-image"
                         src="<%= programsTileImage %>"
                         onerror="this.onerror=null;this.src='<%= consolePlaceholderImage %>';"
                         alt="Programme verwalten">
                    <span class="admin-console-tile-overlay"></span>
                    <span class="admin-console-tile-content">
                        <span class="admin-console-tile-title">Programme verwalten</span>
                        <span class="admin-console-tile-text">Pläne erstellen, Wochen aufbauen und Trainings zuordnen.</span>
                        <span class="admin-console-tile-cta">Öffnen</span>
                    </span>
                </a>

                <a class="admin-console-tile" href="<%= request.getContextPath() %>/admin/trainings">
                    <img class="admin-console-tile-image"
                         src="<%= trainingsTileImage %>"
                         onerror="this.onerror=null;this.src='<%= consolePlaceholderImage %>';"
                         alt="Trainingseinheiten verwalten">
                    <span class="admin-console-tile-overlay"></span>
                    <span class="admin-console-tile-content">
                        <span class="admin-console-tile-title">Trainingseinheiten verwalten</span>
                        <span class="admin-console-tile-text">Trainings bearbeiten und den Status auf Published oder Hidden setzen.</span>
                        <span class="admin-console-tile-cta">Öffnen</span>
                    </span>
                </a>

                <a class="admin-console-tile" href="<%= request.getContextPath() %>/admin/exercises">
                    <img class="admin-console-tile-image"
                         src="<%= exercisesTileImage %>"
                         onerror="this.onerror=null;this.src='<%= consolePlaceholderImage %>';"
                         alt="Übungen verwalten">
                    <span class="admin-console-tile-overlay"></span>
                    <span class="admin-console-tile-content">
                        <span class="admin-console-tile-title">Übungen verwalten</span>
                        <span class="admin-console-tile-text">Übungen anlegen, pflegen und bei Bedarf archivieren.</span>
                        <span class="admin-console-tile-cta">Öffnen</span>
                    </span>
                </a>
            </div>
        </div>
    </section>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
