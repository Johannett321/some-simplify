package com.somesimplify.somesimplify.multitenancy.filter;

import com.somesimplify.somesimplify.model.User;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.TenantRepository;
import com.somesimplify.somesimplify.repository.UserRepository;
import com.somesimplify.somesimplify.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tenantId = extractTenantId(request);

        // Get user directly from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        User currentUser = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (currentUser == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // make sure user has access to tenant
        if (tenantId != null && !tenantRepository.existsByIdAndUsersContains(tenantId, currentUser)) {
            throw new AccessDeniedException("Access denied");
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }
        filterChain.doFilter(request, response);
    }

    private String extractTenantId(HttpServletRequest request) {
        return request.getHeader("X-Tenant-ID");
    }
}
