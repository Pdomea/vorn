VORN - Trainingsplanung und Workout Tracking

Tech-Stack
Backend: Java 17, Servlet API (Jakarta EE 10)
DB: PostgreSQL via JDBC
Frontend: JSP, Vanilla CSS/JS
Server: Apache Tomcat 10.1+

Setup in Eclipse
1. Projekt als "Dynamic Web Project" importieren und Tomcat 10+ zuweisen.
2. PostgreSQL-Daten in src/main/webapp/WEB-INF/web.xml eintragen (db.url, db.user, db.password).
3. Server starten.

Erster Login (wird beim ersten Start automatisch in DB angelegt):
Benutzer: admin@vorn.local
Passwort: Admin123!
