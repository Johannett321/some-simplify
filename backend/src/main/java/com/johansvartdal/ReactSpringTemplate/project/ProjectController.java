package com.johansvartdal.ReactSpringTemplate.project;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProjectController {

    final private ProjectService projectService;

    public ProjectController(ProjectService service) {
        this.projectService = service;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProject(@RequestBody ProjectDTO projectDTO) {
        projectService.addProject(projectDTO);
        return ResponseEntity.ok().body("Created project " + projectDTO.getName());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProject() {
        return ResponseEntity.internalServerError().body("This feature is not implemented yet :(");
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getProjects(@RequestParam int page) {
        return ResponseEntity.ok(projectService.getProjects(PageRequest.of(page,10)));
    }
}
