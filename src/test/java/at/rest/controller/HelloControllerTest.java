package at.rest.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HelloControllerTest {

    private static HttpClient client;
    private static final String BASE_URL = "http://localhost:8080/api";


    @BeforeAll
    static void setup() {
        client = HttpClient.newHttpClient();
    }


    @Test
    void hello() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/hello"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Statuscode prüfen
        assertThat(response.statusCode()).isEqualTo(200);

        // Ausgabe der erhaltenen JSON-Liste
        System.out.println(response.body());
    }
}
