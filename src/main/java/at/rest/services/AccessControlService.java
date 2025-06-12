package at.rest.services;

import at.rest.enums.Role;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.Set;

@ApplicationScoped
public class AccessControlService {

    public boolean isPublicPath(String path) {
        return Set.of(
                "", "/hello", "/auth/login", "/auth/register", "/auth/confirm", "/auth/google", "/auth/forgot", "/auth/reset-password"
        ).contains(path);
    }

    public boolean isAccessAllowed(String path, Role role) {
        if (path.contains("/member")) {
            return hasRole(role, Role.USER, Role.ADMIN, Role.SUPERADMIN);
        } else if (path.startsWith("/admin")) {
            return hasRole(role, Role.ADMIN, Role.SUPERADMIN);
        } else if (path.contains("/superadmin")) {
            return hasRole(role, Role.SUPERADMIN);
        }
        return true;
    }


    private boolean hasRole(Role actualRole, Role... allowedRoles) {
        return Arrays.asList(allowedRoles).contains(actualRole);
    }
}
