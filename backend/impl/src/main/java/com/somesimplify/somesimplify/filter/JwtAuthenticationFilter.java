package com.somesimplify.somesimplify.filter;
import com.somesimplify.somesimplify.exception.ResourceNotFoundException;
import com.somesimplify.somesimplify.service.CustomUserDetailsService;
import com.somesimplify.somesimplify.utils.FormatUtils;
import com.somesimplify.somesimplify.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_COOKIE_NAME = "token"; // keep in sync with the name you set

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String jwtToken = resolveToken(request); // header first, then cookie
        String username = null;

        if (jwtToken != null) {
            try {
                username = jwtUtil.extractUsername(jwtToken);
                log.trace("JWT accepted! Username: {}. Token: {}", username, FormatUtils.maskString(jwtToken));
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                // Invalid signature/format: treat as unauthenticated and continue
                log.debug("JWT rejected: {}", e.getClass().getSimpleName());
            } catch (ExpiredJwtException e) {
                log.debug("JWT expired");
            } catch (IllegalArgumentException e) {
                log.debug("JWT missing claims");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            }catch (UsernameNotFoundException | ResourceNotFoundException e) {
                userDetails = null;
            }

            if (userDetails != null && jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    /** Prefer Authorization header; fall back to cookie "token". */
    private String resolveToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        // Fallback to cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> TOKEN_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
