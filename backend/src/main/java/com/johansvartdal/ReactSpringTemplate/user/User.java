package com.johansvartdal.ReactSpringTemplate.user;

import com.johansvartdal.ReactSpringTemplate.project.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Table(name = "user")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ourID;
    private String clerkID;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Project> project;
}
