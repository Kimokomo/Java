package at.rest.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class AuthControllerTest {

    private static HttpClient client;
    private static final String BASE_URL = "http://localhost:8080/api/auth";

    @BeforeAll
    static void setup() {
        client = HttpClient.newHttpClient();
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        // Achtung: Groß-/Kleinschreibung muss mit deiner echten Benutzerverwaltung übereinstimmen
        String json = """
                {
                    "username": "admin",
                    "password": "pass123"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login")) // <--- sicherstellen, dass dein Login unter /auth/login liegt
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("token");

        System.out.println("Login successful, received token: " + response.body());
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        String json = """
                {
                    "username": "admin",
                    "password": "falsch"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
    }
}
