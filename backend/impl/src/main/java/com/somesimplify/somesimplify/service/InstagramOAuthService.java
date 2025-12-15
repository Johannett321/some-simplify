package com.somesimplify.somesimplify.service;

import com.somesimplify.model.PlatformType;
import com.somesimplify.somesimplify.config.ApplicationConfig;
import com.somesimplify.somesimplify.dto.instagram.InstagramAccountInfo;
import com.somesimplify.somesimplify.dto.instagram.InstagramTokenResponse;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.SocialMediaConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramOAuthService {

    private final SocialMediaConnectionRepository connectionRepository;
    private final RestTemplate restTemplate;
    private final ApplicationConfig applicationConfig;

    @Value("${instagram.app-id}")
    private String instagramAppId;

    @Value("${instagram.app-secret}")
    private String instagramAppSecret;

    private static final String OAUTH_URL = "https://api.instagram.com/oauth";
    private static final String GRAPH_API_URL = "https://graph.facebook.com/v18.0";

    public String getAuthorizationUrl(String tenantId) {
        String redirectUri = applicationConfig.getBackendUrl() + "/instagram/oauth/callback";
        String state = generateState(tenantId);

        return String.format(
                "%s/authorize?client_id=%s&redirect_uri=%s&scope=instagram_basic,instagram_content_publish&response_type=code&state=%s",
                OAUTH_URL,
                instagramAppId,
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                state
        );
    }

    public SocialMediaConnection exchangeCodeForToken(String code, String state) {
        String tenantId = extractTenantIdFromState(state);
        TenantContext.setTenantId(tenantId);

        String shortLivedToken = getShortLivedToken(code);

        InstagramTokenResponse longLivedToken = getLongLivedToken(shortLivedToken);

        InstagramAccountInfo accountInfo = getInstagramAccount(longLivedToken.getAccessToken());

        return saveConnection(tenantId, accountInfo, longLivedToken);
    }

    private String getShortLivedToken(String code) {
        String redirectUri = applicationConfig.getBackendUrl() + "/instagram/oauth/callback";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", instagramAppId);
        params.add("client_secret", instagramAppSecret);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        InstagramTokenResponse response = restTemplate.postForObject(
                OAUTH_URL + "/access_token",
                params,
                InstagramTokenResponse.class
        );

        return response.getAccessToken();
    }

    private InstagramTokenResponse getLongLivedToken(String shortLivedToken) {
        String url = String.format(
                "%s/access_token?grant_type=ig_exchange_token&client_secret=%s&access_token=%s",
                GRAPH_API_URL,
                instagramAppSecret,
                shortLivedToken
        );

        return restTemplate.getForObject(url, InstagramTokenResponse.class);
    }

    private InstagramAccountInfo getInstagramAccount(String accessToken) {
        String url = String.format(
                "%s/me?fields=id,username,account_type&access_token=%s",
                GRAPH_API_URL,
                accessToken
        );

        return restTemplate.getForObject(url, InstagramAccountInfo.class);
    }

    private SocialMediaConnection saveConnection(String tenantId,
                                                  InstagramAccountInfo accountInfo,
                                                  InstagramTokenResponse token) {
        Optional<SocialMediaConnection> existing = connectionRepository
                .findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM);

        SocialMediaConnection connection = existing.orElse(new SocialMediaConnection());
        connection.setPlatform(PlatformType.INSTAGRAM);
        connection.setPlatformAccountId(accountInfo.getId());
        connection.setAccessToken(token.getAccessToken());
        connection.setAccountName(accountInfo.getUsername());
        connection.setIsActive(true);
        connection.setTokenExpiresAt(
                OffsetDateTime.now().plusSeconds(token.getExpiresIn())
        );

        return connectionRepository.save(connection);
    }

    public void disconnectInstagram() {
        connectionRepository.findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM)
                .ifPresent(connection -> {
                    connection.setIsActive(false);
                    connectionRepository.save(connection);
                });
    }

    private String generateState(String tenantId) {
        String nonce = UUID.randomUUID().toString();
        String combined = tenantId + ":" + nonce;
        return Base64.getUrlEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    private String extractTenantIdFromState(String state) {
        String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        return decoded.split(":")[0];
    }
}
