<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="app.model.Plan" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Programme</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="admin-programs-page">
<jsp:useBean id="adminProgramsBean" class="app.beans.AdminProgramsBean" scope="request" />
<%
    List<Plan> plans = adminProgramsBean.getPlans();
    String statusFilter = adminProgramsBean.getStatusFilter();
    String statusQuerySuffix = adminProgramsBean.getStatusQuerySuffix();
%>
<div class="admin-programs-shell page-container">
<div class="admin-programs-brand-row">
    <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
        <img class="admin-programs-brand-logo"
             src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
             alt="vorn">
    </a>
</div>
<a class="subtle-back-link" href="<%= request.getContextPath() %>/admin/console">← Zurück</a>

<div class="page-header">
        <div>
            <h1>Programme verwalten</h1>
            <p class="muted">Pläne anlegen und für die Detailbearbeitung öffnen.</p>
        </div>
        <div class="top-actions">
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/console">Admin Konsole</a>
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
    </div>

    <% if (adminProgramsBean.getInfo() != null && !adminProgramsBean.getInfo().isBlank()) { %>
    <div class="info">${adminProgramsBean.info}</div>
    <% } %>
    <% if (adminProgramsBean.getError() != null && !adminProgramsBean.getError().isBlank()) { %>
    <div class="error">${adminProgramsBean.error}</div>
    <% } %>

    <div class="card admin-programs-create-card">
        <h2>Neuen Plan anlegen</h2>
        <form class="admin-form" method="post" action="<%= request.getContextPath() %>/admin/program/create">
            <label for="name">Planname</label>
            <input id="name" name="name" type="text" required>

            <label for="description">Beschreibung</label>
            <textarea id="description" name="description" class="textarea-fixed" rows="4"></textarea>

            <label for="heroImagePath">Bildpfad (optional)</label>
            <input id="heroImagePath" name="heroImagePath" type="text" placeholder="/img/plans/hybrid.jpg">

            <div class="form-row-actions">
                <button class="btn btn-primary" type="submit">Plan speichern</button>
            </div>
        </form>
    </div>

    <div class="card admin-programs-list-card">
        <h2>Alle Pläne</h2>
        <form class="admin-filter-bar" method="get" action="<%= request.getContextPath() %>/admin/programs">
            <div class="filter-group">
                <label for="planStatus">Status</label>
                <select id="planStatus" name="status">
                    <option value="ALL" <%= "ALL".equals(statusFilter) ? "selected" : "" %>>Alle Status</option>
                    <option value="ACTIVE" <%= "ACTIVE".equals(statusFilter) ? "selected" : "" %>>ACTIVE</option>
                    <option value="ARCHIVED" <%= "ARCHIVED".equals(statusFilter) ? "selected" : "" %>>ARCHIVED</option>
                </select>
            </div>
            <div class="filter-actions">
                <button class="btn btn-primary" type="submit">Filtern</button>
                <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/programs">Reset</a>
            </div>
        </form>
        <% if (!adminProgramsBean.hasPlans()) { %>
        <p>Noch keine Pläne vorhanden.</p>
        <% } else { %>
        <table class="admin-programs-table">
            <thead>
            <tr>
                <th>Name</th>
                <th>Status</th>
                <th>Beschreibung</th>
                <th>Bildpfad</th>
                <th>Aktion</th>
            </tr>
            </thead>
            <tbody>
            <% for (Plan plan : plans) {
                String planStatusClass = "status-open";
                if ("ACTIVE".equals(plan.getStatus())) {
                    planStatusClass = "status-done";
                } else if ("ARCHIVED".equals(plan.getStatus())) {
                    planStatusClass = "status-in-progress";
                }
            %>
            <tr>
                <td><%= plan.getName() %></td>
                <td><span class="status-badge <%= planStatusClass %>"><%= plan.getStatus() %></span></td>
                <td><%= plan.getDescription() == null ? "" : plan.getDescription() %></td>
                <td><%= plan.getHeroImagePath() == null ? "-" : plan.getHeroImagePath() %></td>
                <td>
                    <div class="admin-programs-actions">
                        <a class="btn btn-secondary admin-program-open-btn" href="<%= request.getContextPath() %>/admin/programs/detail?planId=<%= plan.getId() %><%= statusQuerySuffix %>">
                            Plan öffnen
                        </a>
                        <% if ("ARCHIVED".equals(plan.getStatus())) { %>
                        <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/activate">
                            <input type="hidden" name="planId" value="<%= plan.getId() %>">
                            <button class="btn btn-ghost admin-program-activate-btn" type="submit">Aktivieren</button>
                        </form>
                        <% } %>
                        <form class="inline-form"
                              method="post"
                              action="<%= request.getContextPath() %>/admin/program/delete"
                              onsubmit="return confirm('Plan wirklich dauerhaft löschen? Dieser Schritt kann nicht rückgängig gemacht werden.');">
                            <input type="hidden" name="planId" value="<%= plan.getId() %>">
                            <button class="btn btn-danger admin-program-delete-btn" type="submit">Löschen</button>
                        </form>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
