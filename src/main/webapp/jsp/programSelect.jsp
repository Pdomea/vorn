<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="app.beans.ProgramSelectBean" %>
<%@ page import="app.model.Plan" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Plan auswählen</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="admin-programs-page">
<jsp:useBean id="programSelectBean" class="app.beans.ProgramSelectBean" scope="request" />
<%
    List<Plan> activePlans = programSelectBean.getActivePlans();
    Long currentActivePlanId = programSelectBean.getCurrentActivePlanId();
    String fallbackImage = request.getContextPath() + "/img/hero/landing-poster.svg";
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
            <h1>Plan auswählen</h1>
            <p class="muted">Wähle deinen aktiven Plan.</p>
        </div>
        <div class="top-actions">
            <a class="btn btn-ghost" href="<%= request.getContextPath() %>/home">Home</a>
        </div>
    </div>

    <% if (programSelectBean.getInfo() != null && !programSelectBean.getInfo().isBlank()) { %>
    <div class="info">${programSelectBean.info}</div>
    <% } %>
    <% if (programSelectBean.getError() != null && !programSelectBean.getError().isBlank()) { %>
    <div class="error">${programSelectBean.error}</div>
    <% } %>

    <section class="admin-console-stage">
        <h2 class="admin-console-stage-title">Programme auswählen</h2>
        <p class="admin-console-stage-subtitle">Wähle den Plan, den du als Nächstes trainierst.</p>
        <p class="program-select-note">Planwechsel löscht alle Ergebnisse.</p>

        <% if (!programSelectBean.hasActivePlans()) { %>
        <div class="card admin-console-card">
            <p class="muted">Es gibt aktuell keine aktiven Programme.</p>
        </div>
        <% } else { %>
        <div class="card admin-console-card">
            <div class="program-select-carousel" data-program-carousel>
                <div class="program-select-carousel-viewport" data-carousel-viewport>
                    <div class="admin-console-grid program-select-carousel-track" data-carousel-track>
                <% for (Plan plan : activePlans) {
                    String planImagePath = fallbackImage;
                    if (plan.getHeroImagePath() != null && !plan.getHeroImagePath().isBlank()) {
                        String rawPath = plan.getHeroImagePath().trim();
                        if (!rawPath.startsWith("/")) {
                            rawPath = "/" + rawPath;
                        }
                        planImagePath = request.getContextPath() + rawPath;
                    }
                    boolean isActive = currentActivePlanId != null && currentActivePlanId == plan.getId();
                %>
                <article class="admin-console-tile">
                    <img class="admin-console-tile-image"
                         src="<%= planImagePath %>"
                         onerror="this.onerror=null;this.src='<%= fallbackImage %>';"
                         alt="<%= plan.getName() %>">
                    <span class="admin-console-tile-overlay"></span>
                    <span class="admin-console-tile-content">
                        <span class="admin-console-tile-title"><%= plan.getName() %></span>
                        <span class="admin-console-tile-text">
                            <%= plan.getDescription() == null || plan.getDescription().isBlank()
                                    ? "Ohne Beschreibung."
                                    : plan.getDescription() %>
                        </span>
                        <% if (isActive) { %>
                        <span class="status-badge status-done">Aktiv</span>
                        <% } %>
                        <% if (isActive) { %>
                        <button class="btn btn-secondary program-select-btn" type="button" disabled>Aktiv</button>
                        <% } else { %>
                        <form class="inline-form program-select-card-action" method="post" action="<%= request.getContextPath() %>/program/select">
                            <input type="hidden" name="planId" value="<%= plan.getId() %>">
                            <button class="btn btn-secondary program-select-btn" type="submit">Plan wählen</button>
                        </form>
                        <% } %>
                    </span>
                </article>
                <% } %>
                    </div>
                </div>
                <div class="program-select-carousel-controls">
                    <button class="program-select-carousel-nav"
                            type="button"
                            data-carousel-prev
                            aria-label="Vorherige Pläne">
                        &#8249;
                    </button>
                    <button class="program-select-carousel-nav"
                            type="button"
                            data-carousel-next
                            aria-label="Nächste Pläne">
                        &#8250;
                    </button>
                </div>
            </div>
        </div>
        <% } %>
    </section>
</div>
<script>
    (function () {
        var carousels = document.querySelectorAll("[data-program-carousel]");
        carousels.forEach(function (carousel) {
            var viewport = carousel.querySelector("[data-carousel-viewport]");
            var track = carousel.querySelector("[data-carousel-track]");
            var prev = carousel.querySelector("[data-carousel-prev]");
            var next = carousel.querySelector("[data-carousel-next]");
            if (!viewport || !track || !prev || !next) {
                return;
            }

            function getStep() {
                var firstCard = track.querySelector(".admin-console-tile");
                if (!firstCard) {
                    return viewport.clientWidth;
                }
                var trackStyles = window.getComputedStyle(track);
                var gap = parseFloat(trackStyles.columnGap || trackStyles.gap || "0");
                if (isNaN(gap)) {
                    gap = 0;
                }
                return firstCard.getBoundingClientRect().width + gap;
            }

            function getMaxScroll() {
                return Math.max(0, viewport.scrollWidth - viewport.clientWidth);
            }

            function updateNavState() {
                var maxScroll = getMaxScroll();
                var left = viewport.scrollLeft;
                var canScroll = maxScroll > 2;
                prev.disabled = !canScroll || left <= 2;
                next.disabled = !canScroll || left >= maxScroll - 2;
            }

            prev.addEventListener("click", function () {
                viewport.scrollBy({ left: -getStep(), behavior: "smooth" });
            });

            next.addEventListener("click", function () {
                viewport.scrollBy({ left: getStep(), behavior: "smooth" });
            });

            viewport.addEventListener("scroll", updateNavState, { passive: true });
            window.addEventListener("resize", updateNavState);
            updateNavState();
        });
    })();
</script>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
