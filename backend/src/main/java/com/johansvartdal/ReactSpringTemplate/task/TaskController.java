package com.johansvartdal.ReactSpringTemplate.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping(path = "/{taskID}/get")
    public Task getTask (@PathVariable Long taskID) {
        return taskService.getTaskByID(taskID);
    }

    @GetMapping(path = "/list")
    public ResponseEntity<?> getTasks () {
        return ResponseEntity.internalServerError().body("This feature is not implemented yet");
    }

    @PostMapping(path = "/add")
    public ResponseEntity<?> addTask (@RequestBody TaskTO taskTO) {
        taskService.createTask(taskTO);
        return ResponseEntity.ok("The task was successfully created");
    }
}
