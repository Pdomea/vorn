package app.web;

import java.io.IOException;
import java.sql.SQLException;

import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.beans.SessionTrackBean;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.service.TrackingService;
import app.service.TrackingService.TrackPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/session/track")
public class SessionTrackServlet extends HttpServlet {
    private transient TrackingService trackingService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(),
                new WorkoutLogDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String sessionId = req.getParameter("id");
        SessionTrackBean sessionTrackBean = new SessionTrackBean();

        try {
            TrackPageData data = trackingService.loadTrackPage(currentUser, sessionId);
            sessionTrackBean.setSessionData(data.getSession());
            sessionTrackBean.setItems(data.getSnapshotItems());
            sessionTrackBean.setLogsByExercise(data.getLogsByExercise());
            sessionTrackBean.setLastScoreByExerciseId(data.getLastScoreByExerciseId());
            sessionTrackBean.setAlternativenMap(data.getAlternativenMap());
            moveFlashMessages(req, sessionTrackBean);
            forwardWithBean(req, resp, sessionTrackBean);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            sessionTrackBean.setError(ex.getMessage());
            moveFlashMessages(req, sessionTrackBean);
            forwardWithBean(req, resp, sessionTrackBean);
        } catch (SQLException ex) {
            sessionTrackBean.setError("Sessiondaten konnten nicht geladen werden.");
            moveFlashMessages(req, sessionTrackBean);
            forwardWithBean(req, resp, sessionTrackBean);
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

    private void forwardWithBean(HttpServletRequest req, HttpServletResponse resp, SessionTrackBean sessionTrackBean)
            throws ServletException, IOException {
        req.setAttribute(BeanAttributes.forClass(SessionTrackBean.class), sessionTrackBean);
        req.getRequestDispatcher("/jsp/sessionTrack.jsp").forward(req, resp);
    }

    private void moveFlashMessages(HttpServletRequest req, SessionTrackBean sessionTrackBean) {
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
            sessionTrackBean.setInfo(infoText);
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            String errorText = error.toString();
            flashMessageBean.setError(errorText);
            if (sessionTrackBean.getError() == null || sessionTrackBean.getError().isBlank()) {
                sessionTrackBean.setError(errorText);
            }
            session.removeAttribute("flash.error");
        }
    }
}
