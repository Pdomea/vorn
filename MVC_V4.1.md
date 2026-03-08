# MVC V4.1 (sauber getrennt)

## 1. app.model

Alle Entitäten als einfache Java-Klassen (POJOs):

| Klasse | Beschreibung |
|--------|-------------|
| `User` | Benutzer mit Rolle und aktivem Plan |
| `Training` | Trainingseinheit mit Status |
| `Exercise` | Übung mit Status |
| `TrainingExercise` | Zuordnung Übung → Training mit Planwerten + Reihenfolge |
| `WorkoutSession` | Aktive/abgeschlossene Trainings-Session |
| `SessionExercise` | Snapshot der Übungen zum Zeitpunkt des Session-Starts |
| `WorkoutLog` | Einzelner Satz-Eintrag (Gewicht, Wiederholungen) |
| `Plan` | Trainingsplan-Metadaten |
| `PlanWeek` | Woche innerhalb eines Plans |
| `PlanWeekTraining` | Zuordnung Training → Woche mit Reihenfolge |

## 2. app.dao

Data Access Layer — je eine DAO-Klasse pro Tabelle:

| Klasse | Tabelle |
|--------|---------|
| `ConnectionFactory` | DB-Verbindungsverwaltung |
| `UserDao` | `users` |
| `TrainingDao` | `trainings` |
| `ExerciseDao` | `exercises` |
| `TrainingExerciseDao` | `training_exercises` |
| `WorkoutSessionDao` | `workout_sessions` |
| `SessionExerciseDao` | `session_exercises` |
| `WorkoutLogDao` | `workout_logs` |
| `PlanDao` | `plans` |
| `PlanWeekDao` | `plan_weeks` |
| `PlanWeekTrainingDao` | `plan_week_trainings` |

## 3. app.service

Geschäftslogik-Schicht — orchestriert DAOs und setzt Fachregeln um:

| Service | Verantwortung |
|---------|--------------|
| `AuthService` | Register, Login, Passwort-Hashing, Session-Verwaltung |
| `TrainingService` | Trainings/Übungen CRUD, Zuordnung, Publish/Hide |
| `TrackingService` | Session Start (mit Snapshot), Tracking, Bulk-Finish, Discard |
| `ProgramService` | Plan/Woche/Zuordnung, User-Plan-Kontext, integrierter Verlauf |

## 4. app.web (Servlet-Controller)

HTTP-Schicht — nimmt Requests entgegen, ruft Services, leitet zu Views weiter:

| Controller | Endpoints |
|-----------|-----------|
| Auth-Controller | `/login`, `/register`, `/logout` |
| Training-Controller | `/trainings`, `/trainings/detail` |
| Session-Controller | `/session/track`, `/session/start`, `/session/finish`, `/session/discard`, `/session/history` |
| Admin-Controller | `/admin/trainings`, `/admin/exercises`, `/admin/training/*`, `/admin/exercise/*` |
| Program-Controller (User) | `/home`, `/program`, `/program/select`, `/program/session/start` |
| Program-Controller (Admin) | `/admin/programs`, `/admin/program/*` |

## 5. app.web.filter

Servlet-Filter — sichern Zugriff ab, bevor Requests den Controller erreichen:

| Filter | Aufgabe |
|--------|---------|
| `AuthFilter` | Whitelist: `/login`, `/register`, statische Ressourcen. Alle anderen Pfade erfordern Login. |
| `AdminFilter` | `/admin/*` nur für Benutzer mit Rolle `ADMIN`. |
| `ActiveSessionGuardFilter` | Aktive Session absichern (Redirect wenn Session läuft). |

## 6. Views (JSP)

### Primärer UX-Pfad

| View | Beschreibung |
|------|-------------|
| `login.jsp` | Login-Formular |
| `register.jsp` | Registrierungs-Formular |
| `home.jsp` | Dashboard mit KPI-Karten und Planstatus |
| `program.jsp` | Plan-/Wochenansicht mit integrierter Verlaufssicht |
| `sessionTrack.jsp` | Track-Form für aktive Session |
| `jsp/admin/*.jsp` | Admin-Bereich (Trainings, Übungen, Programme) |

### Legacy (technisch vorhanden, nicht primärer UX-Pfad)

| View | Beschreibung |
|------|-------------|
| `trainings.jsp` | Trainingsliste |
| `trainingDetail.jsp` | Training-Detailansicht |
| `sessionHistory.jsp` | Separate Verlaufsansicht |

---

## Architektur-Übersicht

```
┌──────────────────────────────────────────────────┐
│                    Browser/JSP                    │
├──────────────────────────────────────────────────┤
│              app.web.filter                       │
│   AuthFilter → AdminFilter → SessionGuardFilter  │
├──────────────────────────────────────────────────┤
│              app.web (Servlet-Controller)         │
│   Auth │ Training │ Session │ Admin │ Program     │
├──────────────────────────────────────────────────┤
│              app.service                          │
│   AuthService │ TrainingService │ TrackingService │
│                  ProgramService                   │
├──────────────────────────────────────────────────┤
│              app.dao                              │
│   ConnectionFactory + 10 DAO-Klassen              │
├──────────────────────────────────────────────────┤
│              app.model (POJOs)                    │
│   10 Entitäten                                    │
├──────────────────────────────────────────────────┤
│              PostgreSQL                           │
└──────────────────────────────────────────────────┘
```
