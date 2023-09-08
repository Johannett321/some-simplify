package com.johansvartdal.ReactSpringTemplate.project;

import com.johansvartdal.ReactSpringTemplate.exceptions.NotAccessToProjectException;
import com.johansvartdal.ReactSpringTemplate.exceptions.ResourceNotFoundException;
import com.johansvartdal.ReactSpringTemplate.status.Priority;
import com.johansvartdal.ReactSpringTemplate.user.User;
import com.johansvartdal.ReactSpringTemplate.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Object getProjects(Pageable pageable) {
        return projectRepository.findProjectByUsersContains(userService.getCurrentUser(), pageable);
    }

    /**
     * Get a project that the user is part of
     * @param id The id of the project
     * @return The project itself
     */
    public Project getProject(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        // make sure the project exists
        if (project.isEmpty()){
            throw new ResourceNotFoundException();
        }
        // make sure the user is part of the project
        if (!project.get().getUsers().contains(userService.getCurrentUser())) {
            throw new NotAccessToProjectException();
        }

        // return the project
        return project.get();
    }

    public void addProject(ProjectDTO projectDTO) {
        // define the project
        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());

        // add the current to the project
        HashSet<User> users = new HashSet<>();
        users.add(userService.getCurrentUser());
        project.setUsers(users);

        // save the project
        projectRepository.save(project);
    }

    /**
     * Change the priority of a project
     * @param projectID The ID of the project
     * @param priority The new Priority
     */
    public void setPriority(Long projectID, Priority priority) {
        Project project = getProject(projectID);
        project.setPriority(priority);
        projectRepository.save(project);
    }
}
