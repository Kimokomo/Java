package at.rest.factories;

import at.rest.enums.Role;
import at.rest.models.entities.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class GoogleUserFactory {
    public User createFromGoogle(GoogleIdToken.Payload payload) {
        User user = new User();

        user.setEmail(payload.getEmail());
        user.setGoogleId(payload.getSubject());
        user.setUsername(payload.getEmail());
        user.setFirstname((String) payload.get("given_name"));
        user.setLastname((String) payload.get("family_name"));
        user.setGoogleConfirmed((Boolean) payload.get("email_verified"));

        user.setRole(Role.getDefault());
        user.setPasswordHash("GOOGLE_LOGIN_HASH_PASSWORD");

        user.setTstamp(LocalDateTime.now());

        return user;
    }
}
