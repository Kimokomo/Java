package at.rest.controllers;

import at.rest.dtos.CredentialsDTO;
import at.rest.dtos.ForgotPassDTO;
import at.rest.dtos.RegisterUserDTO;
import at.rest.dtos.ResetPasswordDTO;
import at.rest.exceptions.AuthenticationException;
import at.rest.exceptions.DuplicateException;
import at.rest.models.User;
import at.rest.requests.GoogleTokenRequest;
import at.rest.responses.JwtResponse;
import at.rest.responses.MessageResponse;
import at.rest.responses.UserInfoResponse;
import at.rest.services.GoogleAuthService;
import at.rest.services.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
                    .entity(new MessageResponse("Die Anmeldung über Google war leider nicht erfolgreich.")).build();
        }
    }

    // --- REGISTER --- //
    @POST
    @Path("/register")
    public Response register(@Valid RegisterUserDTO dto) {
        try {
            User user = userService.registerNewUser(dto);
            return Response.ok().entity(new MessageResponse("Registrierung erfolgreich. Bitte bestätigen Sie Ihre E-Mail-Adresse.")).build();
        } catch (DuplicateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("Während der Registrierung ist ein Fehler aufgetreten."))
                    .build();
        }
    }

    // --- EMAIL BESTÄTIGUNG //
    @GET
    @Path("/confirm")
    public Response confirmEmail(@QueryParam("token") String token) {
        try {
            userService.confirmEmail(token);
            return Response.ok(new MessageResponse("E-Mail bestätigt. Sie können sich jetzt anmelden.")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        }
    }

    // --- PASSWORT VERGESSEN --- //
    @POST
    @Path("/forgot")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(@Valid ForgotPassDTO dto) {
        try {
            userService.forgotPassword(dto);
            return Response.ok().entity(new MessageResponse("Wir haben dir eine E-Mail geschickt, mit der du dein Passwort zurücksetzen kannst.")).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        }
    }

    // --- PASSWORT ZURÜCKSETZEN --- //
    @POST
    @Path("/reset-password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPassword(@Valid ResetPasswordDTO dto) {
        try {
            userService.resetPassword(dto);
            return Response.ok(new MessageResponse("Ihr Passwort wurde erfolgreich zurückgesetzt.")).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(e.getMessage()))
                    .build();
        }
    }

    // --- GET USERINFO FROM SECURITY CONTEXT //
    @GET
    @Path("member/userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@Context SecurityContext securityContext) {
        try {
            UserInfoResponse userInfo = userService.getUserInfo(securityContext);
            return Response.ok(userInfo).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
