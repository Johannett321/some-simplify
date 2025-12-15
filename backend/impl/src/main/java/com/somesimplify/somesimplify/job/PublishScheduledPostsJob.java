package com.somesimplify.somesimplify.job;

import com.somesimplify.model.PlatformType;
import com.somesimplify.model.PostStatus;
import com.somesimplify.somesimplify.exception.InstagramPublishException;
import com.somesimplify.somesimplify.model.Post;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.PostRepository;
import com.somesimplify.somesimplify.repository.SocialMediaConnectionRepository;
import com.somesimplify.somesimplify.repository.TenantRepository;
import com.somesimplify.somesimplify.service.InstagramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishScheduledPostsJob {

    private final TenantRepository tenantRepository;
    private final PostRepository postRepository;
    private final InstagramService instagramService;
    private final SocialMediaConnectionRepository connectionRepository;

    @Scheduled(cron = "0 * * * * *")
    public void publishScheduledPosts() {
        log.debug("Checking for scheduled posts to publish");

        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            try {
                TenantContext.setTenantId(tenant.getId());
                publishPostsForTenant(tenant);
            } catch (Exception e) {
                log.error("Error publishing posts for tenant {}: {}",
                        tenant.getName(), e.getMessage(), e);
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void publishPostsForTenant(Tenant tenant) {
        Optional<SocialMediaConnection> instagramConnection = connectionRepository
                .findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM);

        if (instagramConnection.isEmpty()) {
            log.trace("No Instagram connection for tenant {}", tenant.getName());
            return;
        }

        List<Post> postsToPublish = postRepository.findPostsReadyForPublishing(
                PostStatus.SCHEDULED,
                OffsetDateTime.now()
        );

        if (postsToPublish.isEmpty()) {
            return;
        }

        log.info("Publishing {} Instagram posts for tenant {}",
                postsToPublish.size(), tenant.getName());

        for (Post post : postsToPublish) {
            if (post.getPlatforms().contains(PlatformType.INSTAGRAM)) {
                publishSinglePost(post, tenant);
            }
        }
    }

    private void publishSinglePost(Post post, Tenant tenant) {
        try {
            log.info("Publishing post {} to Instagram for tenant {}",
                    post.getId(), tenant.getName());

            String instagramMediaId = instagramService.publishCarouselPost(post);

            post.setStatus(PostStatus.PUBLISHED);
            postRepository.save(post);

            log.info("Successfully published post {} to Instagram. Media ID: {}",
                    post.getId(), instagramMediaId);

        } catch (InstagramPublishException e) {
            log.error("Failed to publish post {} to Instagram: {}",
                    post.getId(), e.getMessage());

            if (isRetryableError(e)) {
                log.info("Will retry post {} in next job run", post.getId());
            } else {
                post.setStatus(PostStatus.REJECTED);
                postRepository.save(post);
                log.warn("Marked post {} as REJECTED due to non-retryable error", post.getId());
            }
        }
    }

    private boolean isRetryableError(InstagramPublishException e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("rate limit")
                || message.contains("temporarily unavailable")
                || message.contains("timeout");
    }
}
