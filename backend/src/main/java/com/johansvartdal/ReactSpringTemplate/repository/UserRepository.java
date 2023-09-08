package com.johansvartdal.ReactSpringTemplate.repository;

import com.johansvartdal.ReactSpringTemplate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByClerkID(String clerkId);
    Boolean existsByClerkID(String clerkId);
}
