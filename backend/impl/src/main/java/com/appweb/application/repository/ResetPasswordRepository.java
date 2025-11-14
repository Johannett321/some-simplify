package com.appweb.application.repository;

import com.appweb.application.model.ResetPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordRequest, String> {

}
