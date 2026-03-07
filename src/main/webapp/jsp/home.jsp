<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="app.model.Plan" %>
<%@ page import="app.model.WorkoutSession" %>
<%@ page import="app.service.ProgramService.NextTrainingData" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="home-dashboard-page">
<jsp:useBean id="homeDashboardBean" class="app.beans.HomeDashboardBean" scope="request" />
<%
    boolean isAdmin = homeDashboardBean.isAdmin();
    WorkoutSession activeSession = homeDashboardBean.getActiveSession();
    String activeSessionError = homeDashboardBean.getActiveSessionError();
    String dashboardError = homeDashboardBean.getDashboardError();
    Plan dashboardSelectedPlan = homeDashboardBean.getDashboardSelectedPlan();
    NextTrainingData dashboardNextTraining = homeDashboardBean.getDashboardNextTraining();
%>
<div class="home-shell page-container">
<div class="home-brand-row">
    <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
        <img class="home-brand-logo"
             src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
             alt="vorn">
    </a>
</div>
<div class="page-header">
    <div>
        <h1>Willkommen, ${homeDashboardBean.userDisplayName}</h1>
    </div>
    <div class="top-actions">
        <% if (isAdmin) { %>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/admin/console">Admin Konsole</a>
        <% } else { %>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/plan/details">Plan ansehen</a>
        <% if (activeSession != null) { %>
        <a class="btn btn-primary" href="<%= request.getContextPath() %>/session/track?id=<%= activeSession.getId() %>">
            Session fortsetzen
        </a>
        <% } %>
        <% } %>
        <form class="inline-form" method="post" action="<%= request.getContextPath() %>/logout">
            <button class="btn btn-ghost" type="submit">Logout</button>
        </form>
    </div>
</div>
<% if (homeDashboardBean.getInfo() != null && !homeDashboardBean.getInfo().isBlank()) { %>
<div class="info">${homeDashboardBean.info}</div>
<% } %>
<% if (homeDashboardBean.getError() != null && !homeDashboardBean.getError().isBlank()) { %>
<div class="error">${homeDashboardBean.error}</div>
<% } %>
<% if (activeSessionError != null) { %>
<div class="error"><%= activeSessionError %></div>
<% } %>

<div class="box home-dashboard-box">
    <h2>Dein Dashboard</h2>
    <% if (dashboardError != null) { %>
    <div class="error"><%= dashboardError %></div>
    <% } else if (dashboardSelectedPlan == null) { %>
    <div class="home-empty-state">
        <p><strong>Kein aktiver Plan verfügbar.</strong></p>
        <p class="muted">Öffne die Programmauswahl und setze einen aktiven Plan.</p>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/program/select">Zur Programmauswahl</a>
    </div>
    <% } else { %>
    <div class="kpi-grid">
        <div class="kpi-card">
            <div><strong>Nächstes Training</strong></div>
            <% if (dashboardNextTraining != null) { %>
            <div class="kpi-value"><%= dashboardNextTraining.getTrainingTitle() %></div>
            <div class="muted">Woche <%= dashboardNextTraining.getWeekNo() %></div>
            <form class="inline-form" method="post" action="<%= request.getContextPath() %>/program/session/start">
                <input type="hidden" name="planId" value="<%= dashboardSelectedPlan.getId() %>">
                <input type="hidden" name="planWeekId" value="<%= dashboardNextTraining.getPlanWeekId() %>">
                <input type="hidden" name="trainingId" value="<%= dashboardNextTraining.getTrainingId() %>">
                <button class="btn btn-primary" type="submit">Starten</button>
            </form>
            <% } else { %>
            <div class="muted">Kein offenes Training im aktiven Plan.</div>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/plan/details">Plan ansehen</a>
            <% } %>
        </div>
        <div class="kpi-card">
            <div class="kpi-value"><%= homeDashboardBean.getWorkoutsCompletedText() %></div>
            <div>Abgeschlossene Workouts</div>
        </div>
        <div class="kpi-card">
            <div class="kpi-value"><%= homeDashboardBean.getHoursSpentText() %></div>
            <div>Trainingszeit</div>
        </div>
    </div>

    <div class="home-plan-hero">
        <img class="home-plan-hero-image"
             src="<%= homeDashboardBean.getSelectedPlanImagePath() %>"
             onerror="this.onerror=null;this.src='<%= homeDashboardBean.getFallbackPlanImagePath() %>';"
             alt="Planbild">
        <div class="home-plan-hero-overlay"></div>
        <div class="home-plan-hero-content">
            <div class="home-plan-hero-plan-name"><%= dashboardSelectedPlan.getName() %></div>
            <% if (dashboardSelectedPlan.getDescription() != null && !dashboardSelectedPlan.getDescription().isBlank()) { %>
            <p class="home-plan-hero-description"><%= dashboardSelectedPlan.getDescription() %></p>
            <% } %>
            <div class="home-plan-hero-switch">
                <a class="btn btn-secondary home-switch-btn" href="<%= request.getContextPath() %>/plan/details">Plan ansehen</a>
                <a class="btn btn-secondary home-switch-btn" href="<%= request.getContextPath() %>/program/select">Plan wechseln</a>
            </div>
        </div>
        <div class="home-plan-progress-ring"
             style="--progress: <%= homeDashboardBean.getProgressPercent() %>;"
             aria-label="Fortschritt <%= homeDashboardBean.getProgressPercent() %> Prozent">
            <span><%= homeDashboardBean.getProgressPercent() %>%</span>
        </div>
    </div>
    <% } %>
</div>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
