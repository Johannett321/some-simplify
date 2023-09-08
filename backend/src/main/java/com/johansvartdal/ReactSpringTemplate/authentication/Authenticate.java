package com.johansvartdal.ReactSpringTemplate.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class Authenticate {

    // the bearer token from Clerk
    private static final String BEARER_TOKEN = "sk_test_skkAl17XXUxtzFhCEc13j8giJoMJMzmu14eHDtx4fD";

    /**
     * Returns if the user is authenticated or not
     * @param sessionToken the current session token of the logged-in user
     * @return true if authenticated, false if not
     */
    public static Boolean authenticated(String sessionToken) {
        return isValidSessionToken(sessionToken);
    }

    /**
     * Gets the current session token from the users cookies
     * @param request The HTTPServletRequest with cookies
     * @return An optional session token. Might return an empty optional
     */
    public static Optional<String> getSessionTokenFromCookies(HttpServletRequest request) {
        // make sure there actually is cookies
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        // find the session token
        for (Cookie cookie : request.getCookies()) {
            if ("__session".equals(cookie.getName()) || "__dev_session".equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }

        // session token not found, returning empty
        return Optional.empty();
    }

    /**
     * Validate session token with ClerkAPI. This sends an API request to Clerk
     * @param sessionToken A session token
     * @return True if valid, else false
     */
    public static Boolean isValidSessionToken(String sessionToken) {
        // Decode JWT and get session ID
        DecodedJWT decodedJWT = JWT.decode(sessionToken);
        String sessionID = decodedJWT.getClaim("sid").asString();

        // Create an instance of RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + BEARER_TOKEN);
        headers.set("Content-Type", "application/json");

        // Body
        String body = "{"
                + "\"token\": \"" + sessionToken + "\""
                + "}";

        // create HTTPEntity
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // send request and receive response
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange("https://api.clerk.com/v1/sessions/" + sessionID + "/verify", HttpMethod.POST, entity, String.class);
        }catch (HttpClientErrorException e) {
            return false;
        }

        // validate session
        return response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().contains("active");
    }

    /**
     * Returns the ClerkUserID from the sessionToken
     * @param sessionToken The session token of the user
     * @return Clerk User ID
     */
    public static String getClerkUserID(String sessionToken) {
        // Decode JWT and get session ID
        DecodedJWT decodedJWT = JWT.decode(sessionToken);
        return decodedJWT.getClaim("sub").asString();
    }
}
