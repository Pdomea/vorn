<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="app.model.Plan" %>
<%@ page import="app.model.PlanWeek" %>
<%@ page import="app.model.PlanWeekTraining" %>
<%@ page import="app.service.ProgramService.NextTrainingData" %>
<%@ page import="app.service.ProgramService.WeekProgressData" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Plan-Details</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="program-page plan-details-page">
    <jsp:useBean id="planDetailsBean" class="app.beans.PlanDetailsBean" scope="request" />
    <% 
        Plan selectedPlan = planDetailsBean.getSelectedPlan(); 
        List<PlanWeek> selectedPlanWeeks = planDetailsBean.getSelectedPlanWeeks();
        PlanWeek selectedDetailWeek = planDetailsBean.getSelectedDetailWeek();
        List<PlanWeekTraining> selectedDetailWeekTrainings = planDetailsBean.getSelectedDetailWeekTrainings();
        Map<Long, String> selectedDetailWeekStatuses = planDetailsBean.getSelectedDetailWeekStatuses();
        WeekProgressData selectedDetailWeekProgress = planDetailsBean.getSelectedDetailWeekProgress();
        NextTrainingData selectedPlanNextTraining = planDetailsBean.getSelectedPlanNextTraining();
        int completedSlots = planDetailsBean.getCompletedSlots();
        int totalSlots = planDetailsBean.getTotalSlots();
        int progressPercent = planDetailsBean.getProgressPercent();
        String nextTrainingText = planDetailsBean.getNextTrainingText();
        String fallbackPlanImagePath = planDetailsBean.getFallbackPlanImagePath();
        String selectedPlanImagePath = planDetailsBean.getSelectedPlanImagePath();
    %>
    <div class="program-shell page-container">
        <div class="program-brand-row">
            <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
                <img class="program-brand-logo" src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg" alt="vorn">
            </a>
        </div>
        
        <% if (planDetailsBean.isPreviewMode()) { %>
            <a class="subtle-back-link" href="<%= request.getContextPath() %>/program/select">← Zurück zur Auswahl</a>
        <% } else { %>
            <a class="subtle-back-link" href="<%= request.getContextPath() %>/home">← Zurück</a>
        <% } %>

        <div class="page-header">
            <div>
                <h1>Dein Plan</h1>
            </div>
            <div class="top-actions">
                <% if (planDetailsBean.isPreviewMode()) { %>
                    <a class="btn btn-ghost" href="<%= request.getContextPath() %>/program/select">Plan Auswahl</a>
                <% } else { %>
                    <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
                <% } %>
            </div>
        </div>

        <% if (planDetailsBean.getInfo() != null && !planDetailsBean.getInfo().isBlank()) { %>
            <div class="info">
                <%= planDetailsBean.getInfo() %>
            </div>
        <% } %>
        
        <% if (planDetailsBean.getError() != null && !planDetailsBean.getError().isBlank()) { %>
            <div class="error">
                <%= planDetailsBean.getError() %>
            </div>
        <% } %>

        <% if (planDetailsBean.isPreviewMode()) { %>
            <div class="info" style="margin-bottom: 2rem; background: var(--bg-tertiary); border: 1px solid var(--accent); color: var(--text-primary);">
                <strong>👀 Vorschau-Modus:</strong> Du siehst dir diesen Plan aktuell nur an. Fortschritt wird nicht gespeichert.
                <div style="margin-top: 1rem;">
                    <form method="post" action="<%= request.getContextPath() %>/program/select" style="display:inline;">
                        <input type="hidden" name="planId" value="<%= selectedPlan != null ? selectedPlan.getId() : "" %>">
                        <button type="submit" class="btn btn-primary">Plan jetzt starten</button>
                    </form>
                </div>
            </div>
        <% } %>

        <% if (selectedPlan == null) { %>
            <div class="card plan-details-empty">
                <p><strong>Kein aktiver Plan vorhanden.</strong></p>
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/program/select">Plan wählen</a>
            </div>
        <% } else { %>
            <section class="plan-details-meta-line" aria-label="Planstatus">
                <span><strong>Fortschritt:</strong> <%= completedSlots %>/<%= totalSlots %> (<%= progressPercent %>%)</span>
                <span class="plan-details-meta-sep">·</span>
                <span><strong>Nächstes Training:</strong> <%= nextTrainingText %></span>
            </section>

            <section class="plan-details-hero">
                <img class="plan-details-hero-image" src="<%= selectedPlanImagePath %>" 
                     onerror="this.onerror=null;this.src='<%= fallbackPlanImagePath %>';" alt="Planbild">
                <div class="plan-details-hero-overlay"></div>
                <div class="plan-details-hero-content">
                    <h2><%= selectedPlan.getName() %></h2>
                    <% if (selectedPlan.getDescription() != null && !selectedPlan.getDescription().isBlank()) { %>
                        <p class="muted"><%= selectedPlan.getDescription() %></p>
                    <% } %>
                    <div class="actions">
                        <% if (selectedPlanNextTraining != null) { %>
                            <form class="inline-form" method="post" action="<%= request.getContextPath() %>/program/session/start">
                                <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                                <input type="hidden" name="planWeekId" value="<%= selectedPlanNextTraining.getPlanWeekId() %>">
                                <input type="hidden" name="trainingId" value="<%= selectedPlanNextTraining.getTrainingId() %>">
                                <button class="btn btn-primary" type="submit">Nächstes Training starten</button>
                            </form>
                        <% } %>
                        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/program/select">Plan wechseln</a>
                    </div>
                </div>
                <div class="plan-details-progress-ring" style="--progress: <%= progressPercent %>;" aria-label="Fortschritt <%= progressPercent %> Prozent">
                    <span><%= progressPercent %>%</span>
                </div>
            </section>

            <section class="plan-details-weeks">
                <% if (selectedPlanWeeks == null || selectedPlanWeeks.isEmpty()) { %>
                    <div class="plan-details-week plan-details-empty">
                        <p><strong>Noch keine Wochen vorhanden.</strong></p>
                        <p class="muted">Der Plan wurde angelegt, aber noch nicht mit Wochen befüllt.</p>
                    </div>
                <% } else { %>
                    <div class="plan-details-week-nav">
                        <% for (PlanWeek week : selectedPlanWeeks) {
                            boolean activeWeek = selectedDetailWeek != null && selectedDetailWeek.getId() == week.getId();
                        %>
                            <a class="plan-details-week-link <%= activeWeek ? "is-active" : "" %>" 
                               href="<%= request.getContextPath() %>/plan/details?planId=<%= selectedPlan.getId() %>&weekNo=<%= week.getWeekNo() %>">
                                Woche <%= week.getWeekNo() %>
                            </a>
                        <% } %>
                    </div>
                <% } %>

                <% if (selectedDetailWeek == null) { %>
                    <div class="plan-details-week plan-details-empty">
                        <p><strong>Woche konnte nicht geladen werden.</strong></p>
                    </div>
                <% } else { %>
                    <article class="plan-details-week">
                        <div class="plan-details-week-head">
                            <h3>Woche <%= selectedDetailWeek.getWeekNo() %></h3>
                            <% if (selectedDetailWeekProgress != null) { %>
                                <span class="plan-details-week-progress">
                                    <%= selectedDetailWeekProgress.getCompletedSlots() %>/<%= selectedDetailWeekProgress.getTotalSlots() %> abgeschlossen
                                </span>
                            <% } %>
                        </div>

                        <% if (selectedDetailWeekTrainings == null || selectedDetailWeekTrainings.isEmpty()) { %>
                            <div class="plan-details-empty">
                                <p><strong>Keine veröffentlichten Trainings in dieser Woche.</strong></p>
                                <p class="muted">Sobald Trainings veröffentlicht sind, erscheinen sie hier mit Start-Button.</p>
                            </div>
                        <% } else { %>
                            <ul class="plan-details-list">
                                <% for (PlanWeekTraining mapping : selectedDetailWeekTrainings) {
                                    String slotStatus = selectedDetailWeekStatuses == null ? "OFFEN" : selectedDetailWeekStatuses.getOrDefault(mapping.getTrainingId(), "OFFEN");
                                    String statusClass = "status-open";
                                    String statusLabel = "Offen";
                                    String actionLabel = "Training starten";
                                    String actionButtonClass = "btn btn-secondary";
                                    
                                    if ("ABGESCHLOSSEN".equals(slotStatus)) {
                                        statusClass = "status-done";
                                        statusLabel = "Abgeschlossen";
                                        actionLabel = "Erneut starten";
                                        actionButtonClass = "btn btn-ghost";
                                    } else if ("IN_BEARBEITUNG".equals(slotStatus)) {
                                        statusClass = "status-in-progress";
                                        statusLabel = "In Bearbeitung";
                                        actionLabel = "Session fortsetzen";
                                        actionButtonClass = "btn btn-primary";
                                    }
                                %>
                                    <li class="plan-details-item">
                                        <div class="plan-details-item-main">
                                            <span class="plan-details-order"><%= mapping.getSortOrder() %></span>
                                            <span class="plan-details-title"><%= mapping.getTrainingTitle() %></span>
                                            <span class="status-badge <%= statusClass %>"><%= statusLabel %></span>
                                        </div>
                                        <div class="plan-details-item-action">
                                            <form class="inline-form" method="post" action="<%= request.getContextPath() %>/program/session/start">
                                                <input type="hidden" name="planId" value="<%= selectedPlan.getId() %>">
                                                <input type="hidden" name="planWeekId" value="<%= selectedDetailWeek.getId() %>">
                                                <input type="hidden" name="trainingId" value="<%= mapping.getTrainingId() %>">
                                                <button class="<%= actionButtonClass %>" type="submit"><%= actionLabel %></button>
                                            </form>
                                        </div>
                                    </li>
                                <% } %>
                            </ul>
                        <% } %>
                    </article>
                <% } %>
            </section>
        <% } %>
    </div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
