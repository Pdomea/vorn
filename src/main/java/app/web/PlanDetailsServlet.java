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
        programService = new ProgramService(new PlanDao(), new PlanWeekDao(), new PlanWeekTrainingDao(),
                new TrainingDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        PlanDetailsBean planDetailsBean = new PlanDetailsBean();
        String selectedPlanId = req.getParameter("planId");

        // allow preview if user is not active in this plan but explicitly requests it
        if (currentUser != null && currentUser.getActivePlanId() == null
                && (selectedPlanId == null || selectedPlanId.isBlank())) {
            req.getSession(true).setAttribute("flash.info", "Bitte zuerst einen Plan auswählen.");
            resp.sendRedirect(req.getContextPath() + "/program/select");
            return;
        }

        try {
            UserProgramPageData pageData = programService.loadUserProgramPage(currentUser, selectedPlanId);

            Long activeId = (currentUser != null) ? currentUser.getActivePlanId() : null;
            boolean isPreview = (activeId == null)
                    || (pageData.getSelectedPlan() != null && pageData.getSelectedPlan().getId() != activeId);
            planDetailsBean.setPreviewMode(isPreview);

            applyPageData(planDetailsBean, pageData, req.getParameter("weekNo"), req.getContextPath());
            moveFlashMessages(req, planDetailsBean);
            forwardWithBean(req, resp, planDetailsBean);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // fallback einbauen, damit der user keinen hässlichen 500 error beim neuladen
            // sieht
            try {
                UserProgramPageData fallbackPageData = programService.loadUserProgramPage(currentUser, null);
                applyPageData(planDetailsBean, fallbackPageData, req.getParameter("weekNo"), req.getContextPath());
            } catch (Exception fallbackEx) {
                // if fallback also fails, use emergency error reporting
                resp.setContentType("text/plain;charset=UTF-8");
                fallbackEx.printStackTrace(resp.getWriter());
                return;
            }
            planDetailsBean.setError(ex.getMessage());
            moveFlashMessages(req, planDetailsBean);
            forwardWithBean(req, resp, planDetailsBean);
        } catch (Exception ex) {
            // Emergency error reporting for any other exception
            resp.setContentType("text/plain;charset=UTF-8");
            ex.printStackTrace(resp.getWriter());
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
            planDetailsBean.setSelectedDetailWeekTrainings(new java.util.ArrayList<>());
            planDetailsBean.setSelectedDetailWeekStatuses(new java.util.HashMap<>());
            planDetailsBean.setSelectedDetailWeekProgress(null);
            return;
        }

        planDetailsBean.setSelectedDetailWeekTrainings(
                pageData.getWochenTrainings().getOrDefault(selectedWeek.getId(),
                        new java.util.ArrayList<>()));
        planDetailsBean.setSelectedDetailWeekStatuses(
                pageData.getWeekStatusMap().getOrDefault(selectedWeek.getId(),
                        new java.util.HashMap<>()));
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
