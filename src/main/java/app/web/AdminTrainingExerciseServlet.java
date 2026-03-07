package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import app.beans.AdminTrainingExercisesBean;
import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.dao.ExerciseDao;
import app.dao.TrainingDao;
import app.dao.TrainingExerciseDao;
import app.model.User;
import app.service.AdminMappingService;
import app.service.AdminMappingService.MappingPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/admin/training/exercises",
        "/admin/training/exercise/add",
        "/admin/training/exercise/update"
})
public class AdminTrainingExerciseServlet extends HttpServlet {
    private transient AdminMappingService adminMappingService;

    @Override
    public void init() throws ServletException {
        adminMappingService = new AdminMappingService(new TrainingDao(), new TrainingExerciseDao(), new ExerciseDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/admin/training/exercises".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        User currentUser = getCurrentUser(req);
        String trainingId = req.getParameter("trainingId");
        String search = req.getParameter("search");
        String muscleGroupId = req.getParameter("muscleGroupId");
        String sortDir = req.getParameter("sortDir");
        AdminTrainingExercisesBean adminTrainingExercisesBean = new AdminTrainingExercisesBean();

        try {
            MappingPageData pageData = adminMappingService.loadPage(currentUser, trainingId, search, muscleGroupId, sortDir);
            adminTrainingExercisesBean.setTraining(pageData.getTraining());
            adminTrainingExercisesBean.setMappings(pageData.getMappings());
            adminTrainingExercisesBean.setActiveExercises(pageData.getActiveExercises());
            adminTrainingExercisesBean.setMuscleGroups(pageData.getMuscleGroups());
            adminTrainingExercisesBean.setSearch(pageData.getSearchTerm());
            adminTrainingExercisesBean.setSelectedMuscleGroupId(pageData.getSelectedMuscleGroupId());
            adminTrainingExercisesBean.setSortDir(pageData.getSortDir());
            applyFlash(moveFlashMessages(req), adminTrainingExercisesBean);
            req.setAttribute(BeanAttributes.forClass(AdminTrainingExercisesBean.class), adminTrainingExercisesBean);
            req.getRequestDispatcher("/jsp/admin/adminTrainingExercises.jsp").forward(req, resp);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/trainings");
        } catch (SQLException ex) {
            throw new ServletException("Admin training exercise page could not be loaded.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        String trainingId = req.getParameter("trainingId");

        try {
            String servletPath = req.getServletPath();
            if ("/admin/training/exercise/add".equals(servletPath)) {
                adminMappingService.addMapping(
                        currentUser,
                        trainingId,
                        req.getParameter("exerciseId"),
                        req.getParameter("plannedSets"),
                        req.getParameter("plannedReps"),
                        req.getParameter("sortOrder"));
                setFlashInfo(req, "Zuordnung wurde angelegt.");
            } else if ("/admin/training/exercise/update".equals(servletPath)) {
                adminMappingService.updateMapping(
                        currentUser,
                        trainingId,
                        req.getParameter("mappingId"),
                        req.getParameter("plannedSets"),
                        req.getParameter("plannedReps"),
                        req.getParameter("sortOrder"));
                setFlashInfo(req, "Zuordnung wurde aktualisiert.");
            } else {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            resp.sendRedirect(buildMappingPageUrl(req, trainingId));
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(buildMappingPageUrl(req, trainingId));
        } catch (SQLException ex) {
            throw new ServletException("Admin training exercise action failed.", ex);
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

    private String buildMappingPageUrl(HttpServletRequest req, String trainingId) {
        if (trainingId == null || trainingId.isBlank()) {
            return req.getContextPath() + "/admin/trainings";
        }
        StringBuilder url = new StringBuilder(req.getContextPath())
                .append("/admin/training/exercises?trainingId=")
                .append(URLEncoder.encode(trainingId, StandardCharsets.UTF_8));

        appendOptionalQuery(url, "search", req.getParameter("search"));
        appendOptionalQuery(url, "muscleGroupId", req.getParameter("muscleGroupId"));
        appendOptionalQuery(url, "sortDir", req.getParameter("sortDir"));
        return url.toString();
    }

    private void appendOptionalQuery(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append("&")
                .append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
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

    private void applyFlash(FlashMessageBean flashMessageBean, AdminTrainingExercisesBean adminTrainingExercisesBean) {
        if (flashMessageBean == null || adminTrainingExercisesBean == null) {
            return;
        }
        adminTrainingExercisesBean.setInfo(flashMessageBean.getInfo());
        adminTrainingExercisesBean.setError(flashMessageBean.getError());
    }
}
