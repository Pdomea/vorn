package app.web;

import java.io.IOException;
import java.sql.SQLException;

import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.beans.ProgramSelectBean;
import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.TrainingDao;
import app.dao.UserDao;
import app.model.User;
import app.service.ProgramService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/program/select")
public class ProgramSelectServlet extends HttpServlet {
    private transient ProgramService programService;

    @Override
    public void init() throws ServletException {
        programService = new ProgramService(new PlanDao(), new PlanWeekDao(), new PlanWeekTrainingDao(), new TrainingDao(), new UserDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        try {
            ProgramSelectBean programSelectBean = new ProgramSelectBean();
            programSelectBean.loadForUser(programService, currentUser);
            applyFlash(moveFlashMessages(req), programSelectBean);
            req.setAttribute(BeanAttributes.forClass(ProgramSelectBean.class), programSelectBean);
            req.getRequestDispatcher("/jsp/programSelect.jsp").forward(req, resp);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (SQLException ex) {
            throw new ServletException("Programm-Auswahl konnte nicht geladen werden.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String planId = req.getParameter("planId");

        try {
            programService.setActivePlanForUser(currentUser, planId);
            setFlashInfo(req, "Plan gewechselt. Fortschritt zurückgesetzt.");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildSelectUrl(req));
        } catch (SQLException ex) {
            setFlashError(req, "Planwechsel fehlgeschlagen.");
            resp.sendRedirect(buildSelectUrl(req));
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

    private String buildSelectUrl(HttpServletRequest req) {
        return req.getContextPath() + "/program/select";
    }

    private void setFlashInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.info", message);
    }

    private void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute("flash.error", message);
    }

    private FlashMessageBean moveFlashMessages(HttpServletRequest req) {
        FlashMessageBean flashMessageBean = new FlashMessageBean();
        req.setAttribute(BeanAttributes.FLASH_MESSAGE_BEAN, flashMessageBean);

        HttpSession session = req.getSession(false);
        if (session == null) {
            return flashMessageBean;
        }
        Object info = session.getAttribute("flash.info");
        Object error = session.getAttribute("flash.error");
        if (info != null) {
            String infoText = info.toString();
            flashMessageBean.setInfo(infoText);
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            String errorText = error.toString();
            flashMessageBean.setError(errorText);
            session.removeAttribute("flash.error");
        }
        return flashMessageBean;
    }

    private void applyFlash(FlashMessageBean flashMessageBean, ProgramSelectBean programSelectBean) {
        if (flashMessageBean == null || programSelectBean == null) {
            return;
        }
        if (programSelectBean.getError() == null || programSelectBean.getError().isBlank()) {
            programSelectBean.setError(flashMessageBean.getError());
        }
        programSelectBean.setInfo(flashMessageBean.getInfo());
    }
}
