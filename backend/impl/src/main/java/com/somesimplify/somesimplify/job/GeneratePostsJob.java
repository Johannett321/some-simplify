package com.somesimplify.somesimplify.job;

import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.TenantRepository;
import com.somesimplify.somesimplify.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratePostsJob {

    private final TenantRepository tenantRepository;
    private final PostService postService;

    @Scheduled(cron = "0 30 3 * * *")
    //@Scheduled(fixedRate = 1000*60*60*24)
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Start generating posts job");
        loopThroughTenants();
        log.info("Finish generating posts job at {}", now);
    }

    private void loopThroughTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        tenants.forEach(tenant -> {
            log.info("Generating posts for tenant {}", tenant.getName());
            TenantContext.setTenantId(tenant.getId());
            postService.generatePosts(tenant.getId());
        });
    }
}
