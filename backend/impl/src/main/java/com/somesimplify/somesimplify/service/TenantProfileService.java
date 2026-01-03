package com.somesimplify.somesimplify.service;

import com.somesimplify.model.OnboardingStatusTO;
import com.somesimplify.model.PlatformType;
import com.somesimplify.model.TenantProfileTO;
import com.somesimplify.somesimplify.mapper.TenantProfileMapper;
import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.model.TenantProfile;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.SocialMediaConnectionRepository;
import com.somesimplify.somesimplify.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProfileService {

    private final TenantProfileRepository profileRepository;
    private final TenantProfileMapper profileMapper;
    private final TenantService tenantService;
    private final SocialMediaConnectionRepository socialMediaConnectionRepository;

    public TenantProfile getTenantProfile() {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantService.getTenantById(tenantId);
        return profileRepository.findByTenant(tenant).orElse(null);
    }

    @Transactional
    public TenantProfile upsertTenantProfile(TenantProfileTO profileTO) {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantService.getTenantById(tenantId);

        TenantProfile profile = profileRepository.findByTenant(tenant)
            .orElseGet(() -> {
                TenantProfile newProfile = new TenantProfile();
                newProfile.setTenant(tenant);
                return newProfile;
            });

        profile = profileMapper.updateFromTO(profileTO, profile);
        return profileRepository.save(profile);
    }

    public OnboardingStatusTO getOnboardingStatus() {
        OnboardingStatusTO status = new OnboardingStatusTO();

        // Check Step 1: Profile completion
        TenantProfile profile = getTenantProfile();
        boolean profileExists = profile != null;
        boolean step1Completed = profileExists &&
            profile.getAddress() != null && !profile.getAddress().isEmpty() &&
            profile.getConcept() != null && !profile.getConcept().isEmpty() &&
            profile.getTargetAudience() != null && !profile.getTargetAudience().isEmpty() &&
            profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isEmpty();

        // Check Step 2: Instagram connection
        boolean instagramConnected = socialMediaConnectionRepository
            .findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM)
            .isPresent();

        status.setProfileExists(profileExists);
        status.setStep1Completed(step1Completed);
        status.setInstagramConnected(instagramConnected);
        status.setStep2Completed(instagramConnected);

        return status;
    }
}
