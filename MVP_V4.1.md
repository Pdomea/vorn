# MVP V4.1 (aktualisiert, visionskonform und prüfungstauglich)

## 1. Rollen

| Rolle | Beschreibung |
|-------|-------------|
| **ADMIN** | Verwaltet Trainings, Übungen, Zuordnung, Veröffentlichung, Pläne und Wochenstruktur. |
| **USER** | Registriert sich, loggt sich ein, sieht Dashboard + Plan/Woche, startet Session aus Wochenansicht, trackt, beendet oder verwirft Session, sieht vergangene Ergebnisse direkt in derselben Wochenansicht. |

## 2. Muss-Funktionen (Demo-relevant)

### Auth
- Register / Login / Logout

### User
- Dashboard (Home)
- Plan-/Wochenansicht
- Training aus Woche starten
- Track-Form mit geplanten Satzfeldern
- Beenden & speichern
- Verwerfen
- Integrierte Verlaufssicht pro Training in derselben Wochenansicht

### Admin
- Trainings CRUD (ohne Hard Delete)
- Übungen CRUD (ohne Hard Delete)
- Zuordnung + Planwerte + Reihenfolge
- Publish / Hide
- Plan anlegen
- Woche 1 automatisch, weitere Wochen fortlaufend hinzufügen
- Trainings pro Woche variabel zuordnen
- Plan archivieren
- Woche / Zuordnung entfernen

## 3. Fachregeln

- Genau **eine aktive Session pro User**.
- Nur PUBLISHED-Trainings dürfen gestartet werden.
- Keine Hard Deletes bei genutzten Daten; stattdessen HIDDEN/ARCHIVED.
- Historie bleibt stabil durch Snapshot (`session_exercises`).
- User darf nur eigene Sessions/Logs sehen und bearbeiten.
- Logs nur bei `workout_sessions.status = ACTIVE`; bei FINISHED read-only.
- Session-Form wird im laufenden Training ausgefüllt; Speicherung gesammelt bei Beenden & speichern.
- Leere Satzfelder sind erlaubt.
- Verwerfen speichert nicht und entfernt aktive Session.
- Admin-Rechte werden in Filter **und** Service geprüft.
- Training darf in mehreren Plänen/Wochen wiederverwendet werden.
- ARCHIVED-Pläne sind nur lesbar.
- Gewählter aktiver Plan pro User wird persistent gespeichert.

---

# Datenmodell V4.1 (angepasst auf Wochenvision + Dashboard)

## 1. Tabellen

- `users`, `trainings`, `exercises`, `training_exercises`, `workout_sessions`, `session_exercises`, `workout_logs`
- `plans`, `plan_weeks`, `plan_week_trainings`

## 2. Wichtige zusätzliche Felder

| Feld | Beschreibung |
|------|-------------|
| `users.active_plan_id` | Persistenter aktiver Plan pro User |
| `workout_sessions.plan_id` | Session-Kontext aus Wochenstart |
| `workout_sessions.plan_week_id` | Session-Kontext aus Wochenstart |

## 3. Status-Felder

| Tabelle | Feld | Werte |
|---------|------|-------|
| `trainings` | `status` | `DRAFT` \| `PUBLISHED` \| `HIDDEN` |
| `exercises` | `status` | `ACTIVE` \| `ARCHIVED` |
| `users` | `role` | `ADMIN` \| `USER` |
| `workout_sessions` | `status` | `ACTIVE` \| `FINISHED` |

## 4. Wochenstruktur

| Tabelle | Beschreibung |
|---------|-------------|
| `plans` | Plan-Metadaten |
| `plan_weeks` | Wochen je Plan (`week_no` fortlaufend, Woche 1 default) |
| `plan_week_trainings` | Variable Trainings pro Woche inkl. Reihenfolge |

## 5. Historien-Stabilität

- `session_exercises`: `id`, `session_id`, `exercise_id`, `exercise_name_snapshot`, `planned_sets_snapshot`, `planned_reps_snapshot`, `sort_order`
- `workout_logs` referenziert `session_exercise_id` (nicht `training_exercise_id`)

## 6. Constraints / Indizes

