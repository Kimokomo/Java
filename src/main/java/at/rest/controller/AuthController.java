package at.rest.controller;

import at.rest.dtos.CredentialsDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.AuthenticationException;
import at.rest.exceptions.DuplicateException;
import at.rest.exceptions.ValidationException;
import at.rest.model.User;
import at.rest.requests.GoogleTokenRequest;
import at.rest.responses.JwtResponse;
import at.rest.responses.MessageResponse;
import at.rest.responses.UserInfoResponse;
import at.rest.servcie.GoogleAuthService;
import at.rest.servcie.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    private UserService userService;

    @Inject
    GoogleAuthService googleAuthService;

    // --- LOGIN --- //
    @POST
    @Path("/login")
    public Response login(CredentialsDTO credentialsDTO) {
        try {
            String jwt = userService.login(credentialsDTO.getUsername(), credentialsDTO.getPassword());
            return Response.ok(new JwtResponse(jwt)).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    // --- LOGIN WITH GOOGLE --- //
    @POST
    @Path("/google")
    public Response loginWithGoogle(GoogleTokenRequest request) {
        try {
            String jwt = googleAuthService.loginWithGoogleToken(request.getIdToken());
            return Response.ok(new JwtResponse(jwt)).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new MessageResponse(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("Google Login fehlgeschlagen")).build();
        }
    }

    // --- REGISTER --- //
    @POST
    @Path("/register")
    public Response register(RegisterUserDTO dto) {

        try {
            User user = userService.registerNewUser(dto);
            return Response.status(Response.Status.CREATED)
                    .entity(new MessageResponse("Registration successful. Please confirm your email address."))
                    .build();
        } catch (ValidationException | DuplicateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
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
        try {
            userService.confirmEmail(token);
            return Response.ok(new MessageResponse("Email confirmed. You can now log in.")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        }
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
