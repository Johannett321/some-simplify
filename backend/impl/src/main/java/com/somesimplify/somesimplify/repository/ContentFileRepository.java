package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.ContentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentFileRepository extends JpaRepository<ContentFile, String> {
    List<ContentFile> findAllByTenantId(String tenantId);
}
