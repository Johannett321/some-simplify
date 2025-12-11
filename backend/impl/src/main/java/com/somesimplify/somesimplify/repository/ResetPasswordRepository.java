package com.somesimplify.somesimplify.repository;

import com.somesimplify.somesimplify.model.ResetPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordRequest, String> {

}