| Constraint | Typ |
|-----------|-----|
| `users.email` | UNIQUE |
| `training_exercises (training_id, sort_order)` | UNIQUE |
| `training_exercises (training_id, exercise_id)` | UNIQUE (optional) |
| `plan_weeks (plan_id, week_no)` | UNIQUE |
| `plan_week_trainings (plan_week_id, sort_order)` | UNIQUE |
| `workout_logs (session_id, session_exercise_id, set_no)` | UNIQUE |
| FK-Indizes auf allen FK-Spalten | INDEX |
| `workout_sessions(user_id) WHERE status='ACTIVE'` | PARTIAL UNIQUE INDEX (max. 1 aktive Session/User) |
| Performance-Indizes für integrierten Verlauf | Optional (`008_history_indexes.sql`) |

---

# Endpoint-Set V4.1 (konkret, Ist-Stand + Zielbild)

## 1. Auth

| Methode | Endpoint |
|---------|----------|
| GET | `/login` |
| POST | `/login` |
| GET | `/register` |
| POST | `/register` |
| POST | `/logout` |

## 2. User (primärer Pfad)

| Methode | Endpoint | Beschreibung |
|---------|----------|-------------|
| GET | `/home` | Dashboard |
| GET | `/program` | Plan-/Wochenansicht |
| POST | `/program/select` | Aktiven Plan speichern |
| POST | `/program/session/start` | Session aus Woche starten |
| GET | `/session/track?id=...` | Track-Form |
| POST | `/session/finish` | Bulk-Save + Finish |
| POST | `/session/discard` | Session verwerfen |

## 3. User (Legacy, nicht primär)

| Methode | Endpoint |
|---------|----------|
| GET | `/trainings` |
| GET | `/trainings/detail?id=...` |
| POST | `/session/start` |
| GET | `/session/history` |

## 4. Admin

| Methode | Endpoint |
|---------|----------|
| GET | `/admin/trainings` |
| POST | `/admin/training/save` |
| POST | `/admin/training/publish` |
| POST | `/admin/training/hide` |
| GET | `/admin/exercises` |
| POST | `/admin/exercise/save` |
| POST | `/admin/exercise/archive` |
| POST | `/admin/training/exercise/add` |
| POST | `/admin/training/exercise/update` |
| GET | `/admin/programs` |
| POST | `/admin/program/create` |
| POST | `/admin/program/archive` |
| POST | `/admin/program/week/add` |
| POST | `/admin/program/week/remove` |
| POST | `/admin/program/week/training/add` |
| POST | `/admin/program/week/training/remove` |

---

# Priorisierte Umsetzungsreihenfolge (Demo-first, visionskonform)

| # | Schritt |
|---|---------|
| 1 | Setup + DB + Seed-Admin |
| 2 | Register/Login/Logout + Session + Filter |
| 3 | User Trainingsliste/Detail (legacy Basis) |
| 4 | Admin Trainings/Übungen/Zuordnung + Publish/Hide |
| 5 | Session Start mit Snapshot in `session_exercises` |
| 6 | Tracking Kern: Form ausfüllen, Bulk-finish, discard |
| 7 | Tracking UX: Timer + Guard + Verlassen-Warnung |
| 8 | Plan-/Wochen-Datenmodell (`plans`, `plan_weeks`, `plan_week_trainings`) |
| 9 | Admin UI Plan/Woche (Woche 1 default, Wochen per +, variable Trainings pro Woche) |
| 10 | User Wochenansicht (`/program`) + Start aus Woche + Session-Kontext (`plan_id`, `plan_week_id`) |
| 11 | Persistenter aktiver Plan pro User (`users.active_plan_id`, `/program/select`) |
| 12 | Integrierte Verlaufssicht in Home + Wochenansicht (statt separatem History-Tab) |
| 13 | Dashboard-Polish: KPI-Karten (Volumen, Workouts, Zeit), Planstatus-CTA, Plan-Switch UX |
| 14 | Score-Logik (Input-Wert) + Anzeige letzter Wert/Zielwert |
| 15 | Performance-Paket (Connection Pool + Query-Optimierung + optionale Indizes) |
| 16 | Look & Feel, serverseitige Validierung, Fehlerseiten |
| 17 | Doku + Installationsanleitung + Präsentationsablauf |
