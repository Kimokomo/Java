package at.rest.servcie;

import at.rest.model.User;
import at.rest.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(User user, String passwordInput) {
        return BCrypt.checkpw(passwordInput, user.getPasswordHash());
    }

    public boolean checkPasswordEasy(User user, String passwordInput) {
        // Einfacher Vergleich
        return user.getPassword().equals(passwordInput);
    }

    public User registerUser(String username, String passwordInput, String role) {
        String hashed = BCrypt.hashpw(passwordInput, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordInput);
        user.setPasswordHash(hashed);
        user.setRole(role);
        userRepository.save(user);
        return user;
    }
}
