<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="app.model.Exercise" %>
<%@ page import="app.model.MuscleGroup" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Übungen</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="admin-exercises-page">
<jsp:useBean id="adminExercisesBean" class="app.beans.AdminExercisesBean" scope="request" />
<div class="admin-exercises-shell page-container">
<div class="admin-exercises-brand-row">
    <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
        <img class="admin-exercises-brand-logo"
             src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
             alt="vorn">
    </a>
</div>
<a class="subtle-back-link" href="<%= request.getContextPath() %>/admin/console">← Zurück</a>
<div class="page-header">
    <div>
        <h1>Übungen verwalten</h1>
        <p class="muted">Übungen pflegen und bei Bedarf archivieren.</p>
    </div>
    <div class="top-actions">
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/console">Admin Konsole</a>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
    </div>
</div>

<%
    List<Exercise> exercises = adminExercisesBean.getExercises();
    List<MuscleGroup> muscleGroups = adminExercisesBean.getMuscleGroups();
    String sortBy = adminExercisesBean.getSortBy();
    String sortDir = adminExercisesBean.getSortDir();
    String statusFilter = adminExercisesBean.getStatusFilter();
    String statusQuerySuffix = adminExercisesBean.getStatusQuerySuffix();
%>

<% if (adminExercisesBean.getInfo() != null && !adminExercisesBean.getInfo().isBlank()) { %>
<div class="info">${adminExercisesBean.info}</div>
<% } %>
<% if (adminExercisesBean.getError() != null && !adminExercisesBean.getError().isBlank()) { %>
<div class="error">${adminExercisesBean.error}</div>
<% } %>

<div class="card admin-exercises-form-card">
    <h2><%= adminExercisesBean.getFormHeadline() %></h2>
    <form class="admin-form" method="post" action="<%= request.getContextPath() %>/admin/exercise/save">
        <input type="hidden" name="id" value="<%= adminExercisesBean.getFormId() %>">

        <label for="name">Name</label>
        <input id="name" name="name" type="text" value="<%= adminExercisesBean.getFormName() %>" required>

        <label for="description">Beschreibung</label>
        <textarea id="description"
                  name="description"
                  class="textarea-fixed"
                  rows="4"><%= adminExercisesBean.getFormDescription() %></textarea>

        <label>Muskelgruppen</label>
        <div class="muscle-group-grid">
            <% if (!adminExercisesBean.hasMuscleGroups()) { %>
            <p class="muted">Keine Muskelgruppen vorhanden. Bitte zuerst Migration 010 ausführen.</p>
            <% } else { %>
            <% for (MuscleGroup muscleGroup : muscleGroups) {
                   boolean checked = adminExercisesBean.isSelectedMuscleGroup(muscleGroup.getId());
            %>
            <label class="muscle-group-chip">
                <input type="checkbox"
                       name="muscleGroupIds"
                       value="<%= muscleGroup.getId() %>"
                       <%= checked ? "checked" : "" %>>
                <span><%= muscleGroup.getLabel() %></span>
            </label>
            <% } %>
            <% } %>
        </div>

        <div class="form-row-actions">
            <button class="btn btn-primary" type="submit"><%= adminExercisesBean.getSubmitLabel() %></button>
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/exercises">Formular leeren</a>
        </div>
    </form>
</div>

