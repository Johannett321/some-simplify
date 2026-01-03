package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.Tenant;
import com.somesimplify.somesimplify.model.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile, String> {

    Optional<TenantProfile> findByTenant(Tenant tenant);

    Optional<TenantProfile> findByTenantId(String tenantId);

    boolean existsByTenant(Tenant tenant);
}
