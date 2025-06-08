package at.rest.services;

import at.rest.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.Key;
import java.util.Date;

@ApplicationScoped
public class JwtService {

    // Token Gültigkeit: 1 Stunde
    private static final long EXPIRATION_TIME_MS = 3600_000L;

    private static final String JWT_SECRET = System.getProperty("jwt.secret.key");
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    public String createJwtForUser(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidateToken(String token) throws JwtException {
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token);

        Claims claims = claimsJws.getBody();

        // Ablauf prüfen (optional, weil jwt das auch automatisch macht)
        Date expiration = claims.getExpiration();
        if (expiration == null || expiration.before(new Date())) {
            throw new JwtException("Token expired");
        }

        return claims;
    }
}
