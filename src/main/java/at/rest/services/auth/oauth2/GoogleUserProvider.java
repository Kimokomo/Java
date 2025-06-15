package at.rest.services.auth.oauth2;

import at.rest.factories.GoogleUserFactory;
import at.rest.models.entities.User;
import at.rest.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GoogleUserProvider {

    @Inject
    UserRepository userRepository;
    @Inject
    GoogleUserFactory googleUserFactory;

    public User findOrCreateGoogleUser(GoogleIdToken.Payload payload) {

        User user = userRepository.findByGoogleId(payload.getSubject()).orElse(null);
        if (user != null) {
            return user;
        }

        user = userRepository.findByEmail(payload.getEmail()).orElse(null);
        if (user != null) {
            return user;
        }

        User newUser = googleUserFactory.createFromGoogle(payload);

        userRepository.saveOrUpdate(newUser);
        return newUser;
    }
}
