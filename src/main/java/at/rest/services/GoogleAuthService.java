package at.rest.services;


import at.rest.exceptions.AuthenticationException;
import at.rest.models.User;
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

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        User user = googleUserProvider.findOrCreateGoogleUser(email, googleId, name);

        return jwtService.createJwtForUser(user);
    }
}
