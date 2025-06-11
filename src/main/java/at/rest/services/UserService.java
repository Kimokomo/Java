package at.rest.services;

import at.rest.dtos.ForgotPassDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.AuthenticationException;
import at.rest.exceptions.DuplicateException;
import at.rest.mappers.UserMapper;
import at.rest.models.CustomSecurityContext;
import at.rest.models.User;
import at.rest.repositories.UserRepository;
import at.rest.responses.UserInfoResponse;
import at.rest.validators.UserValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Date;
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

    @Inject
    JwtService jwtService;

    public UserInfoResponse getUserInfo(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new AuthenticationException("Not authenticated");
        }

        String username = securityContext.getUserPrincipal().getName();
        String role = resolveRole(securityContext);

        Date tokenExpiration = null;

        if (securityContext instanceof CustomSecurityContext customCtx) {
            tokenExpiration = customCtx.getTokenExpiration();
        }

        return new UserInfoResponse(username, role, tokenExpiration);
    }

    private String resolveRole(SecurityContext securityContext) {
        if (securityContext.isUserInRole("superadmin")) return "superadmin";
        if (securityContext.isUserInRole("admin")) return "admin";
        if (securityContext.isUserInRole("user")) return "user";
        return "unknown";
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

    public void forgotPass(ForgotPassDTO dto) {
        String emailOrUsername = dto.getEmailOrUsername();

        Optional<User> userOpt;

        if (mailService.isValidEmailSyntax(emailOrUsername)) {
            userOpt = userRepository.findByEmail(emailOrUsername);
        } else {
            userOpt = userRepository.findByUsername(emailOrUsername);
        }

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("E-Mail oder Benutzername nicht vorhanden");
        }

        User user = userOpt.get();

        // Token generieren
        String token = UUID.randomUUID().toString();

        // Token + Ablauf speichern
        user.setForgotPasswordToken(token);
        user.setForgotPasswordTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.update(user);

        mailService.sendForgotPasswordEmail(user.getEmail(), token);
    }
}
