package com.appweb.application.repository;

import com.appweb.application.model.ApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRoleRepository extends JpaRepository<ApplicationRole, String> {
    Optional<ApplicationRole> findByName(String roleUser);
}
