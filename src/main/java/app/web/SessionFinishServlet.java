package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.service.TrackingService;
import app.service.TrackingService.TempLogInput;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/session/finish")
public class SessionFinishServlet extends HttpServlet {
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(),
                new WorkoutLogDao());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String sessionId = req.getParameter("sessionId");

        try {
            List<TempLogInput> tempInputs = extractTempInputs(req);
            trackingService.finishAndSaveSession(currentUser, sessionId, tempInputs);
            setFlashInfo(req, "Training wurde beendet und gespeichert.");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildTrackUrl(req, sessionId));
        } catch (SQLException ex) {
            setFlashError(req, "Session konnte nicht beendet werden (DB-Fehler).");
            resp.sendRedirect(buildTrackUrl(req, sessionId));
        }
    }

    private List<TempLogInput> extractTempInputs(HttpServletRequest req) {
        String[] rowKeys = req.getParameterValues("rowKey");
        if (rowKeys == null || rowKeys.length == 0) {
            return new java.util.ArrayList<>();
        }

        List<TempLogInput> inputs = new ArrayList<>();
        for (String rowKey : rowKeys) {
            if (rowKey == null || rowKey.isBlank()) {
                continue;
            }

            String reps = req.getParameter("reps_" + rowKey);
            String weight = req.getParameter("weight_" + rowKey);
            String note = req.getParameter("note_" + rowKey);
            if (isBlank(reps) && isBlank(weight) && isBlank(note)) {
                continue;
            }

            String[] parts = rowKey.split("_");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Ungültige Satzdaten.");
            }

            long sessionExerciseId;
            int setNo;
            try {
                sessionExerciseId = Long.parseLong(parts[0]);
                setNo = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Ungültige Satzdaten.");
            }

            inputs.add(new TempLogInput(sessionExerciseId, setNo, reps, weight, note));
        }
        return inputs;
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void setFlashInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.info", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.error", message);
    }
}
