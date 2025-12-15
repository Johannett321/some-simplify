package com.somesimplify.somesimplify.service;

import com.somesimplify.model.PlatformType;
import com.somesimplify.somesimplify.dto.instagram.InstagramMediaResponse;
import com.somesimplify.somesimplify.exception.InstagramNotConnectedException;
import com.somesimplify.somesimplify.exception.InstagramPublishException;
import com.somesimplify.somesimplify.model.ContentFile;
import com.somesimplify.somesimplify.model.Post;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import com.somesimplify.somesimplify.repository.SocialMediaConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramService {

    private final RestTemplate restTemplate;
    private final S3Service s3Service;
    private final SocialMediaConnectionRepository connectionRepository;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v18.0";

    public String publishCarouselPost(Post post) {
        SocialMediaConnection connection = getActiveConnection();

        try {
            List<String> mediaIds = uploadMediaContainers(post, connection);

            String carouselId = createCarouselContainer(
                    mediaIds,
                    post.getText(),
                    connection
            );

            String publishedId = publishMedia(carouselId, connection);

            connection.setLastPublishedAt(OffsetDateTime.now());
            connection.setLastError(null);
            connectionRepository.save(connection);

            return publishedId;

        } catch (Exception e) {
            log.error("Failed to publish to Instagram: {}", e.getMessage(), e);
            connection.setLastError(e.getMessage());
            connectionRepository.save(connection);
            throw new InstagramPublishException("Failed to publish post", e);
        }
    }

    private List<String> uploadMediaContainers(Post post, SocialMediaConnection connection) {
        List<String> containerIds = new ArrayList<>();

        for (ContentFile contentFile : post.getContentFiles()) {
            String imageUrl = s3Service.generateLongLivedPresignedUrl(contentFile.getS3Key());
            String containerId = createImageContainer(
                    imageUrl,
                    connection.getPlatformAccountId(),
                    connection.getAccessToken()
            );
            containerIds.add(containerId);
        }

        return containerIds;
    }

    private String createImageContainer(String imageUrl, String accountId, String accessToken) {
        String url = String.format("%s/%s/media", GRAPH_API_URL, accountId);

        Map<String, String> params = new HashMap<>();
        params.put("image_url", imageUrl);
        params.put("is_carousel_item", "true");
        params.put("access_token", accessToken);

        InstagramMediaResponse response = restTemplate.postForObject(
                url,
                params,
                InstagramMediaResponse.class
        );

        return response.getId();
    }

    private String createCarouselContainer(List<String> mediaIds, String caption,
                                           SocialMediaConnection connection) {
        String url = String.format("%s/%s/media", GRAPH_API_URL,
                connection.getPlatformAccountId());

        Map<String, Object> params = new HashMap<>();
        params.put("media_type", "CAROUSEL");
        params.put("children", String.join(",", mediaIds));
        params.put("caption", caption != null ? caption : "");
        params.put("access_token", connection.getAccessToken());

        InstagramMediaResponse response = restTemplate.postForObject(
                url,
                params,
                InstagramMediaResponse.class
        );

        return response.getId();
    }

    private String publishMedia(String containerId, SocialMediaConnection connection) {
        String url = String.format("%s/%s/media_publish", GRAPH_API_URL,
                connection.getPlatformAccountId());

        Map<String, String> params = new HashMap<>();
        params.put("creation_id", containerId);
        params.put("access_token", connection.getAccessToken());

        InstagramMediaResponse response = restTemplate.postForObject(
                url,
                params,
                InstagramMediaResponse.class
        );

        return response.getId();
    }

    private SocialMediaConnection getActiveConnection() {
        return connectionRepository.findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM)
                .orElseThrow(() -> new InstagramNotConnectedException(
                        "No active Instagram connection found for tenant"
                ));
    }
}
