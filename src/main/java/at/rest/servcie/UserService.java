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

    public boolean checkPassword(User user) {
        return BCrypt.checkpw(user.getPassword(), user.getPasswordHash());
    }

    public User registerUser(String username, String passwordInput, String role, String email) {
        String hashed = BCrypt.hashpw(passwordInput, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordInput);
        user.setPasswordHash(hashed);
        user.setRole(role);
        user.setEmail(email);
        userRepository.save(user);
        return user;
    }
}
