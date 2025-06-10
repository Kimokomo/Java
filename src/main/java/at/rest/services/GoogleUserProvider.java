package at.rest.services;

import at.rest.factories.UserFactory;
import at.rest.models.User;
import at.rest.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GoogleUserProvider {

    @Inject
    UserRepository userRepository;
    @Inject
    UserFactory userFactory;

    public User findOrCreateGoogleUser(GoogleIdToken.Payload payload) {

        User user = userRepository.findByGoogleId(payload.getSubject()).orElse(null);
        if (user != null) {
            return user;
        }

        user = userRepository.findByEmail(payload.getEmail()).orElse(null);
        if (user != null) {
            return user;
        }

        User newUser = userFactory.createFromGoogle(payload);

        userRepository.saveOrUpdate(newUser);
        userRepository.saveOrUpdate(newUser);
        return newUser;
    }
}
