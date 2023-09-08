package com.johansvartdal.ReactSpringTemplate.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // check if session token is present
        Optional<String> sessionToken = Authenticate.getSessionTokenFromCookies(request);
        if (sessionToken.isEmpty()) {
            System.out.println("Someone attempted access to backend without session token");
            throwError(response);
            return;
        }

        // check if the user is authenticated. This initiates an API request to Clerk
        Boolean isAuthenticated = Authenticate.authenticated(sessionToken.get());
        if (!isAuthenticated) {
            System.out.println("Someone attempted access to backend without being authenticated. Wrong credentials?");
            throwError(response);
            return;
        }

        // create the authenticationToken
        AuthenticationToken authenticationToken = new AuthenticationToken(sessionToken.get());
        authenticationToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // continue the filter chain
        filterChain.doFilter(request, response);
    }

    @SneakyThrows
    public void throwError(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try {
            response.getWriter().write("401 Unauthorized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
