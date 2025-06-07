package at.rest.controller;

import at.rest.dtos.CredentialsDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.DuplicateException;
import at.rest.exceptions.ValidationException;
import at.rest.mapper.UserMapper;
import at.rest.model.User;
import at.rest.requests.GoogleTokenRequest;
import at.rest.responses.JwtResponse;
import at.rest.responses.MessageResponse;
import at.rest.responses.UserInfoResponse;
import at.rest.servcie.JwtService;
import at.rest.servcie.MailService;
import at.rest.servcie.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    private static final String JWT_SECRET = System.getProperty("jwt.secret.key");
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    private static final String GOOGLE_CLIENT_ID = System.getProperty("google.client.id");

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    // --- LOGIN --- //
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

    // --- LOGIN WITH GOOGLE --- //
    @POST
    @Path("/google")
    public Response loginWithGoogle(GoogleTokenRequest request) {
        String idToken = request.getIdToken();

        // 1. Google Token validieren
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        // 2. Nutzer suchen oder neu anlegen
        User user = userService.findOrCreateUser(email, googleId, name);

        // 3. Eigenes JWT erstellen
        String jwt = jwtService.createJwtForUser(user);

        return Response.ok(new JwtResponse(jwt)).build();
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            // Google Public Keys (cached)
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

    // --- REGISTER --- //
    @POST
    @Path("/register")
    public Response register(RegisterUserDTO dto) {

        try {
            userService.registerNewUser(dto);

            return Response.status(Response.Status.CREATED)
                    .entity(new MessageResponse("Registration successful. Please confirm your email address."))
                    .build();
        } catch (ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        } catch (DuplicateException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred during registration"))
                    .build();
        }
    }

    // --- EMAIL BESTÄTIGUNG //
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
