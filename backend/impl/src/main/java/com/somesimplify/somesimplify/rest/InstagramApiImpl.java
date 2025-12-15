package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.InstagramApi;
import com.somesimplify.model.GetInstagramAuthUrl200Response;
import com.somesimplify.model.SocialMediaConnectionTO;
import com.somesimplify.somesimplify.config.ApplicationConfig;
import com.somesimplify.somesimplify.mapper.SocialMediaConnectionMapper;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import com.somesimplify.somesimplify.repository.SocialMediaConnectionRepository;
import com.somesimplify.somesimplify.service.InstagramOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.somesimplify.model.PlatformType;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InstagramApiImpl implements InstagramApi {

    private final InstagramOAuthService oauthService;
    private final SocialMediaConnectionRepository connectionRepository;
    private final SocialMediaConnectionMapper connectionMapper;
    private final ApplicationConfig applicationConfig;

    @Override
    public ResponseEntity<GetInstagramAuthUrl200Response> getInstagramAuthUrl() {
        String tenantId = TenantContext.getTenantId();
        String authUrl = oauthService.getAuthorizationUrl(tenantId);

        GetInstagramAuthUrl200Response response = new GetInstagramAuthUrl200Response();
        response.setAuthUrl(authUrl);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> instagramOAuthCallback(String code, String state) {
        try {
            oauthService.exchangeCodeForToken(code, state);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(
                    applicationConfig.getFrontendUrl() + "/settings?instagram=connected"
            ));

            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) {
            log.error("Instagram OAuth callback failed: {}", e.getMessage(), e);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(
                    applicationConfig.getFrontendUrl() + "/settings?instagram=error"
            ));

            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @Override
    public ResponseEntity<SocialMediaConnectionTO> getInstagramConnection() {
        Optional<SocialMediaConnection> connection = connectionRepository
                .findByPlatformAndIsActiveTrue(PlatformType.INSTAGRAM);

        return connection
                .map(conn -> ResponseEntity.ok(connectionMapper.toTO(conn)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> disconnectInstagram() {
        oauthService.disconnectInstagram();
        return ResponseEntity.noContent().build();
    }
}
