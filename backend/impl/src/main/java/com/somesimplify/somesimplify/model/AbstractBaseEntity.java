package com.somesimplify.somesimplify.model;

import com.somesimplify.somesimplify.multitenancy.util.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@MappedSuperclass
public abstract class AbstractBaseEntity {

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Setter
    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        createdAt = updatedAt = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Oslo"));
        this.tenantId = TenantContext.getTenantId();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Oslo"));
    }
}
