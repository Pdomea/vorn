package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import app.beans.AdminProgramDetailBean;
import app.beans.AdminProgramsBean;
import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.dao.PlanDao;
import app.dao.PlanWeekDao;
import app.dao.PlanWeekTrainingDao;
import app.dao.TrainingDao;
import app.model.Plan;
import app.model.PlanWeek;
import app.model.Training;
import app.model.User;
import app.service.ProgramService;
import app.service.ProgramService.PlanDetailData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/admin/programs",
        "/admin/programs/detail",
        "/admin/program/create",
        "/admin/program/save",
        "/admin/program/hero/save",
        "/admin/program/activate",
        "/admin/program/archive",
        "/admin/program/delete",
        "/admin/program/week/add",
        "/admin/program/week/duplicate",
        "/admin/program/week/remove",
        "/admin/program/week/training/add",
        "/admin/program/week/training/remove"
})
public class AdminProgramServlet extends HttpServlet {
    private transient ProgramService programService;

    @Override
    public void init() throws ServletException {
        programService = new ProgramService(
                new PlanDao(),
                new PlanWeekDao(),
                new PlanWeekTrainingDao(),
                new TrainingDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String servletPath = req.getServletPath();
        try {
            if ("/admin/programs".equals(servletPath)) {
                renderProgramsOverview(req, resp, currentUser);
                return;
            }
            if ("/admin/programs/detail".equals(servletPath)) {
                renderProgramDetail(req, resp, currentUser);
                return;
            }

            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildProgramsOverviewUrl(req));
        } catch (SQLException ex) {
            throw new ServletException("Admin programs page could not be loaded.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String redirectPlanId = req.getParameter("planId");
        String servletPath = req.getServletPath();
        String redirectUrl;

        try {
            // neues switch case von java genutzt (ohne break), ist uebersichtlicher
            switch (servletPath) {
                case "/admin/program/create" -> {
                    Plan createdPlan = programService.createPlanWithDefaultWeek(
                            currentUser,
                            req.getParameter("name"),
                            req.getParameter("description"),
                            req.getParameter("heroImagePath"));
                    redirectPlanId = String.valueOf(createdPlan.getId());
                    setFlashInfo(req, "Plan angelegt: " + createdPlan.getName() + " (Woche 1 wurde erstellt).");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/save" -> {
                    programService.updatePlanAsAdmin(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("name"),
                            req.getParameter("description"));
                    programService.updatePlanHeroImage(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("heroImagePath"));
                    setFlashInfo(req, "Änderung wurde gespeichert.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/hero/save" -> {
                    programService.updatePlanHeroImage(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("heroImagePath"));
                    setFlashInfo(req, "Bildpfad wurde gespeichert.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/archive" -> {
                    programService.archivePlan(currentUser, req.getParameter("planId"));
                    setFlashInfo(req, "Plan wurde archiviert.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/delete" -> {
                    programService.deletePlanAsAdmin(currentUser, req.getParameter("planId"));
                    setFlashInfo(req, "Plan wurde gelöscht.");
                    redirectPlanId = null;
                    redirectUrl = buildProgramsOverviewUrl(req);
                }
                case "/admin/program/activate" -> {
                    boolean activated = programService.activatePlan(currentUser, req.getParameter("planId"));
                    if (activated) {
                        setFlashInfo(req, "Plan wurde aktiviert.");
                    } else {
                        setFlashInfo(req, "Plan ist bereits aktiv.");
                    }
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/week/add" -> {
                    PlanWeek createdWeek = programService.addNextWeek(currentUser, req.getParameter("planId"));
                    redirectPlanId = String.valueOf(createdWeek.getPlanId());
                    setFlashInfo(req, "Neue Woche hinzugefügt: Woche " + createdWeek.getWeekNo() + ".");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/week/duplicate" -> {
                    PlanWeek duplicatedWeek = programService.duplicateWeek(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("planWeekId"));
                    redirectPlanId = String.valueOf(duplicatedWeek.getPlanId());
                    setFlashInfo(req, "Woche wurde dupliziert: neue Woche " + duplicatedWeek.getWeekNo() + ".");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/week/remove" -> {
                    programService.removeWeek(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("planWeekId"));
                    setFlashInfo(req, "Woche wurde gelöscht und neu nummeriert.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/week/training/add" -> {
                    programService.addTrainingToWeek(
                            currentUser,
                            req.getParameter("planWeekId"),
                            req.getParameter("trainingId"),
                            req.getParameter("sortOrder"));
                    setFlashInfo(req, "Training wurde der Woche zugeordnet.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                case "/admin/program/week/training/remove" -> {
                    programService.removeTrainingFromWeek(
                            currentUser,
                            req.getParameter("planId"),
                            req.getParameter("mappingId"));
                    setFlashInfo(req, "Zuordnung wurde entfernt.");
                    redirectUrl = buildProgramDetailPageUrl(req, redirectPlanId);
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
            }
            resp.sendRedirect(redirectUrl);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            if ("/admin/program/delete".equals(servletPath)) {
                resp.sendRedirect(buildProgramsOverviewUrl(req));
            } else {
                resp.sendRedirect(buildProgramDetailPageUrl(req, redirectPlanId));
            }
        } catch (SQLException ex) {
            throw new ServletException("Admin program action failed.", ex);
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

    private String buildProgramsOverviewUrl(HttpServletRequest req) {
        return req.getContextPath() + "/admin/programs";
    }

    private String buildProgramDetailPageUrl(HttpServletRequest req, String planId) {
        String baseUrl = req.getContextPath() + "/admin/programs/detail";
        if (planId == null || planId.isBlank()) {
            return buildProgramsOverviewUrl(req);
        }
        String encodedPlanId = URLEncoder.encode(planId, StandardCharsets.UTF_8);
        return baseUrl + "?planId=" + encodedPlanId;
    }

    private void renderProgramsOverview(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, ServletException, IOException {
        AdminProgramsBean adminProgramsBean = new AdminProgramsBean();
        String statusFilter = programService.normalizePlanStatusFilter(req.getParameter("status"));
        List<Plan> plans = programService.getAllPlans(currentUser, statusFilter);
        adminProgramsBean.setStatusFilter(statusFilter);
        adminProgramsBean.setPlans(plans);
        applyFlash(moveFlashMessages(req), adminProgramsBean);
        req.setAttribute(BeanAttributes.forClass(AdminProgramsBean.class), adminProgramsBean);
        req.getRequestDispatcher("/jsp/admin/adminPrograms.jsp").forward(req, resp);
    }

    private void renderProgramDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws SQLException, ServletException, IOException {
        AdminProgramDetailBean adminProgramDetailBean = new AdminProgramDetailBean();
        List<Plan> plans = programService.getAllPlans(currentUser);
        List<Training> trainings = programService.getAllTrainingsForAdmin(currentUser);

        adminProgramDetailBean.setPlans(plans);
        adminProgramDetailBean.setTrainings(trainings);

        String selectedPlanId = req.getParameter("planId");
        if ((selectedPlanId == null || selectedPlanId.isBlank()) && !plans.isEmpty()) {
            selectedPlanId = String.valueOf(plans.get(0).getId());
        }

        if (selectedPlanId != null && !selectedPlanId.isBlank()) {
            PlanDetailData detail = programService.getPlanDetail(currentUser, selectedPlanId);
            adminProgramDetailBean.setSelectedPlanDetail(detail);
        }

        applyFlash(moveFlashMessages(req), adminProgramDetailBean);
        req.setAttribute(BeanAttributes.forClass(AdminProgramDetailBean.class), adminProgramDetailBean);
        req.getRequestDispatcher("/jsp/admin/adminProgramDetail.jsp").forward(req, resp);
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
            flashMessageBean.setInfo(info.toString());
            session.removeAttribute("flash.info");
        }
        if (error != null) {
            flashMessageBean.setError(error.toString());
            session.removeAttribute("flash.error");
        }
        return flashMessageBean;
    }

    private void applyFlash(FlashMessageBean flashMessageBean, AdminProgramsBean adminProgramsBean) {
        if (flashMessageBean == null || adminProgramsBean == null) {
            return;
        }
        adminProgramsBean.setInfo(flashMessageBean.getInfo());
        adminProgramsBean.setError(flashMessageBean.getError());
    }

    private void applyFlash(FlashMessageBean flashMessageBean, AdminProgramDetailBean adminProgramDetailBean) {
        if (flashMessageBean == null || adminProgramDetailBean == null) {
            return;
        }
        adminProgramDetailBean.setInfo(flashMessageBean.getInfo());
        adminProgramDetailBean.setError(flashMessageBean.getError());
    }
}
