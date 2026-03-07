package app.web;

import java.io.IOException;
import java.sql.SQLException;

import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.beans.HomeDashboardBean;
import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.SessionExerciseDao;
import app.dao.TrainingDao;
import app.dao.WorkoutLogDao;
import app.dao.WorkoutSessionDao;
import app.model.User;
import app.model.UserRole;
import app.model.WorkoutSession;
import app.service.ProgramService;
import app.service.ProgramService.DashboardMetrics;
import app.service.ProgramService.UserProgramPageData;
import app.service.TrackingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private transient TrackingService trackingService;
    private transient ProgramService programService;

    @Override
    public void init() throws ServletException {
        trackingService = new TrackingService(new TrainingDao(), new WorkoutSessionDao(), new SessionExerciseDao(), new WorkoutLogDao());
        programService = new ProgramService(new PlanDao(), new PlanWeekDao(), new PlanWeekTrainingDao(), new TrainingDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        HomeDashboardBean homeDashboardBean = new HomeDashboardBean();
        if (currentUser != null) {
            homeDashboardBean.setUserDisplayName(currentUser.getDisplayName());
            homeDashboardBean.setAdmin(currentUser.getRole() == UserRole.ADMIN);

            if (currentUser.getActivePlanId() == null) {
                req.getSession(true).setAttribute("flash.info", "Bitte zuerst einen Plan wählen.");
                resp.sendRedirect(req.getContextPath() + "/program/select");
                return;
            }

            try {
                WorkoutSession activeSession = trackingService.findActiveSession(currentUser);
                homeDashboardBean.setActiveSession(activeSession);
            } catch (SQLException ex) {
                homeDashboardBean.setActiveSessionError("Aktive Session konnte nicht geladen werden.");
            }

            loadUserDashboard(homeDashboardBean, currentUser, req.getContextPath(), req.getParameter("planId"));
        }
        moveFlashMessages(req, homeDashboardBean);
        req.setAttribute(BeanAttributes.forClass(HomeDashboardBean.class), homeDashboardBean);
        req.getRequestDispatcher("/jsp/home.jsp").forward(req, resp);
    }

    private void loadUserDashboard(
            HomeDashboardBean homeDashboardBean,
            User currentUser,
            String contextPath,
            String selectedPlanId) {
        try {
            UserProgramPageData pageData = programService.loadUserProgramPage(currentUser, selectedPlanId);
            homeDashboardBean.setDashboardSelectedPlan(pageData.getSelectedPlan());
            homeDashboardBean.setDashboardNextTraining(pageData.getSelectedPlanNextTraining());

            DashboardMetrics metrics = programService.loadDashboardMetrics(currentUser, pageData.getSelectedPlan());
            if (metrics != null) {
                homeDashboardBean.setWorkoutsCompletedText(String.valueOf(metrics.getWorkoutsCompleted()));
                homeDashboardBean.setHoursSpentText(metrics.getHoursSpentText());
                homeDashboardBean.setProgressPercent(metrics.getProgressPercent());
            }

            String fallbackPlanImagePath = contextPath + "/img/hero/landing-poster.svg";
            String selectedPlanImagePath = fallbackPlanImagePath;
            if (pageData.getSelectedPlan() != null
                    && pageData.getSelectedPlan().getHeroImagePath() != null
                    && !pageData.getSelectedPlan().getHeroImagePath().isBlank()) {
                String rawPath = pageData.getSelectedPlan().getHeroImagePath().trim();
                if (!rawPath.startsWith("/")) {
                    rawPath = "/" + rawPath;
                }
                selectedPlanImagePath = contextPath + rawPath;
            }
            homeDashboardBean.setFallbackPlanImagePath(fallbackPlanImagePath);
            homeDashboardBean.setSelectedPlanImagePath(selectedPlanImagePath);
        } catch (IllegalArgumentException ex) {
            homeDashboardBean.setDashboardError(ex.getMessage());
        } catch (SQLException ex) {
            homeDashboardBean.setDashboardError("Dashboard konnte nicht geladen werden.");
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

    private void moveFlashMessages(HttpServletRequest req, HomeDashboardBean homeDashboardBean) {
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
            homeDashboardBean.setInfo(infoText);
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            String errorText = error.toString();
            flashMessageBean.setError(errorText);
            homeDashboardBean.setError(errorText);
            session.removeAttribute("flash.error");
        }
    }
}
