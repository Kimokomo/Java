package at.rest.servcie;

import at.rest.dtos.RegisterUserDTO;
import at.rest.mapper.UserMapper;
import at.rest.model.User;
import at.rest.repositories.UserRepository;
import at.rest.validators.UserValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserValidator userValidator;

    @Inject
    private UserMapper userMapper;

    @Inject
    MailService mailService;

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

    public User findOrCreateUser(String email, String googleId, String name) {
        // Suche User anhand Google-ID
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Falls nicht gefunden, versuche User anhand Email
        userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Wenn User noch nicht existiert, neu anlegen
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setGoogleId(googleId);
        newUser.setUsername(email);  // username ist dann email
        newUser.setRole("user");
        newUser.setConfirmed(true);  // Google-User sind automatisch bestätigt
        newUser.setPasswordHash("GOOGLE_LOGIN_HASH");
        newUser.setPassword("GOOGLE_LOGIN");

        userRepository.save(newUser);

        return newUser;
    }

    public User registerNewUser(RegisterUserDTO dto) {
        // Validierung zentral über Validator
        userValidator.validate(dto);

        // DTO in User umwandeln
        User user = userMapper.toEntity(dto);

        user.setEmail(user.getEmail());
        user.setUsername(user.getUsername());
        user.setAge(user.getAge());
        user.setDateOfBirth(user.getDateOfBirth());

        // Passwort hashen und setzen
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        user.setPassword(null); // nicht speichern!!

        // Rolle setzen
        user.setRole("user");

        // Bestätigungstoken erzeugen
        String token = UUID.randomUUID().toString();
        user.setConfirmed(false);
        user.setConfirmationToken(token);

        // Speichern
        userRepository.save(user);

        // Bestätigungsmail senden
        mailService.sendConfirmationEmail(user.getEmail(), token);

        return user;
    }
}
