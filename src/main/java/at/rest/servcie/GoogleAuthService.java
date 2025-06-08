package at.rest.servcie;


import at.rest.exceptions.AuthenticationException;
import at.rest.model.User;
import at.rest.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Optional;

@ApplicationScoped
public class GoogleAuthService {

    private static final String GOOGLE_CLIENT_ID = System.getProperty("google.client.id");

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    public String loginWithGoogleToken(String idTokenString) {
        GoogleIdToken.Payload payload = verifyToken(idTokenString);

        if (payload == null) {
            throw new AuthenticationException("Ungültiges Google Token.");
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        User user = findOrCreateUser(email, googleId, name);

        return jwtService.createJwtForUser(user);
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private User findOrCreateUser(String email, String googleId, String name) {
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isPresent()) return userOpt.get();

        userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) return userOpt.get();

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setGoogleId(googleId);
        newUser.setUsername(email);
        newUser.setRole("user");
        newUser.setConfirmed(true);
        newUser.setPasswordHash("GOOGLE_LOGIN_HASH");
        newUser.setPassword("GOOGLE_LOGIN");

        userRepository.saveNew(newUser);

        return newUser;
    }
}