<div class="card admin-exercises-table-card">
    <h2>Alle Übungen</h2>
    <form class="admin-filter-bar" method="get" action="<%= request.getContextPath() %>/admin/exercises">
        <input type="hidden" name="sortBy" value="<%= sortBy %>">
        <input type="hidden" name="sortDir" value="<%= sortDir %>">
        <div class="filter-group">
            <label for="exerciseStatus">Status</label>
            <select id="exerciseStatus" name="status">
                <option value="ALL" <%= "ALL".equals(statusFilter) ? "selected" : "" %>>Alle Status</option>
                <option value="ACTIVE" <%= "ACTIVE".equals(statusFilter) ? "selected" : "" %>>ACTIVE</option>
                <option value="ARCHIVED" <%= "ARCHIVED".equals(statusFilter) ? "selected" : "" %>>ARCHIVED</option>
            </select>
        </div>
        <div class="filter-actions">
            <button class="btn btn-primary" type="submit">Filtern</button>
            <a class="btn btn-ghost"
               href="<%= request.getContextPath() %>/admin/exercises?sortBy=<%= sortBy %>&sortDir=<%= sortDir %>">Reset</a>
        </div>
    </form>
    <div class="table-sort-row">
        <a class="btn btn-ghost <%= "name".equals(sortBy) && "asc".equals(sortDir) ? "is-active" : "" %>"
           href="<%= request.getContextPath() %>/admin/exercises?sortBy=name&sortDir=asc<%= statusQuerySuffix %>">Name A-Z</a>
        <a class="btn btn-ghost <%= "name".equals(sortBy) && "desc".equals(sortDir) ? "is-active" : "" %>"
           href="<%= request.getContextPath() %>/admin/exercises?sortBy=name&sortDir=desc<%= statusQuerySuffix %>">Name Z-A</a>
        <a class="btn btn-ghost <%= "muscle".equals(sortBy) && "asc".equals(sortDir) ? "is-active" : "" %>"
           href="<%= request.getContextPath() %>/admin/exercises?sortBy=muscle&sortDir=asc<%= statusQuerySuffix %>">Muskelgruppe A-Z</a>
        <a class="btn btn-ghost <%= "muscle".equals(sortBy) && "desc".equals(sortDir) ? "is-active" : "" %>"
           href="<%= request.getContextPath() %>/admin/exercises?sortBy=muscle&sortDir=desc<%= statusQuerySuffix %>">Muskelgruppe Z-A</a>
    </div>
    <% if (!adminExercisesBean.hasExercises()) { %>
    <p>Noch keine Übungen vorhanden.</p>
    <% } else { %>
    <table class="admin-exercises-table">
        <thead>
        <tr>
            <th>Name</th>
            <th>Muskelgruppen</th>
            <th>Status</th>
            <th>Beschreibung</th>
            <th>Aktionen</th>
        </tr>
        </thead>
        <tbody>
        <% for (Exercise exercise : exercises) { %>
        <tr>
            <td><%= exercise.getName() %></td>
            <td>
                <%
                    List<String> labels = exercise.getMuscleGroupLabels();
                    if (labels == null || labels.isEmpty()) {
                %>
                <span class="muted">-</span>
                <% } else { %>
                <%= String.join(", ", labels) %>
                <% } %>
            </td>
            <td>
                <%
                    String statusClass = "status-open";
                    if ("ACTIVE".equals(exercise.getStatus())) {
                        statusClass = "status-done";
                    } else if ("ARCHIVED".equals(exercise.getStatus())) {
                        statusClass = "status-in-progress";
                    }
                %>
                <span class="status-badge <%= statusClass %>"><%= exercise.getStatus() %></span>
            </td>
            <td><%= exercise.getDescription() == null ? "" : exercise.getDescription() %></td>
            <td class="table-actions">
                <a class="btn btn-ghost"
                   href="<%= request.getContextPath() %>/admin/exercises?editId=<%= exercise.getId() %>&sortBy=<%= sortBy %>&sortDir=<%= sortDir %><%= statusQuerySuffix %>">Bearbeiten</a>
                <% if (!"ARCHIVED".equals(exercise.getStatus())) { %>
                <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/exercise/archive">
                    <input type="hidden" name="id" value="<%= exercise.getId() %>">
                    <button class="btn btn-secondary" type="submit">Archive</button>
                </form>
                <% } %>
                <form class="inline-form"
                      method="post"
                      action="<%= request.getContextPath() %>/admin/exercise/delete"
                      onsubmit="return confirm('Übung wirklich dauerhaft löschen? Dieser Schritt kann nicht rückgängig gemacht werden.');">
                    <input type="hidden" name="id" value="<%= exercise.getId() %>">
                    <button class="btn btn-danger" type="submit">Löschen</button>
                </form>
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
