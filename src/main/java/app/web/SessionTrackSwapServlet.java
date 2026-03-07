package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.service.TrackingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/session/track/swap")
public class SessionTrackSwapServlet extends HttpServlet {
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(), new WorkoutLogDao());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String sessionId = req.getParameter("sessionId");
        String sessionExerciseId = req.getParameter("sessionExerciseId");
        String replacementExerciseId = req.getParameter("replacementExerciseId");

        try {
            TrackingService.SwapResult swapResult =
                    trackingService.swapSessionExercise(currentUser, sessionId, sessionExerciseId, replacementExerciseId);
            setFlashInfo(req, "Übung getauscht: " + swapResult.getSourceExerciseName() + " -> " + swapResult.getReplacementExerciseName());
            resp.sendRedirect(buildTrackUrl(req, sessionId, swapResult.getSessionExerciseId()));
            return;
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
            return;
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
        } catch (SQLException ex) {
            setFlashError(req, "Übungstausch konnte nicht durchgeführt werden (DB-Fehler).");
        }

        resp.sendRedirect(buildTrackUrl(req, sessionId, null));
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        if (value instanceof User user) {
            return user;
        }
        return null;
    }

    private String buildTrackUrl(HttpServletRequest req, String sessionId, Long sessionExerciseId) {
        if (sessionId == null || sessionId.isBlank()) {
            return req.getContextPath() + "/home";
        }
        String encodedSessionId = URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        String baseUrl = req.getContextPath() + "/session/track?id=" + encodedSessionId;
        if (sessionExerciseId == null || sessionExerciseId <= 0) {
            return baseUrl;
        }
        return baseUrl + "#exercise-" + sessionExerciseId;
    }

    private void setFlashInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.info", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.error", message);
    }
}
