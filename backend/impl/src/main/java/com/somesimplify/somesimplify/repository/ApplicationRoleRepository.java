package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.ApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRoleRepository extends JpaRepository<ApplicationRole, String> {
    Optional<ApplicationRole> findByName(String roleUser);
}
