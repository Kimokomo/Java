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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByConfirmationToken(String token) {
        return userRepository.findByConfirmationToken(token);
    }

    public void save(User user) {
        userRepository.save(user);  // nutzt persist oder merge je nach Zustand
    }

    public void update(User user) {
        userRepository.save(user);  // gleiche Methode, da save() beides abdeckt
    }

    public boolean checkPassword(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
    }

    public boolean isPasswordValid(String password) {
        if (password == null) return false;

        // Mindestlänge 8 Zeichen
        if (password.length() < 8) return false;

        // Muss mindestens einen Großbuchstaben, Kleinbuchstaben und Zahl enthalten
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpper && hasLower && hasDigit;
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
