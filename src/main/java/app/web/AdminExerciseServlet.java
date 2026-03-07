package app.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import app.beans.AdminExercisesBean;
import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.dao.ExerciseDao;
import app.model.Exercise;
import app.model.MuscleGroup;
import app.model.User;
import app.service.ExerciseService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/admin/exercises",
        "/admin/exercise/save",
        "/admin/exercise/archive",
        "/admin/exercise/delete"
})
public class AdminExerciseServlet extends HttpServlet {
    private transient ExerciseService exerciseService;

    @Override
    public void init() throws ServletException {
        exerciseService = new ExerciseService(new ExerciseDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if (!"/admin/exercises".equals(servletPath)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        User currentUser = getCurrentUser(req);
        try {
            AdminExercisesBean adminExercisesBean = new AdminExercisesBean();
            String sortBy = exerciseService.normalizeSortBy(req.getParameter("sortBy"));
            String sortDir = exerciseService.normalizeSortDirection(req.getParameter("sortDir"));
            String statusFilter = exerciseService.normalizeStatusFilter(req.getParameter("status"));

            List<Exercise> exercises = exerciseService.getAllExercisesForAdmin(currentUser, sortBy, sortDir, statusFilter);
            List<MuscleGroup> muscleGroups = exerciseService.getAllMuscleGroupsForAdmin(currentUser);

            adminExercisesBean.setExercises(exercises);
            adminExercisesBean.setMuscleGroups(muscleGroups);
            adminExercisesBean.setSortBy(sortBy);
            adminExercisesBean.setSortDir(sortDir);
            adminExercisesBean.setStatusFilter(statusFilter);

            String editId = req.getParameter("editId");
            if (editId != null && !editId.isBlank()) {
                Exercise editExercise = exerciseService.getExerciseForAdmin(currentUser, editId);
                if (editExercise == null) {
                    adminExercisesBean.setError("Übung zum Bearbeiten wurde nicht gefunden.");
                } else {
                    adminExercisesBean.setEditExercise(editExercise);
                }
            }

            applyFlash(moveFlashMessages(req), adminExercisesBean);
            req.setAttribute(BeanAttributes.forClass(AdminExercisesBean.class), adminExercisesBean);
            req.getRequestDispatcher("/jsp/admin/adminExercises.jsp").forward(req, resp);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            AdminExercisesBean adminExercisesBean = new AdminExercisesBean();
            adminExercisesBean.setError(ex.getMessage());
            req.setAttribute(BeanAttributes.forClass(AdminExercisesBean.class), adminExercisesBean);
            req.getRequestDispatcher("/jsp/admin/adminExercises.jsp").forward(req, resp);
        } catch (SQLException ex) {
            throw new ServletException("Admin exercises could not be loaded.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        User currentUser = getCurrentUser(req);

        try {
            switch (servletPath) {
                case "/admin/exercise/save" -> handleSave(req, currentUser);
                case "/admin/exercise/archive" -> handleArchive(req, currentUser);
                case "/admin/exercise/delete" -> handleDelete(req, currentUser);
                default -> {
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/exercises");
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/exercises");
        } catch (SQLException ex) {
            throw new ServletException("Admin exercises action failed.", ex);
        }
    }

    private void handleSave(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String[] muscleGroupIds = req.getParameterValues("muscleGroupIds");

        Exercise saved = exerciseService.saveExerciseAsAdmin(currentUser, id, name, description, muscleGroupIds);
        if (id == null || id.isBlank()) {
            setFlashInfo(req, "Übung angelegt: " + saved.getName());
        } else {
            setFlashInfo(req, "Übung aktualisiert: " + saved.getName());
        }
    }

    private void handleArchive(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        exerciseService.archiveExerciseAsAdmin(currentUser, id);
        setFlashInfo(req, "Übung wurde archiviert.");
    }

    private void handleDelete(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        exerciseService.deleteExerciseAsAdmin(currentUser, id);
        setFlashInfo(req, "Übung wurde gelöscht.");
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

    private void applyFlash(FlashMessageBean flashMessageBean, AdminExercisesBean adminExercisesBean) {
        if (flashMessageBean == null || adminExercisesBean == null) {
            return;
        }
        if (adminExercisesBean.getError() == null || adminExercisesBean.getError().isBlank()) {
            adminExercisesBean.setError(flashMessageBean.getError());
        }
        adminExercisesBean.setInfo(flashMessageBean.getInfo());
    }
}
