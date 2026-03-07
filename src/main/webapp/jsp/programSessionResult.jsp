<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="app.model.WorkoutSession" %>
<%@ page import="app.model.SessionExercise" %>
<%@ page import="app.model.WorkoutLog" %>
<%@ page import="app.service.TrackingService.SessionResultData" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Workout Ergebnis</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="result-page">
<jsp:useBean id="sessionResultBean" class="app.beans.SessionResultBean" scope="request" />
<%
    String backUrl = sessionResultBean.getBackUrl();
    String backLabel = sessionResultBean.getBackLabel();
    SessionResultData resultData = sessionResultBean.getResultData();
%>
<div class="result-shell page-container">
<div class="result-brand-row">
    <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
        <img class="result-brand-logo"
             src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
             alt="vorn">
    </a>
</div>
<a class="subtle-back-link" href="<%= request.getContextPath() %>/home">← Zurück</a>
<div class="page-header">
    <div>
        <p class="page-eyebrow">Core Performance</p>
        <h1>Workout Ergebnis</h1>
        <p class="muted">Deine gespeicherten Sets und Kennzahlen aus dieser Session.</p>
    </div>
    <div class="top-actions">
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        <a class="btn btn-ghost" href="<%= request.getContextPath() %>/plan/details">Wochenplan</a>
        <a class="btn btn-secondary" href="<%= backUrl == null ? (request.getContextPath() + "/plan/details") : backUrl %>">
            <%= backLabel == null ? "Zurück" : backLabel %>
        </a>
    </div>
</div>

<% if (sessionResultBean.getInfo() != null && !sessionResultBean.getInfo().isBlank()) { %>
<div class="info">${sessionResultBean.info}</div>
<% } %>
<% if (sessionResultBean.getError() != null && !sessionResultBean.getError().isBlank()) { %>
<div class="error">${sessionResultBean.error}</div>
<% } %>

<% if (resultData != null) { 
    WorkoutSession sessionData = resultData.getSession();
    String durationText = sessionResultBean.getDurationText();
    String totalVolumeText = sessionResultBean.getTotalVolumeText();
%>
<div class="meta result-session-meta">
    <p><strong>Session-ID:</strong> <%= sessionData.getId() %></p>
    <p><strong>Training:</strong> <%= sessionData.getTrainingTitle() %></p>
    <p><strong>Beendet:</strong> <%= sessionData.getEndedAt() %></p>
    <p><strong>Dauer:</strong> <%= durationText %></p>
    <p><strong>Gesamt:</strong> Sets <%= resultData.getLoggedSets() %>,
        Reps <%= resultData.getTotalReps() %>, Volumen <%= totalVolumeText %></p>
</div>

<% 
    List<SessionExercise> items = resultData.getSnapshotItems();
    Map<Long, List<WorkoutLog>> logsByExercise = resultData.getLogsByExercise();
    if (items == null || items.isEmpty()) { 
%>
<p>Keine Übungen in dieser Session gefunden.</p>
<% } else { 
    for (SessionExercise item : items) { 
        List<WorkoutLog> logs = logsByExercise == null ? null : logsByExercise.get(item.getId());
%>
<div class="card result-exercise-card">
    <p><strong>#<%= item.getSortOrder() %> - <%= item.getExerciseNameSnapshot() %></strong></p>
    <p class="muted">Plan: <%= item.getPlannedSetsSnapshot() %> Sets x <%= item.getPlannedRepsSnapshot() %> Reps</p>
    <table class="plan-table result-table">
        <thead>
        <tr>
            <th>Satz</th>
            <th>Reps</th>
            <th>Gewicht (kg)</th>
            <th>Notiz</th>
        </tr>
        </thead>
        <tbody>
        <% if (logs == null || logs.isEmpty()) { %>
        <tr>
            <td colspan="4">Keine Logs für diese Übung gespeichert.</td>
        </tr>
        <% } else { 
            for (WorkoutLog log : logs) {
                String weightValue = log.getWeight() == null ? "0" : log.getWeight().stripTrailingZeros().toPlainString();
                String noteValue = log.getNote() == null || log.getNote().isBlank() ? "-" : log.getNote();
        %>
        <tr>
            <td><%= log.getSetNo() %></td>
            <td><%= log.getReps() %></td>
            <td><%= weightValue %></td>
            <td><%= noteValue %></td>
        </tr>
        <% } 
           } %>
        </tbody>
    </table>
</div>
<%  } 
   } 
 } %>

</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
