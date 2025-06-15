package at.rest.services.auth.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;

@ApplicationScoped
public class GoogleTokenVerifier {
    private static final String GOOGLE_CLIENT_ID = System.getProperty("google.client.id");

    private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(),
            new GsonFactory()
    ).setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;
        } catch (Exception e) {
            return null;
        }
    }

}
