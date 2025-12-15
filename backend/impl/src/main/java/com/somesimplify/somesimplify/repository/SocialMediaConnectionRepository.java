package com.somesimplify.somesimplify.repository;

import com.somesimplify.model.PlatformType;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialMediaConnectionRepository extends JpaRepository<SocialMediaConnection, String> {

    Optional<SocialMediaConnection> findByPlatformAndIsActiveTrue(PlatformType platform);

    List<SocialMediaConnection> findAllByIsActiveTrueAndTokenExpiresAtBefore(OffsetDateTime expiry);
}
