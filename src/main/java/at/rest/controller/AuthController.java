package at.rest.controller;

import at.rest.model.User;
import at.rest.model.UserInfoResponse;
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

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    private static final String SECRET_KEY = "mein-super-geheimer-key-1234567890123456";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Inject
    private UserService userService;

    @POST
    @Path("/login")
    public Response login(User credentials) {
        Optional<User> userOpt = userService.findByUsername(credentials.getUsername());

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        User user = userOpt.get();

        if (!userService.checkPassword(user)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String jwt = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h Gültigkeit
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        return Response.ok()
                .entity("{\"token\":\"" + jwt + "\"}")
                .build();
    }


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

        userService.registerUser(user.getUsername(), user.getPassword(), "user", user.getEmail());

        return Response.status(Response.Status.CREATED).build();
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
