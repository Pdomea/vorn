package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.service.ProgramService;
import app.service.ProgramService.ProgramTrainingSelection;
import app.service.TrackingService;
import app.service.TrackingService.StartSessionResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/program/session/start")
public class ProgramSessionStartServlet extends HttpServlet {
    private transient ProgramService programService;
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        programService = new ProgramService(new PlanDao(), new PlanWeekDao(), new PlanWeekTrainingDao(), new TrainingDao());
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(), new WorkoutLogDao());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String planId = req.getParameter("planId");
        String planWeekId = req.getParameter("planWeekId");
        String trainingId = req.getParameter("trainingId");

        try {
            ProgramTrainingSelection selection = programService.validateUserProgramTrainingSelection(
                    currentUser,
                    planId,
                    planWeekId,
                    trainingId);

            StartSessionResult result = trackingService.startOrResumeSessionInProgram(
                    currentUser,
                    selection.getPlanId(),
                    selection.getPlanWeekId(),
                    selection.getTrainingId());

            if (result.isResumed()) {
                setFlashInfo(req, "Es laeuft bereits eine aktive Session. Du wurdest weitergeleitet.");
            } else {
                setFlashInfo(req, "Training wurde aus dem Wochenplan gestartet.");
            }
            resp.sendRedirect(req.getContextPath() + "/session/track?id=" + result.getSessionId());
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildProgramUrl(req, planId));
        } catch (SQLException ex) {
            setFlashError(req, "Session konnte nicht gestartet werden (DB-Fehler).");
            resp.sendRedirect(buildProgramUrl(req, planId));
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

    private String buildProgramUrl(HttpServletRequest req, String planId) {
        String baseUrl = req.getContextPath() + "/plan/details";
        if (planId == null || planId.isBlank()) {
            return baseUrl;
        }
        String encodedPlanId = URLEncoder.encode(planId, StandardCharsets.UTF_8);
        return baseUrl + "?planId=" + encodedPlanId;
    }

    private void setFlashInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.info", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.error", message);
    }
}
