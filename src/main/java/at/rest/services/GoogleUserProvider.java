package at.rest.services;

import at.rest.factories.UserFactory;
import at.rest.models.User;
import at.rest.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GoogleUserProvider {

    @Inject
    UserRepository userRepository;
    @Inject
    UserFactory userFactory;

    public User findOrCreateGoogleUser(String email, String googleId, String name) {

        //Suche nach Google ID
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        if (user != null) {
            return user;
        }

        user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return user;
        }

        User newUser = userFactory.createFromGoogle(email, googleId, name);

        userRepository.saveOrUpdate(newUser);
        userRepository.saveOrUpdate(newUser);
        return newUser;
    }
}
