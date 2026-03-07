package app.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.beans.PlanDetailsBean;
import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.TrainingDao;
import app.model.PlanWeek;
import app.model.User;
import app.service.ProgramService;
import app.service.ProgramService.UserProgramPageData;
import app.service.ProgramService.WeekProgressData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/plan/details")
public class PlanDetailsServlet extends HttpServlet {
    private transient ProgramService programService;

    @Override
    public void init() throws ServletException {
        programService = new ProgramService(new PlanDao(), new PlanWeekDao(), new PlanWeekTrainingDao(), new TrainingDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        PlanDetailsBean planDetailsBean = new PlanDetailsBean();
        if (currentUser != null && currentUser.getActivePlanId() == null) {
            req.getSession(true).setAttribute("flash.info", "Bitte zuerst einen Plan auswählen.");
            resp.sendRedirect(req.getContextPath() + "/program/select");
            return;
        }

        try {
            String selectedPlanId = req.getParameter("planId");
            UserProgramPageData pageData = programService.loadUserProgramPage(currentUser, selectedPlanId);
            applyPageData(planDetailsBean, pageData, req.getParameter("weekNo"), req.getContextPath());
            moveFlashMessages(req, planDetailsBean);
            forwardWithBean(req, resp, planDetailsBean);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            try {
                UserProgramPageData fallbackPageData = programService.loadUserProgramPage(currentUser, null);
                applyPageData(planDetailsBean, fallbackPageData, req.getParameter("weekNo"), req.getContextPath());
            } catch (SQLException fallbackEx) {
                throw new ServletException("Plan-Details konnten nicht geladen werden.", fallbackEx);
            }
            planDetailsBean.setError(ex.getMessage());
            moveFlashMessages(req, planDetailsBean);
            forwardWithBean(req, resp, planDetailsBean);
        } catch (SQLException ex) {
            throw new ServletException("Plan-Details konnten nicht geladen werden.", ex);
        }
    }

    private void applyPageData(
            PlanDetailsBean planDetailsBean,
            UserProgramPageData pageData,
            String requestedWeekNo,
            String contextPath) {
        planDetailsBean.setSelectedPlan(pageData.getSelectedPlan());
        planDetailsBean.setSelectedPlanWeeks(pageData.getSelectedPlanWeeks());
        planDetailsBean.setSelectedPlanNextTraining(pageData.getSelectedPlanNextTraining());
        applyPlanProgress(planDetailsBean, pageData);
        applyPlanHeroImage(planDetailsBean, contextPath);
        if (pageData.getSelectedPlanNextTraining() != null) {
            planDetailsBean.setNextTrainingText(
                    "Woche "
                            + pageData.getSelectedPlanNextTraining().getWeekNo()
                            + " - "
                            + pageData.getSelectedPlanNextTraining().getTrainingTitle());
        }

        PlanWeek selectedWeek = resolveSelectedWeek(pageData.getSelectedPlanWeeks(), requestedWeekNo);
        planDetailsBean.setSelectedDetailWeek(selectedWeek);
        if (selectedWeek == null) {
            planDetailsBean.setSelectedDetailWeekTrainings(List.of());
            planDetailsBean.setSelectedDetailWeekStatuses(Map.of());
            planDetailsBean.setSelectedDetailWeekProgress(null);
            return;
        }

        planDetailsBean.setSelectedDetailWeekTrainings(
                pageData.getSelectedPlanWeekTrainings().getOrDefault(selectedWeek.getId(), List.of()));
        planDetailsBean.setSelectedDetailWeekStatuses(
                pageData.getSelectedPlanWeekTrainingStatus().getOrDefault(selectedWeek.getId(), Map.of()));
        planDetailsBean.setSelectedDetailWeekProgress(pageData.getSelectedPlanWeekProgress().get(selectedWeek.getId()));
    }

    private void applyPlanProgress(PlanDetailsBean planDetailsBean, UserProgramPageData pageData) {
        int completedSlots = 0;
        int totalSlots = 0;
        Map<Long, WeekProgressData> progressByWeek = pageData.getSelectedPlanWeekProgress();
        List<PlanWeek> weeks = pageData.getSelectedPlanWeeks();

        if (weeks != null && progressByWeek != null) {
            for (PlanWeek week : weeks) {
                WeekProgressData weekProgress = progressByWeek.get(week.getId());
                if (weekProgress == null) {
                    continue;
                }
                completedSlots += Math.max(0, weekProgress.getCompletedSlots());
                totalSlots += Math.max(0, weekProgress.getTotalSlots());
            }
        }

        int progressPercent = 0;
        if (totalSlots > 0) {
            progressPercent = (int) Math.round((completedSlots * 100.0) / totalSlots);
        }

        planDetailsBean.setCompletedSlots(completedSlots);
        planDetailsBean.setTotalSlots(totalSlots);
        planDetailsBean.setProgressPercent(progressPercent);
    }

    private void applyPlanHeroImage(PlanDetailsBean planDetailsBean, String contextPath) {
        String fallbackPlanImagePath = contextPath + "/img/hero/landing-poster.svg";
        String selectedPlanImagePath = fallbackPlanImagePath;
        if (planDetailsBean.getSelectedPlan() != null
                && planDetailsBean.getSelectedPlan().getHeroImagePath() != null
                && !planDetailsBean.getSelectedPlan().getHeroImagePath().isBlank()) {
            String rawPath = planDetailsBean.getSelectedPlan().getHeroImagePath().trim();
            if (!rawPath.startsWith("/")) {
                rawPath = "/" + rawPath;
            }
            selectedPlanImagePath = contextPath + rawPath;
        }
        planDetailsBean.setFallbackPlanImagePath(fallbackPlanImagePath);
        planDetailsBean.setSelectedPlanImagePath(selectedPlanImagePath);
    }

    private PlanWeek resolveSelectedWeek(List<PlanWeek> weeks, String requestedWeekNo) {
        if (weeks == null || weeks.isEmpty()) {
            return null;
        }
        if (requestedWeekNo == null || requestedWeekNo.isBlank()) {
            return weeks.get(0);
        }
        try {
            int weekNo = Integer.parseInt(requestedWeekNo.trim());
            if (weekNo <= 0) {
                return weeks.get(0);
            }
            for (PlanWeek week : weeks) {
                if (week.getWeekNo() == weekNo) {
                    return week;
                }
            }
        } catch (NumberFormatException ignored) {
            return weeks.get(0);
        }
        return weeks.get(0);
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

    private void forwardWithBean(HttpServletRequest req, HttpServletResponse resp, PlanDetailsBean planDetailsBean)
            throws ServletException, IOException {
        req.setAttribute(BeanAttributes.forClass(PlanDetailsBean.class), planDetailsBean);
        req.getRequestDispatcher("/jsp/planDetails.jsp").forward(req, resp);
    }

    private void moveFlashMessages(HttpServletRequest req, PlanDetailsBean planDetailsBean) {
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
            if (planDetailsBean.getInfo() == null || planDetailsBean.getInfo().isBlank()) {
                planDetailsBean.setInfo(infoText);
            }
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            String errorText = error.toString();
            flashMessageBean.setError(errorText);
            if (planDetailsBean.getError() == null || planDetailsBean.getError().isBlank()) {
                planDetailsBean.setError(errorText);
            }
            session.removeAttribute("flash.error");
        }
    }
}
