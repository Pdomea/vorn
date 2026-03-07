package app.web;

import java.io.IOException;
import java.sql.SQLException;

import app.beans.BeanAttributes;
import app.beans.LoginBean;
import app.dao.UserDao;
import app.model.User;
import app.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private transient AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService(new UserDao());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginBean loginBean = new LoginBean();
        Object startupOk = getServletContext().getAttribute("app.startup.ok");
        if (Boolean.FALSE.equals(startupOk)) {
            loginBean.setError("Datenbank-Initialisierung fehlgeschlagen. Bitte DB-Konfiguration prüfen.");
        }
        String redirect = req.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            loginBean.setRedirect(redirect);
        }
        moveLegacyInfo(req, loginBean);
        forwardWithBean(req, resp, loginBean);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");
        LoginBean loginBean = createBean(email, redirect);

        try {
            User user = authService.login(email, password);
            if (user == null) {
                loginBean.setError("Login fehlgeschlagen. Bitte Eingaben prüfen.");
                forwardWithBean(req, resp, loginBean);
                return;
            }

            HttpSession existingSession = req.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            session.setMaxInactiveInterval(30 * 60);
            String target = "/home";
            if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
                target = redirect;
            }
            resp.sendRedirect(req.getContextPath() + target);
        } catch (IllegalArgumentException ex) {
            loginBean.setError(ex.getMessage());
            forwardWithBean(req, resp, loginBean);
        } catch (SQLException ex) {
            loginBean.setError("Login derzeit nicht verfügbar (DB nicht erreichbar).");
            forwardWithBean(req, resp, loginBean);
        }
    }

    private LoginBean createBean(String email, String redirect) {
        LoginBean loginBean = new LoginBean();
        loginBean.setEmail(email);
        if (redirect != null && !redirect.isBlank()) {
            loginBean.setRedirect(redirect);
        }
        return loginBean;
    }

    private void forwardWithBean(HttpServletRequest req, HttpServletResponse resp, LoginBean loginBean)
            throws ServletException, IOException {
        req.setAttribute(BeanAttributes.forClass(LoginBean.class), loginBean);
        req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
    }

    private void moveLegacyInfo(HttpServletRequest req, LoginBean loginBean) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object info = session.getAttribute("info");
        if (info != null) {
            loginBean.setInfo(info.toString());
            session.removeAttribute("info");
        }
    }
}
