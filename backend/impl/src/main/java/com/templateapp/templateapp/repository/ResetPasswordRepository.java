package com.templateapp.templateapp.repository;

import com.templateapp.templateapp.model.ResetPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordRequest, String> {

}
