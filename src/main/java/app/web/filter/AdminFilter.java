package app.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import app.model.User;
import app.model.UserRole;
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

@WebFilter("/admin/*")
public class AdminFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        Object sessionUser = session != null ? session.getAttribute("currentUser") : null;

        if (!(sessionUser instanceof User user)) {
            String path = req.getRequestURI().substring(req.getContextPath().length());
            if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
                path = path + "?" + req.getQueryString();
            }
            String redirectTarget = URLEncoder.encode(path, StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + redirectTarget);
            return;
        }

        if (user.getRole() != UserRole.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Nur ADMIN darf diesen Bereich aufrufen.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
