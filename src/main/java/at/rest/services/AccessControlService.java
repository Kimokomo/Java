package at.rest.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Set;

@ApplicationScoped
public class AccessControlService {

    public boolean isPublicPath(String path) {
        return Set.of(
                "", "/auth/login", "/auth/register", "/auth/confirm", "/auth/google", "/hello"
        ).contains(path);
    }

    public boolean isAccessAllowed(String path, String role) {
        if (path.contains("/member")) {
            return hasRole(role, "user", "admin", "superadmin");
        } else if (path.startsWith("admin")) {
            return hasRole(role, "admin", "superadmin");
        } else if (path.contains("/superadmin")) {
            return hasRole(role, "superadmin");
        }
        return true;
    }

    private boolean hasRole(String actualRole, String... allowedRoles) {
        return Arrays.asList(allowedRoles).contains(actualRole);
    }
}
