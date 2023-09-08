package com.johansvartdal.ReactSpringTemplate.task;

import com.johansvartdal.ReactSpringTemplate.project.Project;
import com.johansvartdal.ReactSpringTemplate.status.Priority;
import com.johansvartdal.ReactSpringTemplate.status.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String title;
    private String description;
    private Long deadlineMillis;
    private Status status;
    private Priority priority;

    public Task() {

    }
}
