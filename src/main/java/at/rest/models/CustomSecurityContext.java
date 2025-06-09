package at.rest.models;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.Date;

public class CustomSecurityContext implements SecurityContext {

    private final String username;
    private final String role;
    private final boolean isSecure;
    private final Date expiration;

    public CustomSecurityContext(String username, String role, boolean isSecure, Date expiration) {
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
        return role.equals(roleName);
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
