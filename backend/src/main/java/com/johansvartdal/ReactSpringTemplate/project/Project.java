package com.johansvartdal.ReactSpringTemplate.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.johansvartdal.ReactSpringTemplate.status.Priority;
import com.johansvartdal.ReactSpringTemplate.task.Task;
import com.johansvartdal.ReactSpringTemplate.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Priority priority;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "project_user",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks;


    public Project() {

    }
}
