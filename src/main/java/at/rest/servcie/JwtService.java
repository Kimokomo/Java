package at.rest.servcie;

import at.rest.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.Key;
import java.util.Date;

@ApplicationScoped
public class JwtService {

    private static final String JWT_SECRET = System.getProperty("jwt.secret.key");
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    // Token Gültigkeit: 1 Stunde
    private static final long EXPIRATION_TIME_MS = 3600_000L;

    public String createJwtForUser(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
