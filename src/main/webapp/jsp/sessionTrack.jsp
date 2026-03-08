<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="java.util.Map" %>
            <%@ page import="java.math.BigDecimal" %>
                <%@ page import="app.model.WorkoutSession" %>
                    <%@ page import="app.model.SessionExercise" %>
                        <%@ page import="app.model.WorkoutLog" %>
                            <%@ page import="app.model.Exercise" %>
                                <!DOCTYPE html>
                                <html lang="de">

                                <head>
                                    <meta charset="UTF-8">
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <title>Session Tracking</title>
                                    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
                                    <link rel="icon" type="image/svg+xml"
                                        href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
                                </head>

                                <body class="track-page">
                                    <jsp:useBean id="sessionTrackBean" class="app.beans.SessionTrackBean"
                                        scope="request" />
                                    <% WorkoutSession sessionData=sessionTrackBean.getSessionData();
                                        List<SessionExercise> items = sessionTrackBean.getItems();
                                        Map<Long, List<WorkoutLog>> logsByExercise =
                                            sessionTrackBean.getLogsByExercise();
                                            Map<Long, BigDecimal> lastScoreByExerciseId =
                                                sessionTrackBean.getLastScoreByExerciseId();
                                                Map<Long, List<Exercise>> alternativenMap =
                                                    sessionTrackBean.getAlternativenMap();
                                                    boolean activeSession = sessionTrackBean.isActiveSession();
                                                    Long startedAtEpochMillis =
                                                    sessionTrackBean.getStartedAtEpochMillis();
                                                    %>
                                                    <div class="track-shell page-container">
                                                        <div class="track-brand-row">
                                                            <a class="brand-home-link"
                                                                href="<%= request.getContextPath() %>/home"
                                                                aria-label="Home">
                                                                <img class="track-brand-logo"
                                                                    src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
                                                                    alt="vorn">
                                                            </a>
                                                        </div>
                                                        <div class="page-header">
                                                            <div>
                                                                <h1>Session Tracking</h1>
                                                                <% if (sessionData !=null) { %>
                                                                    <p class="muted">
                                                                        <%= sessionData.getTrainingTitle() %>
                                                                    </p>
                                                                    <% } %>
                                                            </div>
                                                            <div class="top-actions">
                                                                <a class="btn btn-ghost"
                                                                    href="<%= request.getContextPath() %>/home">Zurück
                                                                    zu Home</a>
                                                                <a class="btn btn-ghost"
                                                                    href="<%= request.getContextPath() %>/plan/details">Wochenplan</a>
                                                            </div>
                                                        </div>

                                                        <% if (sessionTrackBean.getInfo() !=null &&
                                                            !sessionTrackBean.getInfo().isBlank()) { %>
                                                            <div class="info">${sessionTrackBean.info}</div>
                                                            <% } %>
                                                                <% if (sessionTrackBean.getError() !=null &&
                                                                    !sessionTrackBean.getError().isBlank()) { %>
                                                                    <div class="error">${sessionTrackBean.error}</div>
                                                                    <% } %>

                                                                        <% if (sessionData !=null) { %>
                                                                            <div class="meta track-session-meta">
                                                                                <% if (activeSession) { %>
                                                                                    <p><strong>Trainingszeit:</strong>
                                                                                        <span
                                                                                            id="sessionDuration">00:00:00</span>
                                                                                    </p>
                                                                                    <% } %>
                                                                            </div>

                                                                            <% if (items==null || items.isEmpty()) { %>
                                                                                <p>Keine Snapshot-Übungen vorhanden.</p>
                                                                                <% } else { %>
                                                                                    <% if (activeSession) { %>
                                                                                        <form id="swapSubmitForm"
                                                                                            method="post"
                                                                                            action="<%= request.getContextPath() %>/session/track/swap"
                                                                                            hidden>
                                                                                            <input type="hidden"
                                                                                                name="sessionId"
                                                                                                value="<%= sessionData.getId() %>">
                                                                                            <input type="hidden"
                                                                                                name="sessionExerciseId"
                                                                                                id="swapSessionExerciseId"
                                                                                                value="">
                                                                                            <input type="hidden"
                                                                                                name="replacementExerciseId"
                                                                                                id="swapReplacementExerciseId"
                                                                                                value="">
                                                                                        </form>
                                                                                        <form method="post"
                                                                                            action="<%= request.getContextPath() %>/session/finish">
                                                                                            <input type="hidden"
                                                                                                name="sessionId"
                                                                                                value="<%= sessionData.getId() %>">
                                                                                            <% } %>

                                                                                                <% for (SessionExercise
                                                                                                    item : items) {
                                                                                                    List<WorkoutLog>
                                                                                                    itemLogs =
                                                                                                    logsByExercise ==
                                                                                                    null ? null :
                                                                                                    logsByExercise.get(item.getId());
                                                                                                    boolean hasSavedLogs
                                                                                                    = itemLogs != null
                                                                                                    &&
                                                                                                    !itemLogs.isEmpty();
                                                                                                    java.util.Map
                                                                                                    <Integer,
                                                                                                        WorkoutLog>
                                                                                                        existingBySet =
                                                                                                        new
                                                                                                        java.util.HashMap
                                                                                                        <>();
                                                                                                            BigDecimal
                                                                                                            itemCurrentScore
                                                                                                            =
                                                                                                            BigDecimal.ZERO;
                                                                                                            if (itemLogs
                                                                                                            != null) {
                                                                                                            for
                                                                                                            (WorkoutLog
                                                                                                            log :
                                                                                                            itemLogs) {
                                                                                                            existingBySet.put(log.getSetNo(),
                                                                                                            log);
                                                                                                            if
                                                                                                            (log.getWeight()
                                                                                                            != null) {
                                                                                                            itemCurrentScore
                                                                                                            =
                                                                                                            itemCurrentScore.add(log.getWeight().multiply(BigDecimal.valueOf(log.getReps())));
                                                                                                            }
                                                                                                            }
                                                                                                            }
                                                                                                            BigDecimal
                                                                                                            lastScore =
                                                                                                            lastScoreByExerciseId
                                                                                                            == null ?
                                                                                                            null :
                                                                                                            lastScoreByExerciseId.get(item.getExerciseId());
                                                                                                            String
                                                                                                            lastScoreText
                                                                                                            = lastScore
                                                                                                            == null ?
                                                                                                            "-" :
                                                                                                            lastScore.stripTrailingZeros().toPlainString();
                                                                                                            String
                                                                                                            currentScoreText
                                                                                                            =
                                                                                                            itemCurrentScore.stripTrailingZeros().toPlainString();
                                                                                                            List
                                                                                                            <Exercise>
                                                                                                                swapCandidates
                                                                                                                =
                                                                                                                alternativenMap
                                                                                                                == null
                                                                                                                ? null
                                                                                                                :
                                                                                                                alternativenMap.get(item.getId());
                                                                                                                %>
                                                                                                                <div class="card track-exercise-card"
                                                                                                                    id="exercise-<%= item.getId() %>">
                                                                                                                    <p><strong>#
                                                                                                                            <%= item.getSortOrder()
                                                                                                                                %>
                                                                                                                                -
                                                                                                                                <%= item.getExerciseNameSnapshot()
                                                                                                                                    %>
                                                                                                                        </strong>
                                                                                                                    </p>
                                                                                                                    <p
                                                                                                                        class="muted">
                                                                                                                        Plan:
                                                                                                                        <%= item.getPlannedSetsSnapshot()
                                                                                                                            %>
                                                                                                                            Sets
                                                                                                                            x
                                                                                                                            <%= item.getPlannedRepsSnapshot()
                                                                                                                                %>
                                                                                                                                Reps
                                                                                                                    </p>
                                                                                                                    <% if
                                                                                                                        (activeSession)
                                                                                                                        {
                                                                                                                        %>
                                                                                                                        <div
                                                                                                                            class="track-swap-row">
                                                                                                                            <% if
                                                                                                                                (hasSavedLogs)
                                                                                                                                {
                                                                                                                                %>
                                                                                                                                <p
                                                                                                                                    class="muted track-swap-hint">
                                                                                                                                    Tausch
                                                                                                                                    gesperrt:
                                                                                                                                    Für
                                                                                                                                    diese
                                                                                                                                    Übung
                                                                                                                                    wurden
                                                                                                                                    bereits
                                                                                                                                    Sets
                                                                                                                                    gespeichert.
                                                                                                                                </p>
                                                                                                                                <% } else
                                                                                                                                    if
                                                                                                                                    (swapCandidates==null
                                                                                                                                    ||
                                                                                                                                    swapCandidates.isEmpty())
                                                                                                                                    {
                                                                                                                                    %>
                                                                                                                                    <p
                                                                                                                                        class="muted track-swap-hint">
                                                                                                                                        Keine
                                                                                                                                        passende
                                                                                                                                        Ersatzübung
                                                                                                                                        in
                                                                                                                                        derselben
                                                                                                                                        Muskelgruppe
                                                                                                                                        verfügbar.
                                                                                                                                    </p>
                                                                                                                                    <% } else
                                                                                                                                        {
                                                                                                                                        %>
                                                                                                                                        <div
                                                                                                                                            class="track-swap-form">
                                                                                                                                            <select
                                                                                                                                                class="track-swap-select"
                                                                                                                                                data-session-exercise-id="<%= item.getId() %>">
                                                                                                                                                <option
                                                                                                                                                    value=""
                                                                                                                                                    selected
                                                                                                                                                    disabled>
                                                                                                                                                    Übung
                                                                                                                                                    tauschen
                                                                                                                                                </option>
                                                                                                                                                <% for
                                                                                                                                                    (Exercise
                                                                                                                                                    candidate
                                                                                                                                                    :
                                                                                                                                                    swapCandidates)
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <option
                                                                                                                                                        value="<%= candidate.getId() %>">
                                                                                                                                                        <%= candidate.getName()
                                                                                                                                                            %>
                                                                                                                                                    </option>
                                                                                                                                                    <% }
                                                                                                                                                        %>
                                                                                                                                            </select>
                                                                                                                                        </div>
                                                                                                                                        <% }
                                                                                                                                            %>
                                                                                                                        </div>
                                                                                                                        <% }
                                                                                                                            %>
                                                                                                                            <div
                                                                                                                                class="score-strip">
                                                                                                                                <div
                                                                                                                                    class="score-pill">
                                                                                                                                    Letzter
                                                                                                                                    Score:
                                                                                                                                    <strong>
                                                                                                                                        <%= lastScoreText
                                                                                                                                            %>
                                                                                                                                    </strong>
                                                                                                                                </div>
                                                                                                                                <div
                                                                                                                                    class="score-pill">
                                                                                                                                    Aktueller
                                                                                                                                    Score:
                                                                                                                                    <strong
                                                                                                                                        id="currentScore_<%= item.getId() %>">
                                                                                                                                        <%= currentScoreText
                                                                                                                                            %>
                                                                                                                                    </strong>
                                                                                                                                </div>
                                                                                                                            </div>
                                                                                                                            <table
                                                                                                                                class="plan-table track-table">
                                                                                                                                <thead>
                                                                                                                                    <tr>
                                                                                                                                        <th>Satz
                                                                                                                                        </th>
                                                                                                                                        <th>Reps
                                                                                                                                        </th>
                                                                                                                                        <th>Gewicht
                                                                                                                                            (kg)
                                                                                                                                        </th>
                                                                                                                                        <th>Notiz
                                                                                                                                        </th>
                                                                                                                                    </tr>
                                                                                                                                </thead>
                                                                                                                                <tbody>
                                                                                                                                    <% for
                                                                                                                                        (int
                                                                                                                                        setNo=1;
                                                                                                                                        setNo
                                                                                                                                        <=item.getPlannedSetsSnapshot();
                                                                                                                                        setNo++)
                                                                                                                                        {
                                                                                                                                        String
                                                                                                                                        rowKey=item.getId()
                                                                                                                                        + "_"
                                                                                                                                        +
                                                                                                                                        setNo;
                                                                                                                                        WorkoutLog
                                                                                                                                        existing=existingBySet.get(setNo);
                                                                                                                                        String
                                                                                                                                        repsValue=existing==null
                                                                                                                                        ? ""
                                                                                                                                        :
                                                                                                                                        String.valueOf(existing.getReps());
                                                                                                                                        String
                                                                                                                                        weightValue=existing==null
                                                                                                                                        ? ""
                                                                                                                                        :
                                                                                                                                        String.valueOf(existing.getWeight());
                                                                                                                                        String
                                                                                                                                        noteValue=existing==null
                                                                                                                                        ||
                                                                                                                                        existing.getNote()==null
                                                                                                                                        ? ""
                                                                                                                                        :
                                                                                                                                        existing.getNote();
                                                                                                                                        %>
                                                                                                                                        <tr>
                                                                                                                                            <td>
                                                                                                                                                <%= setNo
                                                                                                                                                    %>
                                                                                                                                                    <% if
                                                                                                                                                        (activeSession)
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <input
                                                                                                                                                            type="hidden"
                                                                                                                                                            name="rowKey"
                                                                                                                                                            value="<%= rowKey %>">
                                                                                                                                                        <% }
                                                                                                                                                            %>
                                                                                                                                            </td>
                                                                                                                                            <td>
                                                                                                                                                <% if
                                                                                                                                                    (activeSession)
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <input
                                                                                                                                                        name="reps_<%= rowKey %>"
                                                                                                                                                        type="number"
                                                                                                                                                        min="1"
                                                                                                                                                        value="<%= repsValue %>"
                                                                                                                                                        data-score-group="<%= item.getId() %>"
                                                                                                                                                        data-score-kind="reps"
                                                                                                                                                        data-row-key="<%= rowKey %>">
                                                                                                                                                    <% } else
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <%= repsValue
                                                                                                                                                            %>
                                                                                                                                                            <% }
                                                                                                                                                                %>
                                                                                                                                            </td>
                                                                                                                                            <td>
                                                                                                                                                <% if
                                                                                                                                                    (activeSession)
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <input
                                                                                                                                                        name="weight_<%= rowKey %>"
                                                                                                                                                        type="number"
                                                                                                                                                        step="0.01"
                                                                                                                                                        min="0"
                                                                                                                                                        value="<%= weightValue %>"
                                                                                                                                                        data-score-group="<%= item.getId() %>"
                                                                                                                                                        data-score-kind="weight"
                                                                                                                                                        data-row-key="<%= rowKey %>">
                                                                                                                                                    <% } else
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <%= weightValue
                                                                                                                                                            %>
                                                                                                                                                            <% }
                                                                                                                                                                %>
                                                                                                                                            </td>
                                                                                                                                            <td>
                                                                                                                                                <% if
                                                                                                                                                    (activeSession)
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <textarea
                                                                                                                                                        class="textarea-fixed"
                                                                                                                                                        name="note_<%= rowKey %>"><%= noteValue %></textarea>
                                                                                                                                                    <% } else
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <%= noteValue
                                                                                                                                                            %>
                                                                                                                                                            <% }
                                                                                                                                                                %>
                                                                                                                                            </td>
                                                                                                                                        </tr>
                                                                                                                                        <% }
                                                                                                                                            %>
                                                                                                                                </tbody>
                                                                                                                            </table>
                                                                                                                </div>
                                                                                                                <% } %>

                                                                                                                    <% if
                                                                                                                        (activeSession)
                                                                                                                        {
                                                                                                                        %>
                                                                                                                        <p
                                                                                                                            class="track-finish-actions">
                                                                                                                            <button
                                                                                                                                class="btn btn-primary"
                                                                                                                                type="submit">Training
                                                                                                                                beenden
                                                                                                                                &amp;
                                                                                                                                speichern</button>
                                                                                                                        </p>
                                                                                        </form>

                                                                                        <form class="discard-form"
                                                                                            method="post"
                                                                                            action="<%= request.getContextPath() %>/session/discard"
                                                                                            onsubmit="return confirm('Willst du dieses Training wirklich verwerfen? Nicht gespeicherte Eingaben gehen verloren.');">
                                                                                            <input type="hidden"
                                                                                                name="sessionId"
                                                                                                value="<%= sessionData.getId() %>">
                                                                                            <button
                                                                                                class="btn btn-danger"
                                                                                                type="submit">Training
                                                                                                verwerfen</button>
                                                                                        </form>
                                                                                        <% } %>
                                                                                            <% } %>
                                                                                                <% } %>

                                                                                                    <% if
                                                                                                        (activeSession)
                                                                                                        { %>
                                                                                                        <script>
                                                                                                            (function () {
                                                                                                                var sessionId = <%= sessionData == null ? "null" : String.valueOf(sessionData.getId()) %>;
                                                                                                                var draftStorageKey = sessionId === null ? null : ("sessionTrackDraft_" + sessionId);
                                                                                                                var startEpochMillis = <%= startedAtEpochMillis == null ? "null" : startedAtEpochMillis.toString() %>;
                                                                                                                var allowUnload = false;
                                                                                                                var warningText = "Training läuft noch. Bitte zuerst 'Training beenden & speichern' oder 'Training verwerfen'.";

                                                                                                                function formatDuration(totalSeconds) {
                                                                                                                    var hours = Math.floor(totalSeconds / 3600);
                                                                                                                    var minutes = Math.floor((totalSeconds % 3600) / 60);
                                                                                                                    var seconds = totalSeconds % 60;
                                                                                                                    return String(hours).padStart(2, "0") + ":" + String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
                                                                                                                }

                                                                                                                function updateTimer() {
                                                                                                                    if (startEpochMillis === null) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    var elapsedSeconds = Math.max(0, Math.floor((Date.now() - startEpochMillis) / 1000));
                                                                                                                    var timerElement = document.getElementById("sessionDuration");
                                                                                                                    if (timerElement) {
                                                                                                                        timerElement.textContent = formatDuration(elapsedSeconds);
                                                                                                                    }
                                                                                                                }

                                                                                                                function parseScoreNumber(value) {
                                                                                                                    if (!value) {
                                                                                                                        return 0;
                                                                                                                    }
                                                                                                                    var normalized = String(value).replace(",", ".").trim();
                                                                                                                    if (normalized.length === 0) {
                                                                                                                        return 0;
                                                                                                                    }
                                                                                                                    var parsed = parseFloat(normalized);
                                                                                                                    if (isNaN(parsed) || parsed < 0) {
                                                                                                                        return 0;
                                                                                                                    }
                                                                                                                    return parsed;
                                                                                                                }

                                                                                                                function formatScore(value) {
                                                                                                                    var fixed = value.toFixed(2);
                                                                                                                    return fixed.replace(/\.00$/, "").replace(/(\.\d)0$/, "$1");
                                                                                                                }

                                                                                                                function collectDraftInputs() {
                                                                                                                    var draft = {};
                                                                                                                    document.querySelectorAll("input[name^='reps_'], input[name^='weight_'], textarea[name^='note_']").forEach(function (field) {
                                                                                                                        draft[field.name] = field.value;
                                                                                                                    });
                                                                                                                    return draft;
                                                                                                                }

                                                                                                                function restoreDraftInputs() {
                                                                                                                    if (!draftStorageKey) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    try {
                                                                                                                        var raw = window.sessionStorage.getItem(draftStorageKey);
                                                                                                                        if (!raw) {
                                                                                                                            return;
                                                                                                                        }
                                                                                                                        var draft = JSON.parse(raw);
                                                                                                                        Object.keys(draft).forEach(function (fieldName) {
                                                                                                                            var field = document.querySelector("[name='" + fieldName + "']");
                                                                                                                            if (field) {
                                                                                                                                field.value = draft[fieldName];
                                                                                                                            }
                                                                                                                        });
                                                                                                                    } catch (error) {
                                                                                                                        window.sessionStorage.removeItem(draftStorageKey);
                                                                                                                    }
                                                                                                                }

                                                                                                                function persistDraftInputs() {
                                                                                                                    if (!draftStorageKey) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    try {
                                                                                                                        var draft = collectDraftInputs();
                                                                                                                        window.sessionStorage.setItem(draftStorageKey, JSON.stringify(draft));
                                                                                                                    } catch (error) {
                                                                                                                        // Ignore sessionStorage issues and continue.
                                                                                                                    }
                                                                                                                }

                                                                                                                function clearDraftInputs() {
                                                                                                                    if (!draftStorageKey) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    window.sessionStorage.removeItem(draftStorageKey);
                                                                                                                }

                                                                                                                function updateCurrentScoreForGroup(groupId) {
                                                                                                                    var repsInputs = document.querySelectorAll(
                                                                                                                        "input[data-score-group='" + groupId + "'][data-score-kind='reps']"
                                                                                                                    );
                                                                                                                    var score = 0;
                                                                                                                    repsInputs.forEach(function (repsInput) {
                                                                                                                        var rowKey = repsInput.getAttribute("data-row-key");
                                                                                                                        var weightInput = document.querySelector(
                                                                                                                            "input[data-score-group='" + groupId + "'][data-score-kind='weight'][data-row-key='" + rowKey + "']"
                                                                                                                        );
                                                                                                                        var reps = parseScoreNumber(repsInput.value);
                                                                                                                        var weight = parseScoreNumber(weightInput ? weightInput.value : "");
                                                                                                                        score += reps * weight;
                                                                                                                    });
                                                                                                                    var target = document.getElementById("currentScore_" + groupId);
                                                                                                                    if (target) {
                                                                                                                        target.textContent = formatScore(score);
                                                                                                                    }
                                                                                                                }

                                                                                                                restoreDraftInputs();
                                                                                                                updateTimer();
                                                                                                                setInterval(updateTimer, 1000);

                                                                                                                var scoreInputs = document.querySelectorAll("input[data-score-group][data-score-kind]");
                                                                                                                var scoreGroups = new Set();
                                                                                                                scoreInputs.forEach(function (input) {
                                                                                                                    var groupId = input.getAttribute("data-score-group");
                                                                                                                    if (groupId) {
                                                                                                                        scoreGroups.add(groupId);
                                                                                                                        input.addEventListener("input", function () {
                                                                                                                            updateCurrentScoreForGroup(groupId);
                                                                                                                        });
                                                                                                                    }
                                                                                                                });
                                                                                                                scoreGroups.forEach(function (groupId) {
                                                                                                                    updateCurrentScoreForGroup(groupId);
                                                                                                                });

                                                                                                                var finishForm = document.querySelector("form[action$='/session/finish']");
                                                                                                                if (finishForm) {
                                                                                                                    finishForm.addEventListener("submit", function () {
                                                                                                                        clearDraftInputs();
                                                                                                                        allowUnload = true;
                                                                                                                    });
                                                                                                                }
                                                                                                                var discardForm = document.querySelector("form[action$='/session/discard']");
                                                                                                                if (discardForm) {
                                                                                                                    discardForm.addEventListener("submit", function () {
                                                                                                                        clearDraftInputs();
                                                                                                                        allowUnload = true;
                                                                                                                    });
                                                                                                                }
                                                                                                                var swapSubmitForm = document.getElementById("swapSubmitForm");
                                                                                                                var swapSessionExerciseId = document.getElementById("swapSessionExerciseId");
                                                                                                                var swapReplacementExerciseId = document.getElementById("swapReplacementExerciseId");
                                                                                                                document.querySelectorAll(".track-swap-select").forEach(function (swapSelect) {
                                                                                                                    swapSelect.addEventListener("change", function () {
                                                                                                                        var sessionExerciseId = swapSelect.getAttribute("data-session-exercise-id");
                                                                                                                        if (!swapSubmitForm || !swapSessionExerciseId || !swapReplacementExerciseId || !swapSelect.value) {
                                                                                                                            return;
                                                                                                                        }
                                                                                                                        persistDraftInputs();
                                                                                                                        swapSessionExerciseId.value = sessionExerciseId;
                                                                                                                        swapReplacementExerciseId.value = swapSelect.value;
                                                                                                                        allowUnload = true;
                                                                                                                        swapSubmitForm.submit();
                                                                                                                    });
                                                                                                                });

                                                                                                                window.addEventListener("beforeunload", function (event) {
                                                                                                                    if (allowUnload) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    event.preventDefault();
                                                                                                                    event.returnValue = "";
                                                                                                                });

                                                                                                                window.history.pushState(null, "", window.location.href);
                                                                                                                window.addEventListener("popstate", function () {
                                                                                                                    if (allowUnload) {
                                                                                                                        return;
                                                                                                                    }
                                                                                                                    alert(warningText);
                                                                                                                    window.history.pushState(null, "", window.location.href);
                                                                                                                });

                                                                                                                document.querySelectorAll("a").forEach(function (link) {
                                                                                                                    link.addEventListener("click", function (event) {
                                                                                                                        if (allowUnload) {
                                                                                                                            return;
                                                                                                                        }
                                                                                                                        alert(warningText);
                                                                                                                        event.preventDefault();
                                                                                                                    });
                                                                                                                });

                                                                                                                // Verhindert das Absenden des Formulars durch die Enter-Taste in Inputs
                                                                                                                document.querySelectorAll("input, textarea").forEach(function (el) {
                                                                                                                    el.addEventListener("keydown", function (event) {
                                                                                                                        if (event.key === "Enter") {
                                                                                                                            event.preventDefault();
                                                                                                                            return false;
                                                                                                                        }
                                                                                                                    });
                                                                                                                });
                                                                                                            })();
                                                                                                        </script>
                                                                                                        <% } %>

                                                    </div>
                                                    <script
                                                        src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
                                </body>

                                </html>