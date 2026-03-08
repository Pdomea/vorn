<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="app.model.Training" %>
            <!DOCTYPE html>
            <html lang="de">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Admin Trainings</title>
                <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
                <link rel="icon" type="image/svg+xml"
                    href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
            </head>

            <body class="admin-trainings-page">
                <jsp:useBean id="adminTrainingsBean" class="app.beans.AdminTrainingsBean" scope="request" />
                <div class="admin-trainings-shell page-container">
                    <div class="admin-trainings-brand-row">
                        <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
                            <img class="admin-trainings-brand-logo"
                                src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg" alt="vorn">
                        </a>
                    </div>
                    <a class="subtle-back-link" href="<%= request.getContextPath() %>/admin/console">← Zurück</a>
                    <div class="page-header">
                        <div>
                            <h1>Trainings verwalten</h1>
                            <p class="muted">Trainings anlegen, bearbeiten und Sichtbarkeit steuern.</p>
                        </div>
                        <div class="top-actions">
                            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/console">Admin
                                Konsole</a>
                            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
                        </div>
                    </div>

                    <% List<Training> trainings = adminTrainingsBean.getTrainings();
                        String sortBy = adminTrainingsBean.getSortBy();
                        String sortDir = adminTrainingsBean.getSortDir();
                        String statusFilter = adminTrainingsBean.getStatusFilter();
                        String sortQuerySuffix = adminTrainingsBean.getSortQuerySuffix();
                        String statusQuerySuffix = adminTrainingsBean.getStatusQuerySuffix();
                        %>

                        <% if (adminTrainingsBean.getInfo() !=null && !adminTrainingsBean.getInfo().isBlank()) { %>
                            <div class="info">${adminTrainingsBean.info}</div>
                            <% } %>
                                <% if (adminTrainingsBean.getError() !=null && !adminTrainingsBean.getError().isBlank())
                                    { %>
                                    <div class="error">${adminTrainingsBean.error}</div>
                                    <% } %>

                                        <div class="card admin-trainings-form-card">
                                            <h2>
                                                <%= adminTrainingsBean.getFormHeadline() %>
                                            </h2>
                                            <form class="admin-form" method="post"
                                                action="<%= request.getContextPath() %>/admin/training/save">
                                                <input type="hidden" name="id"
                                                    value="<%= adminTrainingsBean.getFormId() %>">

                                                <label for="title">Titel</label>
                                                <input id="title" name="title" type="text"
                                                    value="<%= adminTrainingsBean.getFormTitle() %>" required>

                                                <label for="description">Beschreibung</label>
                                                <textarea id="description" name="description" class="textarea-fixed"
                                                    rows="4"><%= adminTrainingsBean.getFormDescription() %></textarea>

                                                <div class="form-row-actions">
                                                    <button class="btn btn-primary" type="submit">
                                                        <%= adminTrainingsBean.getSubmitLabel() %>
                                                    </button>
                                                    <a class="btn btn-ghost"
                                                        href="<%= request.getContextPath() %>/admin/trainings">Formular
                                                        leeren</a>
                                                </div>
                                            </form>
                                        </div>

                                        <div class="card admin-trainings-table-card">
                                            <h2>Alle Trainings</h2>
                                            <form class="admin-filter-bar" method="get"
                                                action="<%= request.getContextPath() %>/admin/trainings">
                                                <input type="hidden" name="sortBy" value="<%= sortBy %>">
                                                <input type="hidden" name="sortDir" value="<%= sortDir %>">
                                                <div class="filter-group">
                                                    <label for="trainingStatus">Status</label>
                                                    <select id="trainingStatus" name="status">
                                                        <option value="ALL" <%="ALL" .equals(statusFilter) ? "selected"
                                                            : "" %>>Alle Status</option>
                                                        <option value="DRAFT" <%="DRAFT" .equals(statusFilter)
                                                            ? "selected" : "" %>>DRAFT</option>
                                                        <option value="PUBLISHED" <%="PUBLISHED" .equals(statusFilter)
                                                            ? "selected" : "" %>>PUBLISHED</option>
                                                        <option value="HIDDEN" <%="HIDDEN" .equals(statusFilter)
                                                            ? "selected" : "" %>>HIDDEN</option>
                                                    </select>
                                                </div>
                                                <div class="filter-actions">
                                                    <button class="btn btn-primary" type="submit">Filtern</button>
                                                    <a class="btn btn-ghost"
                                                        href="<%= request.getContextPath() %>/admin/trainings?sortBy=<%= sortBy %>&sortDir=<%= sortDir %>">Reset</a>
                                                </div>
                                            </form>
                                            <div class="table-sort-row">
                                                <a class="btn btn-ghost <%= " id".equals(sortBy) && "asc"
                                                    .equals(sortDir) ? "is-active" : "" %>"
                                                    href="<%= request.getContextPath() %>
                                                        /admin/trainings?sortBy=id&sortDir=asc<%= statusQuerySuffix %>
                                                            ">ID aufsteigend</a>
                                                <a class="btn btn-ghost <%= " id".equals(sortBy) && "desc"
                                                    .equals(sortDir) ? "is-active" : "" %>"
                                                    href="<%= request.getContextPath() %>
                                                        /admin/trainings?sortBy=id&sortDir=desc<%= statusQuerySuffix %>
                                                            ">ID absteigend</a>
                                                <a class="btn btn-ghost <%= " title".equals(sortBy) && "asc"
                                                    .equals(sortDir) ? "is-active" : "" %>"
                                                    href="<%= request.getContextPath() %>
                                                        /admin/trainings?sortBy=title&sortDir=asc<%= statusQuerySuffix
                                                            %>">Titel A-Z</a>
                                                <a class="btn btn-ghost <%= " title".equals(sortBy) && "desc"
                                                    .equals(sortDir) ? "is-active" : "" %>"
                                                    href="<%= request.getContextPath() %>
                                                        /admin/trainings?sortBy=title&sortDir=desc<%= statusQuerySuffix
                                                            %>">Titel Z-A</a>
                                            </div>
                                            <% if (!adminTrainingsBean.hasTrainings()) { %>
                                                <p>Noch keine Trainings vorhanden.</p>
                                                <% } else { %>
                                                    <table class="admin-trainings-table">
                                                        <thead>
                                                            <tr>
                                                                <th>ID</th>
                                                                <th>Titel</th>
                                                                <th>Status</th>
                                                                <th>Beschreibung</th>
                                                                <th>Aktionen</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <% for (Training training : trainings) { %>
                                                                <tr>
                                                                    <td>
                                                                        <%= training.getId() %>
                                                                    </td>
                                                                    <td>
                                                                        <%= training.getTitle() %>
                                                                    </td>
                                                                    <td>
                                                                        <% String statusClass="status-open" ; if
                                                                            ("PUBLISHED".equals(training.getStatus())) {
                                                                            statusClass="status-done" ; } else if
                                                                            ("HIDDEN".equals(training.getStatus())) {
                                                                            statusClass="status-in-progress" ; } %>
                                                                            <span
                                                                                class="status-badge <%= statusClass %>">
                                                                                <%= training.getStatus() %>
                                                                            </span>
                                                                    </td>
                                                                    <td>
                                                                        <%= training.getDescription()==null ? "" :
                                                                            training.getDescription() %>
                                                                    </td>
                                                                    <td class="table-actions">
                                                                        <a class="btn btn-ghost"
                                                                            href="<%= request.getContextPath() %>/admin/trainings?editId=<%= training.getId() %><%= sortQuerySuffix %><%= statusQuerySuffix %>">Bearbeiten</a>
                                                                        <a class="btn btn-secondary"
                                                                            href="<%= request.getContextPath() %>/admin/training/exercises?trainingId=<%= training.getId() %>">Übungen
                                                                            zuordnen</a>

                                                                        <% if
                                                                            (!"PUBLISHED".equals(training.getStatus()))
                                                                            { %>
                                                                            <form class="inline-form" method="post"
                                                                                action="<%= request.getContextPath() %>/admin/training/publish">
                                                                                <input type="hidden" name="id"
                                                                                    value="<%= training.getId() %>">
                                                                                <button class="btn btn-primary"
                                                                                    type="submit">Aktivieren</button>
                                                                            </form>
                                                                            <% } %>

                                                                                <% if
                                                                                    (!"HIDDEN".equals(training.getStatus()))
                                                                                    { %>
                                                                                    <form class="inline-form"
                                                                                        method="post"
                                                                                        action="<%= request.getContextPath() %>/admin/training/hide">
                                                                                        <input type="hidden" name="id"
                                                                                            value="<%= training.getId() %>">
                                                                                        <button
                                                                                            class="btn btn-secondary"
                                                                                            type="submit">Archivieren</button>
                                                                                    </form>
                                                                                    <% } %>

                                                                                        <form class="inline-form"
                                                                                            method="post"
                                                                                            action="<%= request.getContextPath() %>/admin/training/delete"
                                                                                            onsubmit="return confirm('Training wirklich dauerhaft löschen? Dieser Schritt kann nicht rückgängig gemacht werden.');">
                                                                                            <input type="hidden"
                                                                                                name="id"
                                                                                                value="<%= training.getId() %>">
                                                                                            <button
                                                                                                class="btn btn-danger"
                                                                                                type="submit">Löschen</button>
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