package com.templateapp.templateapp.repository;

import com.templateapp.templateapp.model.Tenant;
import com.templateapp.templateapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    List<Tenant> findAllByUsersContains(User user);

    Optional<Tenant> findByIdAndUsersContains(String id, User users);

    Boolean existsByIdAndUsersContains(String tenantId, User currentUser);
}
