package at.rest.factories;

import at.rest.models.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserFactory {
    public User createFromGoogle(GoogleIdToken.Payload payload) {
        User user = new User();

        user.setEmail(payload.getEmail());
        user.setGoogleId(payload.getSubject());
        user.setUsername(payload.getEmail());
        user.setFirstname((String) payload.get("given_name"));
        user.setLastname((String) payload.get("family_name"));
        user.setConfirmed((Boolean) payload.get("email_verified"));

        user.setRole("user");
        user.setPasswordHash("GOOGLE_LOGIN_HASH_PASSWORD");
        user.setPassword("GOOGLE_LOGIN_PASSWORD");

        return user;
    }
}
