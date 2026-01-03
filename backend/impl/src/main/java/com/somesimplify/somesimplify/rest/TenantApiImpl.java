package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.TenantApi;
import com.somesimplify.model.OnboardingStatusTO;
import com.somesimplify.model.TenantProfileTO;
import com.somesimplify.model.TenantTO;
import com.somesimplify.somesimplify.mapper.TenantMapper;
import com.somesimplify.somesimplify.mapper.TenantProfileMapper;
import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.model.TenantProfile;
import com.somesimplify.somesimplify.service.TenantProfileService;
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
    private final TenantProfileService tenantProfileService;
    private final TenantProfileMapper tenantProfileMapper;

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

    @Override
    public ResponseEntity<TenantProfileTO> getTenantProfile() {
        TenantProfile profile = tenantProfileService.getTenantProfile();
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tenantProfileMapper.toTO(profile));
    }

    @Override
    public ResponseEntity<TenantProfileTO> upsertTenantProfile(TenantProfileTO profileTO) {
        TenantProfile profile = tenantProfileService.upsertTenantProfile(profileTO);
        return ResponseEntity.ok(tenantProfileMapper.toTO(profile));
    }

    @Override
    public ResponseEntity<OnboardingStatusTO> getOnboardingStatus() {
        OnboardingStatusTO status = tenantProfileService.getOnboardingStatus();
        return ResponseEntity.ok(status);
    }
}
