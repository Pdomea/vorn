package app.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/admin", "/admin/console"})
public class AdminConsoleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/admin".equals(req.getServletPath())) {
            resp.sendRedirect(req.getContextPath() + "/admin/console");
            return;
        }
        req.getRequestDispatcher("/jsp/admin/adminConsole.jsp").forward(req, resp);
    }
}
