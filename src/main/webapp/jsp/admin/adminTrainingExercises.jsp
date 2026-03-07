<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="app.model.Training" %>
<%@ page import="app.model.Exercise" %>
<%@ page import="app.model.MuscleGroup" %>
<%@ page import="app.model.TrainingExercise" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Training Übungen</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body>
<jsp:useBean id="adminTrainingExercisesBean" class="app.beans.AdminTrainingExercisesBean" scope="request" />
<%
    Training training = adminTrainingExercisesBean.getTraining();
    List<TrainingExercise> mappings = adminTrainingExercisesBean.getMappings();
    List<Exercise> activeExercises = adminTrainingExercisesBean.getActiveExercises();
    List<MuscleGroup> muscleGroups = adminTrainingExercisesBean.getMuscleGroups();

    String search = adminTrainingExercisesBean.getSearch();
    Long selectedMuscleGroupId = adminTrainingExercisesBean.getSelectedMuscleGroupId();
    String sortDir = adminTrainingExercisesBean.getSortDir();
    String trainingId = adminTrainingExercisesBean.getTrainingIdText();
%>
<div class="page-container">
    <div class="admin-trainings-brand-row">
        <a class="brand-home-link" href="<%= request.getContextPath() %>/home" aria-label="Home">
            <img class="admin-trainings-brand-logo"
                 src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
                 alt="vorn">
        </a>
    </div>

    <a class="subtle-back-link" href="<%= request.getContextPath() %>/admin/trainings">← Zurück</a>

    <div class="page-header">
        <div>
            <h1>Übungen einem Training zuordnen</h1>
        </div>
        <div class="top-actions">
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
    </div>

    <% if (adminTrainingExercisesBean.getInfo() != null && !adminTrainingExercisesBean.getInfo().isBlank()) { %>
    <div class="info">${adminTrainingExercisesBean.info}</div>
    <% } %>
    <% if (adminTrainingExercisesBean.getError() != null && !adminTrainingExercisesBean.getError().isBlank()) { %>
    <div class="error">${adminTrainingExercisesBean.error}</div>
    <% } %>

    <% if (training != null) { %>
    <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>Training: <%= training.getTitle() %></h2>
            <span class="status-badge <%= "PUBLISHED".equals(training.getStatus())
                    ? "status-done"
                    : ("HIDDEN".equals(training.getStatus()) ? "status-in-progress" : "status-open") %>">
                <%= training.getStatus() %>
            </span>
        </div>
    </div>

    <div class="card">
        <h3 style="margin-bottom: 1rem;">Übungen filtern & suchen</h3>
        <form class="admin-filter-bar" method="get" action="<%= request.getContextPath() %>/admin/training/exercises">
            <input type="hidden" name="trainingId" value="<%= trainingId %>">

            <div class="filter-group">
                <label for="search">Übungsname</label>
                <input type="text" id="search" name="search" value="<%= search %>" placeholder="Suchen...">
            </div>

            <div class="filter-group">
                <label for="muscleGroupId">Muskelgruppe</label>
                <select id="muscleGroupId" name="muscleGroupId">
                    <option value="">Alle Muskelgruppen</option>
                    <% for (MuscleGroup mg : muscleGroups) { %>
                    <option value="<%= mg.getId() %>"
                        <%= selectedMuscleGroupId != null && selectedMuscleGroupId.equals(mg.getId()) ? "selected" : "" %>>
                        <%= mg.getLabel() %>
                    </option>
                    <% } %>
                </select>
            </div>

            <div class="filter-group">
                <label for="sortDir">Sortierung</label>
                <select id="sortDir" name="sortDir">
                    <option value="asc" <%= "asc".equalsIgnoreCase(sortDir) ? "selected" : "" %>>Name A-Z</option>
                    <option value="desc" <%= "desc".equalsIgnoreCase(sortDir) ? "selected" : "" %>>Name Z-A</option>
                </select>
            </div>

            <div class="filter-actions">
                <button class="btn btn-primary" type="submit">Filtern</button>
                <a class="btn btn-ghost"
                   href="<%= request.getContextPath() %>/admin/training/exercises?trainingId=<%= trainingId %>">Reset</a>
            </div>
        </form>
    </div>

    <div class="card">
        <h2>Neue Zuordnung anlegen</h2>
        <% if (!adminTrainingExercisesBean.hasActiveExercises()) { %>
        <p class="info">Keine passenden ACTIVE-Übungen gefunden. Bitte Filter anpassen.</p>
        <% } else { %>
        <form class="admin-form" method="post" action="<%= request.getContextPath() %>/admin/training/exercise/add">
            <input type="hidden" name="trainingId" value="<%= trainingId %>">
            <input type="hidden" name="search" value="<%= search %>">
            <input type="hidden" name="muscleGroupId" value="<%= adminTrainingExercisesBean.getSelectedMuscleGroupIdText() %>">
            <input type="hidden" name="sortDir" value="<%= sortDir %>">

            <label for="exerciseId">Übung wählen</label>
            <select id="exerciseId" name="exerciseId" required>
                <option value="" disabled selected>-- Übung auswählen --</option>
                <% for (Exercise exercise : activeExercises) {
                    String labels = String.join(", ", exercise.getMuscleGroupLabels());
                %>
                <option value="<%= exercise.getId() %>">
                    <%= exercise.getName() %><%= labels.isEmpty() ? "" : " — " + labels %>
                </option>
                <% } %>
            </select>

            <div class="grid" style="margin-top: 1rem;">
                <div>
                    <label for="plannedSets">Plan-Sets</label>
                    <input id="plannedSets" name="plannedSets" type="number" min="1" value="3" required>
                </div>
                <div>
                    <label for="plannedReps">Plan-Reps</label>
                    <input id="plannedReps" name="plannedReps" type="number" min="1" value="10" required>
                </div>
                <div>
                    <label for="sortOrder">Sortierung (Reihenfolge)</label>
                    <input id="sortOrder"
                           name="sortOrder"
                           type="number"
                           min="1"
                           value="<%= adminTrainingExercisesBean.getNextSortOrder() %>"
                           required>
                </div>
            </div>

            <div class="form-row-actions" style="margin-top: 1.5rem;">
                <button class="btn btn-primary" type="submit">Zuordnung speichern</button>
            </div>
        </form>
        <% } %>
    </div>

    <div class="card">
        <h2>Bestehende Übungen in diesem Training</h2>
        <% if (!adminTrainingExercisesBean.hasMappings()) { %>
        <p class="muted">Noch keine Übungen zugeordnet.</p>
        <% } else { %>
        <table>
            <thead>
            <tr>
                <th style="width: 80px; text-align: center;">Pos.</th>
                <th>Übung</th>
                <th>Planwerte</th>
                <th>Update / Verwaltung</th>
            </tr>
            </thead>
            <tbody>
            <% for (TrainingExercise mapping : mappings) { %>
            <tr>
                <td class="mono" style="text-align: center; vertical-align: middle;">
                    <%= mapping.getSortOrder() %>
                </td>
                <td style="vertical-align: middle;">
                    <strong><%= mapping.getExerciseName() %></strong>
                </td>
                <td style="vertical-align: middle;">
                    <%= mapping.getPlannedSets() %> Sets x <%= mapping.getPlannedReps() %> Reps
                </td>
                <td>
                    <form class="inline-form mapping-update-form"
                          method="post"
                          action="<%= request.getContextPath() %>/admin/training/exercise/update">
                        <input type="hidden" name="trainingId" value="<%= trainingId %>">
                        <input type="hidden" name="mappingId" value="<%= mapping.getId() %>">
                        <input type="hidden" name="search" value="<%= search %>">
                        <input type="hidden" name="muscleGroupId" value="<%= adminTrainingExercisesBean.getSelectedMuscleGroupIdText() %>">
                        <input type="hidden" name="sortDir" value="<%= sortDir %>">

                        <div class="inline-grid">
                            <div>
                                <label>Sets</label>
                                <input name="plannedSets"
                                       type="number"
                                       min="1"
                                       value="<%= mapping.getPlannedSets() %>"
                                       required
                                       style="max-width: 60px;">
                            </div>
                            <div>
                                <label>Reps</label>
                                <input name="plannedReps"
                                       type="number"
                                       min="1"
                                       value="<%= mapping.getPlannedReps() %>"
                                       required
                                       style="max-width: 60px;">
                            </div>
                            <div>
                                <label>Sort.</label>
                                <input name="sortOrder"
                                       type="number"
                                       min="1"
                                       value="<%= mapping.getSortOrder() %>"
                                       required
                                       style="max-width: 60px;">
                            </div>
                            <div>
                                <button class="btn btn-secondary" type="submit">Update</button>
                            </div>
                        </div>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>
    <% } %>
</div>
<script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
