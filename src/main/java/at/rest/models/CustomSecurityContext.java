package at.rest.models;

import at.rest.enums.Role;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.Date;

public class CustomSecurityContext implements SecurityContext {

    private final String username;
    private final Role role;
    private final boolean isSecure;
    private final Date expiration;

    public CustomSecurityContext(String username, Role role, boolean isSecure, Date expiration) {
        this.username = username;
        this.role = role;
        this.isSecure = isSecure;
        this.expiration = expiration;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String roleName) {
        try {
            return role.equals(Role.valueOf(roleName));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";

    }

    public Date getTokenExpiration() {
        return expiration;
    }
}
