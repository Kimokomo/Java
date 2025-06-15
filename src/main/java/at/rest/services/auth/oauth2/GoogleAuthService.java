package at.rest.services.auth.oauth2;


import at.rest.exceptions.AuthenticationException;
import at.rest.models.entities.User;
import at.rest.services.auth.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GoogleAuthService {
    @Inject
    JwtService jwtService;

    @Inject
    GoogleTokenVerifier tokenVerifier;

    @Inject
    GoogleUserProvider googleUserProvider;


    public String loginWithGoogleToken(String idTokenString) {
        GoogleIdToken.Payload payload = tokenVerifier.verify(idTokenString);

        if (payload == null) {
            throw new AuthenticationException("Ungültiges Google Token.");
        }

        User user = googleUserProvider.findOrCreateGoogleUser(payload);

        return jwtService.createJwtForUser(user);
    }
}
