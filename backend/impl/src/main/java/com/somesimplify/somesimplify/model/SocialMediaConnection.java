package com.somesimplify.somesimplify.model;

import com.somesimplify.model.PlatformType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class SocialMediaConnection extends AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformType platform;

    @Column(nullable = false)
    private String platformAccountId;

    @Column(nullable = false, columnDefinition = "text")
    private String accessToken;

    @Column(nullable = false)
    private OffsetDateTime tokenExpiresAt;

    private String accountName;

    @Column(nullable = false)
    private Boolean isActive;

    private OffsetDateTime lastPublishedAt;

    @Column(columnDefinition = "text")
    private String lastError;
}
