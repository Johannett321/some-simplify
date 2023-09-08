package com.johansvartdal.ReactSpringTemplate.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByClerkID(String clerkId);
    Boolean existsByClerkID(String clerkId);
}
