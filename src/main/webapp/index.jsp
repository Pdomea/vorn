<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VORN - Core Performance</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
</head>
<body class="landing-page">
<div class="landing-shell">
    <header class="landing-header">
        <img class="landing-brand-logo"
             src="<%= request.getContextPath() %>/img/brand/logo-vorn-home.svg"
             alt="vorn">
    </header>

    <section class="landing-hero">
        <div class="landing-media">
            <img class="landing-poster"
                 src="<%= request.getContextPath() %>/img/hero/landing-poster.svg"
                 alt="Training Hero">
            <video class="landing-video"
                   autoplay
                   muted
                   loop
                   playsinline
                   preload="metadata"
                   poster="<%= request.getContextPath() %>/img/hero/landing-poster.svg">
                <source src="<%= request.getContextPath() %>/img/video/landing-hero.mp4" type="video/mp4">
            </video>
            <div class="landing-overlay"></div>
            <div class="landing-content">
                <h1>Andere hoffen auf Ergebnisse.</h1>
                <p>Du erzwingst sie. Sei vorn.</p>
                <div class="landing-actions">
                    <a class="landing-cta-outline" href="<%= request.getContextPath() %>/login">Jetzt vorn starten</a>
                </div>
            </div>
        </div>
    </section>
</div>
    <script src="<%= request.getContextPath() %>/js/accent-randomizer.js"></script>
</body>
</html>
