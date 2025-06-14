package at.rest.services.user;

import at.rest.dtos.ForgotPassDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.dtos.ResetPasswordDTO;
import at.rest.enums.Role;
import at.rest.exceptions.AuthenticationException;
import at.rest.exceptions.DuplicateException;
import at.rest.mappers.UserMapper;
import at.rest.models.context.CustomSecurityContext;
import at.rest.models.entities.User;
import at.rest.repositories.UserRepository;
import at.rest.responses.UserInfoResponse;
import at.rest.services.auth.JwtService;
import at.rest.services.mail.MailService;
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
        Role role = resolveRole(securityContext);

        Date tokenExpiration = null;

        if (securityContext instanceof CustomSecurityContext customCtx) {
            tokenExpiration = customCtx.getTokenExpiration();
        }

        return new UserInfoResponse(username, role.name().toLowerCase(), tokenExpiration);
    }


    private Role resolveRole(SecurityContext securityContext) {
        if (securityContext.isUserInRole("SUPERADMIN")) return Role.SUPERADMIN;
        if (securityContext.isUserInRole("ADMIN")) return Role.ADMIN;
        if (securityContext.isUserInRole("USER")) return Role.USER;
        return Role.UNKNOWN;
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
        user.setRole(Role.getDefault());

        user.setTstamp(LocalDateTime.now());

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

    public void forgotPassword(ForgotPassDTO dto) {
        String emailOrUsername = dto.getEmailOrUsername();

        Optional<User> userOpt = mailService.isValidEmailSyntax(emailOrUsername)
                ? userRepository.findByEmail(emailOrUsername)
                : userRepository.findByUsername(emailOrUsername);

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

    public void resetPassword(ResetPasswordDTO dto) {

        // Token, neues Passwort und Bestätigung extrahieren
        String token = dto.getToken();
        String newPassword = dto.getNewPassword();
        String confirmNewPassword = dto.getConfirmNewPassword();

        // Passwort bestätigen kontrolle
        if (!newPassword.equals(confirmNewPassword)) {
            throw new AuthenticationException("Passwörter stimmen nicht überein");
        }

        // Benutzer mit gültigem Token finden
        User user = userRepository.findByForgotPasswordToken(token)
                .orElseThrow(() -> new AuthenticationException("Ungültiger oder abgelaufener Token"));

        // Token-Ablaufzeit prüfen
        if (user.getForgotPasswordTokenExpiry() == null || user.getForgotPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Token ist abgelaufen");
        }

        // Neues Passwort hashen und setzen
        String hashedPassword = BCrypt.hashpw(dto.getConfirmNewPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        user.setPassword(null);

        // Token invalidieren
        user.setForgotPasswordToken(null);
        user.setForgotPasswordTokenExpiry(null);

        // Speichern
        userRepository.update(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
