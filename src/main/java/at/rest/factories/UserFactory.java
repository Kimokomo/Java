package at.rest.factories;

import at.rest.models.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserFactory {
    public User createFromGoogle(String email, String googleId, String name) {
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setUsername(email);
        user.setRole("user");
        user.setConfirmed(true);
        user.setPasswordHash("GOOGLE_LOGIN_HASH_PASSWORD");
        user.setPassword("GOOGLE_LOGIN_PASSWORD");
        return user;
    }
}
