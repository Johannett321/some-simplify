package com.johansvartdal.ReactSpringTemplate.task;

import com.johansvartdal.ReactSpringTemplate.status.Priority;
import com.johansvartdal.ReactSpringTemplate.status.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskTO {
    private String title;
    private String description;
    private Long projectID;
    private Status status;
    private Priority priority;
    private Long deadlineMillis;

}
