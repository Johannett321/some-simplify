package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.TenantApi;
import com.somesimplify.model.TenantTO;
import com.somesimplify.somesimplify.mapper.TenantMapper;
import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TenantApiImpl implements TenantApi {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    @Override
    public ResponseEntity<String> createTenant(TenantTO tenantTO) {
        Tenant tenant = tenantService.createTenant(tenantTO);
        return ResponseEntity.ok(tenant.getId());
    }

    @Override
    public ResponseEntity<List<TenantTO>> getTenants() {
        List<Tenant> tenants = tenantService.getTenants();
        return ResponseEntity.ok(tenants.stream().map(tenantMapper::toTenantTO).toList());
    }

    @Override
    public ResponseEntity<TenantTO> getTenant(String tenantId) {
        Tenant tenant = tenantService.getTenantById(tenantId);
        return ResponseEntity.ok(tenantMapper.toTenantTO(tenant));
    }
}
