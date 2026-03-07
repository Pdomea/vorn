package app.web;

import java.io.IOException;
import java.sql.SQLException;

import app.beans.BeanAttributes;
import app.beans.RegisterBean;
import app.dao.UserDao;
import app.model.User;
import app.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private transient AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService(new UserDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardWithBean(req, resp, new RegisterBean());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String displayName = req.getParameter("displayName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String passwordConfirm = req.getParameter("passwordConfirm");
        RegisterBean registerBean = createBean(displayName, email);

        if (password == null || !password.equals(passwordConfirm)) {
            registerBean.setError("Passwort und Passwort-Wiederholung stimmen nicht ueberein.");
            forwardWithBean(req, resp, registerBean);
            return;
        }

        try {
            User user = authService.register(email, displayName, password);
            req.getSession(true).setAttribute("currentUser", user);
            req.getSession(true).setAttribute("flash.info", "Registrierung erfolgreich. Bitte zuerst einen Plan wählen.");
            resp.sendRedirect(req.getContextPath() + "/program/select");
        } catch (IllegalArgumentException ex) {
            registerBean.setError(ex.getMessage());
            forwardWithBean(req, resp, registerBean);
        } catch (SQLException ex) {
            registerBean.setError("Registrierung derzeit nicht verfuegbar (DB nicht erreichbar).");
            forwardWithBean(req, resp, registerBean);
        }
    }

    private RegisterBean createBean(String displayName, String email) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.setDisplayName(displayName);
        registerBean.setEmail(email);
        return registerBean;
    }

    private void forwardWithBean(HttpServletRequest req, HttpServletResponse resp, RegisterBean registerBean)
            throws ServletException, IOException {
        req.setAttribute(BeanAttributes.forClass(RegisterBean.class), registerBean);
        req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
    }
}
