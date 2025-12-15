package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
}
