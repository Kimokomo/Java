package at.rest.servcie;

import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.DuplicateException;
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


    public Optional<User> findByConfirmationToken(String token) {
        return userRepository.findByConfirmationToken(token);
    }

    public void update(User user) {
        userRepository.save(user);  // gleiche Methode, da save() beides abdeckt
    }

    public boolean checkPassword(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
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
        checkDuplicateUser(dto);

        // DTO in User umwandeln
        User user = userMapper.toEntity(dto);

        // Passwort hashen und setzen
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        user.setPassword(null); // nicht speichern!!

        // Rolle, Token, Bestätigung setzen
        String token = UUID.randomUUID().toString();
        user.setConfirmed(false);
        user.setConfirmationToken(token);
        user.setRole("user");

        // Speichern
        userRepository.save(user);

        // Bestätigungsmail senden
        mailService.sendConfirmationEmail(user.getEmail(), token);

        return user;
    }

    public void checkDuplicateUser(RegisterUserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateException("username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateException("email already exists");
        }
    }

}
