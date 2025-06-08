package at.rest.filters;

import at.rest.factories.SecurityContextFactory;
import at.rest.services.AccessControlService;
import at.rest.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(JWTAuthFilter.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    JwtService jwtService;

    @Inject
    AccessControlService accessControlService;


    @Override
    public void filter(ContainerRequestContext requestContext) {

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        // OPTIONS-Anfragen durchlassen (CORS Preflight) und öffentliche Pfade erlauben (Whitelist)
        if ("OPTIONS".equalsIgnoreCase(method) || accessControlService.isPublicPath(path)) {
            return;
        }

        // Authorization Header prüfen
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (!isBearerTokenPresent(authHeader)) {
            logger.warning("Missing or invalid Authorization header for path: " + path);
            abort(requestContext);
            return;
        }

        // Token extrahieren
        String token = extractToken(authHeader);

        Claims claims;
        try {
            claims = jwtService.parseAndValidateToken(token);

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username == null || role == null) {
                logger.warning("JWT token missing required claims");
                abort(requestContext);
                return;
            }

            // SecurityContext setzen, damit Benutzerinfos später verfügbar sind
            requestContext.setSecurityContext(
                    SecurityContextFactory.create(username, role, requestContext.getSecurityContext())
            );

            // Rollenbasierter Zugriff prüfen
            if (!accessControlService.isAccessAllowed(path, role)) {
                logger.warning("Access denied for user: " + username + " with role: " + role + " on path: " + path);
                abort(requestContext);
            } else {
                logger.fine("Access granted for user: " + username + " with role: " + role + " on path: " + path);
            }
        } catch (JwtException e) {
            logger.log(Level.WARNING, "JWT validation failed: " + e.getMessage(), e);
            abort(requestContext);
        }
    }

    private boolean isBearerTokenPresent(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private void abort(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}