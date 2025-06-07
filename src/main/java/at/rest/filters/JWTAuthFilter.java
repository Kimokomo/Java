package at.rest.filters;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.security.Key;
import java.security.Principal;
import java.util.Date;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthFilter implements ContainerRequestFilter {

    private static final String JWT_SECRET = System.getProperty("jwt.secret.key");
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();

        // 1. OPTIONS-Anfragen durchlassen (CORS Preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        // 2. Öffentliche Pfade erlauben (Whitelist)
        if (path.equals("/auth/confirm") || path.equals("/auth/login") || path.equals("") ||
                path.equals("/hello") || path.equals("/auth/register") || path.equals("/auth/google")) {
            return;
        }

        // 2. Authorization Header prüfen
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext);
            return;
        }

        // 3. Token extrahieren
        String token = authHeader.substring("Bearer ".length());

        try {
            // 4. Token validieren/prüfen & parsen
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            // Ablauf prüfen (jjwt macht das eigentlich automatisch, hier doppelt geprüft)
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                abort(requestContext);
                return;
            }

            // Rolle auslesen
            String role = claims.get("role", String.class);
            if (role == null) {
                abort(requestContext);
                return;
            }


            // Benutzername aus "sub" Claim auslesen
            final String username = claims.getSubject();

            // SecurityContext setzen, damit Benutzerinfos später verfügbar sind
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {
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
                    return currentSecurityContext != null && currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

            if (path.contains("/member") && !(role.equals("admin") || role.equals("superadmin") || role.equals("user"))) {
                abort(requestContext);
                return;
            }

            // Pfad-Rollen-Prüfung
            if (path.startsWith("admin") && !(role.equals("admin") || role.equals("superadmin"))) {
                abort(requestContext);
                return;
            }

            // Nur Benutzer mit Rolle "superadmin" dürfen Pfade unter /superadmin aufrufen.
            if (path.contains("/superadmin") && !role.equals("superadmin")) {
                abort(requestContext);
                return;
            }

        } catch (JwtException e) {
            abort(requestContext);
        }
    }

    private void abort(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
