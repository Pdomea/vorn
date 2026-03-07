<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="app.model.Plan" %>
<%@ page import="app.model.PlanWeek" %>
<%@ page import="app.model.PlanWeekTraining" %>
<%@ page import="app.model.Training" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Programm-Detail</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="admin-programs-page">
<jsp:useBean id="adminProgramDetailBean" class="app.beans.AdminProgramDetailBean" scope="request" />
<%
    List<Training> trainings = adminProgramDetailBean.getTrainings();
    Plan selectedPlan = adminProgramDetailBean.getSelectedPlan();
    List<PlanWeek> selectedWeeks = adminProgramDetailBean.getSelectedWeeks();
    Map<Long, List<PlanWeekTraining>> weekTrainings = adminProgramDetailBean.getWeekTrainings();
    boolean selectedPlanActive = adminProgramDetailBean.isSelectedPlanActive();
%>
<div class="admin-programs-shell page-container">
    <div class="admin-programs-brand-row">
        <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
            <img class="admin-programs-brand-logo"
                 src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
                 alt="vorn">
        </a>
    </div>
    <a class="subtle-back-link" href="<%= request.getContextPath() %>/admin/programs">← Zurück</a>
    <div class="page-header">
        <div>
            <h1>Programm-Detail</h1>
            <p class="muted">Plan bearbeiten, Wochen verwalten und Trainings zuordnen.</p>
        </div>
        <div class="top-actions">
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/admin/console">Admin Konsole</a>
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
    </div>

    <% if (adminProgramDetailBean.getInfo() != null && !adminProgramDetailBean.getInfo().isBlank()) { %>
    <div class="info">${adminProgramDetailBean.info}</div>
    <% } %>
    <% if (adminProgramDetailBean.getError() != null && !adminProgramDetailBean.getError().isBlank()) { %>
    <div class="error">${adminProgramDetailBean.error}</div>
    <% } %>

    <% if (selectedPlan == null) { %>
    <div class="card admin-programs-detail-card">
        <h2>Kein Plan ausgewählt</h2>
        <p>Bitte öffne zuerst einen Plan aus der Übersicht.</p>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/admin/programs">Zur Plan-Übersicht</a>
    </div>
    <% } else { %>
    <div class="card admin-programs-detail-card">
        <h2>Plan-Details: <%= selectedPlan.getName() %></h2>
        <p>
            <strong>Status:</strong>
            <span class="status-badge <%= selectedPlanActive ? "status-done" : "status-in-progress" %>">
                <%= selectedPlan.getStatus() %>
            </span>
        </p>
        <form class="admin-form" method="post" action="<%= request.getContextPath() %>/admin/program/save">
            <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
            <label for="selectedPlanName">Planname</label>
            <input id="selectedPlanName"
                   name="name"
                   type="text"
                   value="<%= selectedPlan.getName() == null ? "" : selectedPlan.getName() %>"
                   required>
            <label for="selectedPlanDescription">Beschreibung</label>
            <textarea id="selectedPlanDescription"
                      name="description"
                      class="textarea-fixed"
                      rows="4"><%= selectedPlan.getDescription() == null ? "" : selectedPlan.getDescription() %></textarea>
            <label for="selectedHeroImagePath">Bildpfad</label>
            <input id="selectedHeroImagePath"
                   name="heroImagePath"
                   type="text"
                   value="<%= selectedPlan.getHeroImagePath() == null ? "" : selectedPlan.getHeroImagePath() %>"
                   placeholder="/img/plans/hybrid.jpg">
            <div class="form-row-actions">
                <button class="btn btn-secondary" type="submit">Änderung speichern</button>
            </div>
        </form>
        <div class="admin-program-detail-actions">
            <div class="actions admin-program-safe-actions">
                <% if (selectedPlanActive) { %>
                <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/archive">
                    <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                    <button class="btn btn-secondary" type="submit">Plan archivieren</button>
                </form>
                <% } else { %>
                <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/activate">
                    <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                    <button class="btn btn-secondary" type="submit">Plan aktivieren</button>
                </form>
                <% } %>
            </div>
            <div class="admin-program-danger-zone">
                <form class="inline-form admin-program-danger-form"
                      method="post"
                      action="<%= request.getContextPath() %>/admin/program/delete"
                      onsubmit="return confirm('Plan wirklich dauerhaft löschen? Dieser Schritt kann nicht rückgängig gemacht werden.');">
                    <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                    <button class="btn btn-danger admin-program-danger-btn" type="submit">Plan löschen</button>
                </form>
            </div>
        </div>
    </div>

    <div class="card admin-programs-weeks-card">
        <h2>Wochen und Trainings</h2>
        <div class="admin-week-add-row">
            <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/week/add">
                <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                <button class="btn admin-week-add-btn" type="submit">Nächste Woche hinzufügen</button>
            </form>
        </div>
        <% if (selectedWeeks.isEmpty()) { %>
        <p>Dieser Plan hat noch keine Wochen.</p>
        <% } else { %>
        <% for (PlanWeek week : selectedWeeks) { %>
        <div class="week-card admin-programs-week-card">
            <div class="admin-program-week-head">
                <h3>Woche <%= week.getWeekNo() %></h3>
                <div class="admin-program-week-actions">
                    <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/week/duplicate">
                        <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                        <input type="hidden" name="planWeekId" value="<%= week.getId() %>">
                        <button class="btn btn-secondary admin-week-duplicate-btn" type="submit">Woche duplizieren</button>
                    </form>
                    <form class="inline-form"
                          method="post"
                          action="<%= request.getContextPath() %>/admin/program/week/remove"
                          onsubmit="return confirm('Woche wirklich löschen? Die nachfolgenden Wochen werden neu nummeriert.');">
                        <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                        <input type="hidden" name="planWeekId" value="<%= week.getId() %>">
                        <button class="btn btn-danger admin-week-delete-btn" type="submit">Woche löschen</button>
                    </form>
                </div>
            </div>

            <%
                List<PlanWeekTraining> mappings = weekTrainings.get(week.getId());
            %>
            <% if (mappings == null || mappings.isEmpty()) { %>
            <p class="muted">Noch keine Trainings zugeordnet.</p>
            <% } else { %>
            <table class="admin-programs-table">
                <thead>
                <tr>
                    <th>Sortierung</th>
                    <th>Training</th>
                    <th>Status</th>
                    <th>Aktion</th>
                </tr>
                </thead>
                <tbody>
                <% for (PlanWeekTraining mapping : mappings) {
                    String trainingStatusClass = "status-open";
                    if ("PUBLISHED".equals(mapping.getTrainingStatus())) {
                        trainingStatusClass = "status-done";
                    } else if ("HIDDEN".equals(mapping.getTrainingStatus())) {
                        trainingStatusClass = "status-in-progress";
                    }
                %>
                <tr>
                    <td><%= mapping.getSortOrder() %></td>
                    <td><%= mapping.getTrainingTitle() %></td>
                    <td><span class="status-badge <%= trainingStatusClass %>"><%= mapping.getTrainingStatus() %></span></td>
                    <td>
                        <form class="inline-form" method="post" action="<%= request.getContextPath() %>/admin/program/week/training/remove">
                            <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                            <input type="hidden" name="mappingId" value="<%= mapping.getId() %>">
                            <button class="btn btn-ghost" type="submit">Training entfernen</button>
                        </form>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
            <% } %>

            <h3>Training zu Woche hinzufügen</h3>
            <% if (trainings == null || trainings.isEmpty()) { %>
            <p>Keine Trainings vorhanden. Bitte zuerst unter Admin Trainings ein Training anlegen.</p>
            <% } else { %>
            <form class="admin-form" method="post" action="<%= request.getContextPath() %>/admin/program/week/training/add">
                <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                <input type="hidden" name="planWeekId" value="<%= week.getId() %>">

                <label>Training</label>
                <select name="trainingId" required>
                    <% for (Training training : trainings) { %>
                    <option value="<%= training.getId() %>">
                        <%= training.getTitle() %> (<%= training.getStatus() %>)
                    </option>
                    <% } %>
                </select>

                <label>Sortierung</label>
                <input name="sortOrder" type="number" min="1" required>

                <div class="form-row-actions">
                    <button class="btn btn-primary" type="submit">Training hinzufügen</button>
                </div>
            </form>
            <% } %>
        </div>
        <% } %>
        <% } %>
    </div>
    <% } %>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
