package com.johansvartdal.ReactSpringTemplate.project;

import com.johansvartdal.ReactSpringTemplate.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findProjectByUsersContains(User user, Pageable pageable);

}
