package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.ContentFile;
import com.somesimplify.somesimplify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ContentFile, String> {
    List<ContentFile> findAllByUploadedByOrderByCreatedAtDesc(User user);
    Optional<ContentFile> findByIdAndUploadedBy(String id, User user);
}
