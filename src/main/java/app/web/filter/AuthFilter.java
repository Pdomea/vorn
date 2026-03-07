package app.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import app.model.User;
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
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object sessionUser = session != null ? session.getAttribute("currentUser") : null;
        if (sessionUser instanceof User) {
            chain.doFilter(request, response);
            return;
        }

        String redirectPath = path;
        if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
            redirectPath = redirectPath + "?" + req.getQueryString();
        }
        String redirectTarget = URLEncoder.encode(redirectPath, StandardCharsets.UTF_8);
        resp.sendRedirect(contextPath + "/login?redirect=" + redirectTarget);
    }

    @Override
    public void destroy() {
        // no-op
    }

    private boolean isPublicPath(String path) {
        return "/".equals(path)
                || "/index.jsp".equals(path)
                || "/login".equals(path)
                || "/register".equals(path)
                || "/logout".equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/favicon")
                || path.startsWith("/META-INF/");
    }
}
