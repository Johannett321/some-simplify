package com.johansvartdal.ReactSpringTemplate.task;

import com.johansvartdal.ReactSpringTemplate.exceptions.NotAccessToProjectException;
import com.johansvartdal.ReactSpringTemplate.exceptions.ResourceNotFoundException;
import com.johansvartdal.ReactSpringTemplate.project.Project;
import com.johansvartdal.ReactSpringTemplate.project.ProjectService;
import com.johansvartdal.ReactSpringTemplate.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, UserService userService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    /**
     * Returns a task by ID. It requires that the user is part of the project that the task belongs to
     * @param id The id of the task
     * @return The task itself
     */
    public Task getTaskByID(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        // make sure the current user has access to the project
        if (!task.get().getProject().getUsers().contains(userService.getCurrentUser())) {
            throw new NotAccessToProjectException();
        }

        // return the task
        return task.get();
    }

    /**
     * Creates a new task as long as the user is part of the project that the task will belong to
     * @param taskTO The information for creating a new task
     */
    public void createTask(TaskTO taskTO) {
        // get the project
        Project project = projectService.getProject(taskTO.getProjectID());

        // make the user actually have the right permissions
        if (!project.getUsers().contains(userService.getCurrentUser())) {
            throw new NotAccessToProjectException();
        }

        // create the task
        Task task = new Task();
        task.setTitle(taskTO.getTitle());
        task.setDescription(taskTO.getDescription());
        task.setPriority(taskTO.getPriority());
        task.setDeadlineMillis(taskTO.getDeadlineMillis());
        task.setStatus(taskTO.getStatus());

        // connect the task to the project
        task.setProject(project);

        // save
        taskRepository.save(task);
    }
}
