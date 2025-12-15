package com.somesimplify.somesimplify.repository;

import com.somesimplify.model.PostStatus;
import com.somesimplify.somesimplify.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.publishAt <= :publishAt")
    List<Post> findPostsReadyForPublishing(
            @Param("status") PostStatus status,
            @Param("publishAt") OffsetDateTime publishAt
    );
}
