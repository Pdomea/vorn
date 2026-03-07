package app.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import app.beans.AdminTrainingsBean;
import app.beans.BeanAttributes;
import app.beans.FlashMessageBean;
import app.dao.TrainingDao;
import app.dao.TrainingExerciseDao;
import app.model.Training;
import app.model.User;
import app.service.TrainingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/admin/trainings",
        "/admin/training/save",
        "/admin/training/publish",
        "/admin/training/hide",
        "/admin/training/delete"
})
public class AdminTrainingServlet extends HttpServlet {
    private transient TrainingService trainingService;

    @Override
    public void init() throws ServletException {
        trainingService = new TrainingService(new TrainingDao(), new TrainingExerciseDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if (!"/admin/trainings".equals(servletPath)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        User currentUser = getCurrentUser(req);
        try {
            AdminTrainingsBean adminTrainingsBean = new AdminTrainingsBean();
            String sortBy = trainingService.normalizeSortBy(req.getParameter("sortBy"));
            String sortDir = trainingService.normalizeSortDirection(req.getParameter("sortDir"));
            String statusFilter = trainingService.normalizeStatusFilter(req.getParameter("status"));
            List<Training> trainings = trainingService.getAllTrainingsForAdmin(currentUser, sortBy, sortDir, statusFilter);
            adminTrainingsBean.setSortBy(sortBy);
            adminTrainingsBean.setSortDir(sortDir);
            adminTrainingsBean.setStatusFilter(statusFilter);
            adminTrainingsBean.setTrainings(trainings);

            String editId = req.getParameter("editId");
            if (editId != null && !editId.isBlank()) {
                Training editTraining = trainingService.getTrainingForAdmin(currentUser, editId);
                if (editTraining == null) {
                    adminTrainingsBean.setError("Training zum Bearbeiten wurde nicht gefunden.");
                } else {
                    adminTrainingsBean.setEditTraining(editTraining);
                }
            }

            applyFlash(moveFlashMessages(req), adminTrainingsBean);
            req.setAttribute(BeanAttributes.forClass(AdminTrainingsBean.class), adminTrainingsBean);
            req.getRequestDispatcher("/jsp/admin/adminTrainings.jsp").forward(req, resp);
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            AdminTrainingsBean adminTrainingsBean = new AdminTrainingsBean();
            adminTrainingsBean.setError(ex.getMessage());
            req.setAttribute(BeanAttributes.forClass(AdminTrainingsBean.class), adminTrainingsBean);
            req.getRequestDispatcher("/jsp/admin/adminTrainings.jsp").forward(req, resp);
        } catch (SQLException ex) {
            throw new ServletException("Admin trainings could not be loaded.", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        User currentUser = getCurrentUser(req);

        try {
            switch (servletPath) {
                case "/admin/training/save" -> handleSave(req, currentUser);
                case "/admin/training/publish" -> handlePublish(req, currentUser);
                case "/admin/training/hide" -> handleHide(req, currentUser);
                case "/admin/training/delete" -> handleDelete(req, currentUser);
                default -> {
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/trainings");
        } catch (SecurityException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setFlashError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/trainings");
        } catch (SQLException ex) {
            throw new ServletException("Admin trainings action failed.", ex);
        }
    }

    private void handleSave(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        Training saved = trainingService.saveTrainingAsAdmin(currentUser, id, title, description);
        if (id == null || id.isBlank()) {
            setFlashInfo(req, "Training angelegt: " + saved.getTitle());
        } else {
            setFlashInfo(req, "Training aktualisiert: " + saved.getTitle());
        }
    }

    private void handlePublish(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        trainingService.publishTrainingAsAdmin(currentUser, id);
        setFlashInfo(req, "Training wurde veröffentlicht.");
    }

    private void handleHide(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        trainingService.hideTrainingAsAdmin(currentUser, id);
        setFlashInfo(req, "Training wurde ausgeblendet.");
    }

    private void handleDelete(HttpServletRequest req, User currentUser) throws SQLException {
        String id = req.getParameter("id");
        trainingService.deleteTrainingAsAdmin(currentUser, id);
        setFlashInfo(req, "Training wurde gelöscht.");
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

    private void applyFlash(FlashMessageBean flashMessageBean, AdminTrainingsBean adminTrainingsBean) {
        if (flashMessageBean == null || adminTrainingsBean == null) {
            return;
        }
        if (adminTrainingsBean.getError() == null || adminTrainingsBean.getError().isBlank()) {
            adminTrainingsBean.setError(flashMessageBean.getError());
        }
        adminTrainingsBean.setInfo(flashMessageBean.getInfo());
    }
}
