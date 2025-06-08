package at.rest.servcie;

import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.AuthenticationException;
import at.rest.exceptions.DuplicateException;
import at.rest.mapper.UserMapper;
import at.rest.model.User;
import at.rest.repositories.UserRepository;
import at.rest.validators.UserValidator;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    private static final String GOOGLE_CLIENT_ID = System.getProperty("google.client.id");

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserValidator userValidator;

    @Inject
    private UserMapper userMapper;

    @Inject
    MailService mailService;

    @Inject
    JwtService jwtService;

    public String loginWithGoogleToken(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        if (payload == null) {
            throw new AuthenticationException("Ungültiges Google Token.");
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        User user = findOrCreateUser(email, googleId, name);

        return jwtService.createJwtForUser(user);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = new GsonFactory();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;
        } catch (Exception e) {
            return null;
        }
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

        userRepository.saveNew(newUser);

        return newUser;
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("username existiret nicht");
        }

        User user = userOpt.get();

        if (!checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Passwort ungültig");
        }

        if (!user.isConfirmed()) {
            throw new AuthenticationException("Please confirm your email before logging in.");
        }

        return jwtService.createJwtForUser(user);
    }

    public boolean checkPassword(String rawPassword, String passwordHash) {
        return BCrypt.checkpw(rawPassword, passwordHash);
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
        userRepository.saveNew(user);

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

    public void confirmEmail(String token) {
        Optional<User> userOpt = userRepository.findByConfirmationToken(token);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired confirmation token.");
        }

        User user = userOpt.get();
        user.setConfirmed(true);
        user.setConfirmationToken(null);
        userRepository.update(user);
    }
}
