package at.rest.factory;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class SecurityContextFactory {

    public static SecurityContext create(String username, String role, SecurityContext originalContext) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String roleName) {
                return role.equals(roleName);
            }

            @Override
            public boolean isSecure() {
                return originalContext != null && originalContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        };
    }
}
