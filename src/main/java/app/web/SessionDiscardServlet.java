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

@WebServlet("/session/discard")
public class SessionDiscardServlet extends HttpServlet {
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(), new WorkoutLogDao());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String sessionId = req.getParameter("sessionId");

        try {
            trackingService.discardSession(currentUser, sessionId);
            setFlashInfo(req, "Training wurde verworfen.");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildTrackUrl(req, sessionId));
        } catch (SQLException ex) {
            setFlashError(req, "Training konnte nicht verworfen werden (DB-Fehler).");
            resp.sendRedirect(buildTrackUrl(req, sessionId));
        }
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

    private String buildTrackUrl(HttpServletRequest req, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return req.getContextPath() + "/home";
        }
        String encodedSessionId = URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        return req.getContextPath() + "/session/track?id=" + encodedSessionId;
    }

    private void setFlashInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.info", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.error", message);
    }
}
