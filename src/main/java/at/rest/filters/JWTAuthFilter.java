package at.rest.filters;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthFilter implements ContainerRequestFilter {

    // Achtung: Mindestens 256-bit (32-Byte) lang bei HS256!
    private static final String SECRET_KEY = "mein-super-geheimer-key-1234567890123456";

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();

        // 1. OPTIONS-Anfragen durchlassen (Preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        // 2. Öffentliche Pfade erlauben (Whitelist)
        if (path.equals("/auth/login") || path.equals("") || path.equals("/hello")) {
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
            // 4. Token prüfen
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token);

            // Optional: Benutzername oder Rollen auslesen
            String username = claimsJws.getBody().getSubject();
            Date expiration = claimsJws.getBody().getExpiration();

            // Optional: Ablauf prüfen (normalerweise macht das jjwt automatisch)
            if (expiration.before(new Date())) {
                abort(requestContext);
            }

        } catch (JwtException e) {
            // Ungültiges oder manipuliertes Token
            abort(requestContext);
        }
    }

    private void abort(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
