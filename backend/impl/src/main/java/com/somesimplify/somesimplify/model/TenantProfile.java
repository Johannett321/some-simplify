package com.somesimplify.somesimplify.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class TenantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String concept;

    @Column(columnDefinition = "text")
    private String conceptOther;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_profile_target_audience", joinColumns = @JoinColumn(name = "tenant_profile_id"))
    @Column(name = "audience")
    private List<String> targetAudience;

    @Column(columnDefinition = "text")
    private String targetAudienceOther;

    @Column(nullable = false)
    private String websiteUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
