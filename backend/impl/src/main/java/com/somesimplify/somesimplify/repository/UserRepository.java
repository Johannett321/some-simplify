package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByFacebookId(String facebookId);
    Optional<User> findByGoogleSub(String googleSub);
}
