package app.web.filter;

import java.io.IOException;
import java.sql.SQLException;

import app.dao.WorkoutSessionDao;
import app.model.User;
import app.model.WorkoutSession;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class ActiveSessionGuardFilter implements Filter {
    private WorkoutSessionDao workoutSessionDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        workoutSessionDao = new WorkoutSessionDao();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (isStaticPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        WorkoutSession activeSession;
        try {
            activeSession = workoutSessionDao.findActiveByUserId(currentUser.getId());
        } catch (SQLException ex) {
            throw new ServletException("Active session could not be checked.", ex);
        }

        if (activeSession == null) {
            chain.doFilter(request, response);
            return;
        }

        if (isAllowedPathDuringActiveSession(path, req, activeSession)) {
            chain.doFilter(request, response);
            return;
        }

        resp.sendRedirect(contextPath + "/session/track?id=" + activeSession.getId());
    }

    @Override
    public void destroy() {
        // no-op
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

    private boolean isAllowedPathDuringActiveSession(String path, HttpServletRequest req, WorkoutSession activeSession) {
        if ("/logout".equals(path)) {
            return true;
        }
        if ("/session/finish".equals(path)
                || "/session/discard".equals(path)
                || "/session/track/save".equals(path)
                || "/session/track/swap".equals(path)) {
            return true;
        }
        if ("/session/track".equals(path)) {
            String idRaw = req.getParameter("id");
            if (idRaw == null || idRaw.isBlank()) {
                return false;
            }
            try {
                long requestedId = Long.parseLong(idRaw);
                return requestedId == activeSession.getId();
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return false;
    }

    private boolean isStaticPath(String path) {
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/favicon")
                || path.startsWith("/META-INF/");
    }
}
