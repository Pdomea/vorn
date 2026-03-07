package app.web;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/session/start")
public class SessionStartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession(true).setAttribute("flash.info", "Bitte starte Trainings nur über die Plan-Details.");
        resp.sendRedirect(req.getContextPath() + "/plan/details");
    }
}
