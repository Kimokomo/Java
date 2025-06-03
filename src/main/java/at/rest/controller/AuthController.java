package at.rest.controller;

import at.rest.model.*;
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
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    private static final String SECRET_KEY = "mein-super-geheimer-key-1234567890123456";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;

    // --- LOGIN ---
    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        Optional<User> userOpt = userService.findByUsername(credentials.getUsername());

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        User dbUser = userOpt.get();

        if (!userService.checkPassword(credentials.getPassword(), dbUser.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        if (!dbUser.isConfirmed()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Please confirm your email before logging in.")
                    .build();
        }

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
    public Response register(User user) {

        // DTO in Entity umwandeln
        // User user = UserMapper.INSTANCE.toEntity(dto);

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username already exists")
                    .build();
        }

        // findByEmailAdresse auch machen

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
    @Path("/userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@Context SecurityContext securityContext) {

        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String username = securityContext.getUserPrincipal().getName();

        String role;
        if (securityContext.isUserInRole("superAdmin")) {
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
