package com.somesimplify.somesimplify.service;

import com.somesimplify.model.TenantTO;
import com.somesimplify.somesimplify.mapper.TenantMapper;
import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final UserService userService;

    public Tenant createTenant(TenantTO tenantTO) {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID().toString());
        tenant = tenantMapper.updateTenantFromTO(tenantTO, tenant);
        tenant.setUsers(List.of(userService.getCurrentUser()));

        TenantContext.setTenantId(tenant.getId());

        return tenantRepository.save(tenant);
    }

    public Tenant getTenantById(String id) {
        return tenantRepository.findByIdAndUsersContains(id, userService.getCurrentUser()).orElse(null);
    }

    public List<Tenant> getTenants() {
        return tenantRepository.findAllByUsersContains(userService.getCurrentUser());
    }
}
