package com.johansvartdal.ReactSpringTemplate.filter;

import com.johansvartdal.ReactSpringTemplate.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserExistsFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/api/v1/test/")) {
            filterChain.doFilter(request, response);
            return;
        }
        // make sure user exists in database
        userService.createUserIfItDoesNotExist();
        filterChain.doFilter(request, response);
    }
}
