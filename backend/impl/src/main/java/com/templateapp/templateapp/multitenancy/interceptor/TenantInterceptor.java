package com.templateapp.templateapp.multitenancy.interceptor;

import com.templateapp.templateapp.model.User;
import com.templateapp.templateapp.multitenancy.util.TenantContext;
import com.templateapp.templateapp.repository.TenantRepository;
import com.templateapp.templateapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = extractTenantId(request);
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return true;
        }

        // make sure user has access to tenant
        Boolean hasAccess = tenantRepository.existsByIdAndUsersContains(tenantId, currentUser);
        if (tenantId != null && !hasAccess) {
            throw new AccessDeniedException("Access denied");
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String extractTenantId(HttpServletRequest request) {
        return request.getHeader("X-Tenant-ID");
    }
}