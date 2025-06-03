package at.rest.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.Principal;
import java.util.Date;
import java.util.Properties;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthFilter implements ContainerRequestFilter {

    private static final Key SIGNING_KEY;

    static {
        String secretKey = loadSecretKey();
        SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private static String loadSecretKey() {
        Properties properties = new Properties();
        try (InputStream is = JWTAuthFilter.class.getClassLoader().getResourceAsStream("jwt.properties")) {
            if (is == null) throw new RuntimeException("jwt.properties nicht gefunden");
            properties.load(is);
            return properties.getProperty("jwt.secret.key");
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden des Secret Keys", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();

        // 1. OPTIONS-Anfragen durchlassen (CORS Preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        // 2. Öffentliche Pfade erlauben (Whitelist)
        if (path.equals("/auth/confirm") || path.equals("/auth/login") || path.equals("") || path.equals("/hello") || path.equals("/auth/register")) {
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
            // 4. Token prüfen Claims lesen
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


            // Pfad-Rollen-Prüfung
            if (path.startsWith("admin") && !(role.equals("admin") || role.equals("superAdmin"))) {
                abort(requestContext);
                return;
            }

            // Nur Benutzer mit Rolle "superAdmin" dürfen Pfade unter /superadmin aufrufen.
            if (path.startsWith("superadmin") && !role.equals("superAdmin")) {
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
