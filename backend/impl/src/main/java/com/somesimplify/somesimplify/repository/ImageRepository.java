package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.Image;
import com.somesimplify.somesimplify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findAllByUploadedByOrderByCreatedAtDesc(User user);
    Optional<Image> findByIdAndUploadedBy(String id, User user);
}
