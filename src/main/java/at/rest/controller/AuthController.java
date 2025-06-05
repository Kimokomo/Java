package at.rest.controller;

import at.rest.dtos.CredentialsDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.mapper.UserMapper;
import at.rest.model.User;
import at.rest.responses.MessageResponse;
import at.rest.responses.UserInfoResponse;
import at.rest.servcie.MailService;
import at.rest.servcie.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.security.Key;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    private static final String JWT_SECRET = System.getProperty("jwt.secret.key");
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;

    @Inject
    UserMapper userMapper;

    // --- LOGIN ---
    @POST
    @Path("/login")
    public Response login(CredentialsDTO credentialsDTO) {
        Optional<User> userOpt = userService.findByUsername(credentialsDTO.getUsername());

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        User dbUser = userOpt.get();

        if (!userService.checkPassword(credentialsDTO.getPassword(), dbUser.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        if (!dbUser.isConfirmed()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Please confirm your email before logging in.")
                    .build();
        }

        // Token erstellen & signieren
        String jwt = Jwts.builder()
                .setSubject(dbUser.getUsername())
                .claim("role", dbUser.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h Gültigkeit
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        return Response.ok()
                .entity("{\"token\":\"" + jwt + "\"}")
                .build();
    }

    // --- REGISTER ---
    @POST
    @Path("/register")
    public Response register(RegisterUserDTO dto) {

        // DTO in Entity umwandeln
        User user = userMapper.toEntity(dto);

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse("username already exists"))
                    .build();
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse("email already exists"))
                    .build();
        }

        if (!userService.isPasswordValid(user.getPassword())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Passwort zu schwach – mindestens 8 Zeichen, ein Großbuchstabe, ein Kleinbuchstabe und eine Zahl erforderlich."))
                    .build();
        }

        if (user.getFirstname() == null || user.getFirstname().trim().length() < 2) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Vorname muss mindestens 2 Zeichen lang sein."))
                    .build();
        }

        if (user.getLastname() == null || user.getLastname().trim().length() < 2) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Nachname muss mindestens 2 Zeichen lang sein."))
                    .build();
        }

        if (user.getAge() == null || user.getAge() < 18) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Alter muss mindestens 18 Jahre betragen."))
                    .build();
        }

        if (user.getDateOfBirth() == null || user.getDateOfBirth().isAfter(LocalDate.now())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Geburtsdatum darf nicht leer sein oder in der Zukunft liegen."))
                    .build();
        }

        // Altersangabe und Geburtsdatum abgleichen (Konsistenzprüfung)
        int calculatedAge = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
        if (calculatedAge != user.getAge()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Alter stimmt nicht mit dem Geburtsdatum überein."))
                    .build();
        }


        String token = UUID.randomUUID().toString();
        user.setEmail(user.getEmail());
        user.setUsername(user.getUsername());
        user.setPassword(user.getPassword());
        user.setAge(user.getAge());
        user.setDateOfBirth(user.getDateOfBirth());
        user.setRole("user");
        user.setPasswordHash(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setConfirmed(false);
        user.setConfirmationToken(token);

        userService.save(user);

        boolean emailSent = mailService.sendConfirmationEmail(user.getEmail(), token);

        if (!emailSent) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("Registrierung erfolgreich, aber E-Mail konnte nicht gesendet werden."))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(new MessageResponse("Registration successful. Please confirm your email address."))
                .build();
    }

    // --- EMAIL BESTÄTIGUNG ---
    @GET
    @Path("/confirm")
    public Response confirmEmail(@QueryParam("token") String token) {
        Optional<User> userOpt = userService.findByConfirmationToken(token);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid or expired confirmation token.")
                    .build();
        }

        User user = userOpt.get();
        user.setConfirmed(true);
        user.setConfirmationToken(null);
        userService.update(user);

        return Response.ok("Email confirmed. You can now log in.").build();
    }

    @GET
    @Path("member/userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@Context SecurityContext securityContext) {

        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String username = securityContext.getUserPrincipal().getName();

        String role;
        if (securityContext.isUserInRole("superadmin")) {
            role = "superadmin";
        } else if (securityContext.isUserInRole("admin")) {
            role = "admin";
        } else if (securityContext.isUserInRole("user")) {
            role = "user";
        } else {
            role = "unknown";
        }

        UserInfoResponse userInfo = new UserInfoResponse(username, role);

        return Response.ok(userInfo).build();
    }
}
