package app.web;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.dao.ConnectionFactory;
import app.dao.UserDao;
import app.service.AuthService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String dbUrl = context.getInitParameter("db.url");
        String dbUser = context.getInitParameter("db.user");
        String dbPassword = context.getInitParameter("db.password");
        boolean dbInitEnabled = Boolean.parseBoolean(defaultValue(context.getInitParameter("db.init.enabled"), "true"));

        ConnectionFactory.configure(dbUrl, dbUser, dbPassword);
        context.setAttribute("db.url", dbUrl);
        context.setAttribute("db.user", dbUser);

        if (!dbInitEnabled) {
            LOGGER.info("DB init disabled by context parameter.");
            return;
        }

        String adminEmail = defaultValue(context.getInitParameter("admin.email"), "admin@vorn.local");
        String adminDisplayName = defaultValue(context.getInitParameter("admin.displayName"), "System Admin");
        String adminPassword = defaultValue(context.getInitParameter("admin.password"), "Admin123!");

        AuthService authService = new AuthService(new UserDao());
        try {
            authService.initializeSchema();
            boolean seeded = authService.seedAdminIfMissing(adminEmail, adminDisplayName, adminPassword);
            context.setAttribute("app.startup.ok", Boolean.TRUE);
            LOGGER.info("Startup DB init finished. adminSeeded=" + seeded);
        } catch (SQLException | IllegalArgumentException ex) {
            context.setAttribute("app.startup.ok", Boolean.FALSE);
            context.setAttribute("app.startup.error", ex.getMessage());
            LOGGER.log(Level.SEVERE, "Startup DB init failed.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
