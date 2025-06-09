package at.rest.factories;

import at.rest.models.CustomSecurityContext;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Date;

public class SecurityContextFactory {

    public static SecurityContext create(String username, String role, SecurityContext originalContext, Date expiration) {
        boolean isSecure = originalContext != null && originalContext.isSecure();
        return new CustomSecurityContext(username, role, isSecure, expiration);
    }
}
