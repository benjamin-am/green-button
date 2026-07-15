package com.greenbutton.backend;

import com.greenbutton.backend.auth.LoginRateLimitFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired LoginRateLimitFilter rateLimitFilter;

    @AfterEach
    void resetRateLimit() {
        rateLimitFilter.clearAll();
    }

    @BeforeEach
    void registerAlice() {
        rest.postForEntity("/api/auth/register", Map.of("username", "alice", "password", "hunter2"), Map.class);
    }

    @Test
    void register_validCredentials_returnsJwt() {
        var response = rest.postForEntity("/api/auth/register",
                Map.of("username", "bob", "password", "hunter2"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) response.getBody().get("token")).isNotBlank();
    }

    @Test
    void protectedRoute_withValidJwt_returns200() {
        String token = login("alice", "hunter2");

        var response = rest.exchange("/api/users/me", HttpMethod.GET, bearerRequest(token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("username")).isEqualTo("alice");
    }

    @Test
    void protectedRoute_withoutToken_returns401() {
        var response = rest.getForEntity("/api/users/me", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_correctCredentials_returnsJwt() {
        var response = rest.postForEntity("/api/auth/login",
                Map.of("username", "alice", "password", "hunter2"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) response.getBody().get("token")).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() {
        var response = rest.postForEntity("/api/auth/login",
                Map.of("username", "alice", "password", "wrong"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownUsername_returns401Not404() {
        var response = rest.postForEntity("/api/auth/login",
                Map.of("username", "nobody", "password", "hunter2"), Map.class);

        // 401, not 404 — prevents username enumeration
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void register_duplicateUsername_returns409() {
        var response = rest.postForEntity("/api/auth/register",
                Map.of("username", "alice", "password", "different"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_rateLimitedAfterFiveFailedAttempts_returns429() {
        for (int i = 0; i < 5; i++) {
            rest.postForEntity("/api/auth/login",
                    Map.of("username", "alice", "password", "wrong"), Map.class);
        }

        var response = rest.postForEntity("/api/auth/login",
                Map.of("username", "alice", "password", "wrong"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    private String login(String username, String password) {
        var response = rest.postForEntity("/api/auth/login",
                Map.of("username", username, "password", password), Map.class);
        return (String) response.getBody().get("token");
    }

    private HttpEntity<Void> bearerRequest(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
