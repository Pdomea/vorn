package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.beans.SessionResultBean;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.service.TrackingService;
import app.service.TrackingService.SessionResultData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/program/session/result")
public class ProgramSessionResultServlet extends HttpServlet {
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(), new WorkoutLogDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String sessionId = req.getParameter("id");
        String source = req.getParameter("source");
        String planId = req.getParameter("planId");
        String weekNo = req.getParameter("weekNo");
        SessionResultBean sessionResultBean = new SessionResultBean();

        sessionResultBean.setBackUrl(buildBackUrl(req, source, planId, weekNo));
        sessionResultBean.setBackLabel("home".equals(source) ? "Zurück zu Home" : "Zurück zum Wochenplan");

        try {
            SessionResultData resultData = trackingService.loadFinishedSessionResult(currentUser, sessionId);
            sessionResultBean.setResultData(resultData);
            moveFlashMessages(req, sessionResultBean);
            forwardWithBean(req, resp, sessionResultBean);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            sessionResultBean.setError(ex.getMessage());
            moveFlashMessages(req, sessionResultBean);
            forwardWithBean(req, resp, sessionResultBean);
        } catch (SQLException ex) {
            sessionResultBean.setError("Ergebnis konnte nicht geladen werden.");
            moveFlashMessages(req, sessionResultBean);
            forwardWithBean(req, resp, sessionResultBean);
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

    private String buildBackUrl(HttpServletRequest req, String source, String planId, String weekNo) {
        if ("home".equals(source)) {
            return buildHomeUrl(req, planId, weekNo);
        }
        return buildProgramUrl(req, planId);
    }

    private String buildHomeUrl(HttpServletRequest req, String planId, String weekNo) {
        String baseUrl = req.getContextPath() + "/home";
        List<String> params = new ArrayList<>();
        if (planId != null && !planId.isBlank()) {
            params.add("planId=" + URLEncoder.encode(planId, StandardCharsets.UTF_8));
        }
        if (weekNo != null && !weekNo.isBlank()) {
            params.add("weekNo=" + URLEncoder.encode(weekNo, StandardCharsets.UTF_8));
        }
        if (params.isEmpty()) {
            return baseUrl;
        }
        return baseUrl + "?" + String.join("&", params);
    }

    private String buildProgramUrl(HttpServletRequest req, String planId) {
        String baseUrl = req.getContextPath() + "/plan/details";
        if (planId == null || planId.isBlank()) {
            return baseUrl;
        }
        return baseUrl + "?planId=" + URLEncoder.encode(planId, StandardCharsets.UTF_8);
    }

    private void forwardWithBean(HttpServletRequest req, HttpServletResponse resp, SessionResultBean sessionResultBean)
            throws ServletException, IOException {
        req.setAttribute(BeanAttributes.forClass(SessionResultBean.class), sessionResultBean);
        req.getRequestDispatcher("/jsp/programSessionResult.jsp").forward(req, resp);
    }

    private void moveFlashMessages(HttpServletRequest req, SessionResultBean sessionResultBean) {
        FlashMessageBean flashMessageBean = new FlashMessageBean();
        req.setAttribute(BeanAttributes.FLASH_MESSAGE_BEAN, flashMessageBean);

        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object info = session.getAttribute("flash.info");
        Object error = session.getAttribute("flash.error");
        if (info != null) {
            String infoText = info.toString();
            flashMessageBean.setInfo(infoText);
            sessionResultBean.setInfo(infoText);
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            String errorText = error.toString();
            flashMessageBean.setError(errorText);
            if (sessionResultBean.getError() == null || sessionResultBean.getError().isBlank()) {
                sessionResultBean.setError(errorText);
            }
            session.removeAttribute("flash.error");
        }
    }
}
