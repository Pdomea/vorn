<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
    <!DOCTYPE html>
    <html lang="de">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>500 - Serverfehler</title>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
        <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/img/brand/favicon.svg?v=20260309">
        <style>
            .error-page {
                display: flex;
                align-items: center;
                justify-content: center;
                min-height: 100vh;
                text-align: center;
            }

            .error-code {
                font-size: 8rem;
                font-weight: 800;
                letter-spacing: -4px;
                line-height: 1;
                color: var(--text-primary);
            }

            .error-message {
                margin-top: 1rem;
                font-size: 1.1rem;
                color: var(--text-secondary);
            }

            .error-home {
                margin-top: 2rem;
            }
        </style>
    </head>

    <body class="error-page">
        <div>
            <div class="error-code">500</div>
            <p class="error-message">Der Server braucht eine Pause - selbst er hat mal einen schlechten Tag.</p>
            <div class="error-home">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/home">Zurück zur Startseite</a>
            </div>
        </div>
    </body>

    </html>