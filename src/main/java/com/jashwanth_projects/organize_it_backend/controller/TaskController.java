package com.jashwanth_projects.organize_it_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // Get all tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Create a new task
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    @PutMapping("/{id}")
    public Task updateTaskupdateTask(@PathVariable String id, @RequestBody Task taskDetails) {
        // Check if the task exists
        Optional<Task> taskOptional = taskRepository.findById(id);

        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            // Update task properties
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setCompleted(taskDetails.isCompleted());
            task.setPriority(taskDetails.getPriority());

            return taskRepository.save(task);  // Save updated task
        } else {
            throw new RuntimeException("Task not found with id: " + id);
        }
    }

    // Delete a task by id
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable String id) {
        // Check if the task exists
        Optional<Task> taskOptional = taskRepository.findById(id);

        if (taskOptional.isPresent()) {
            taskRepository.deleteById(id);  // Delete the task
            return "Task with id " + id + " has been deleted successfully.";
        } else {
            throw new RuntimeException("Task not found with id: " + id);
        }
    }

    // Get a task by id
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable String id) {
        // Check if the task exists
        Optional<Task> taskOptional = taskRepository.findById(id);

        if (taskOptional.isPresent()) {
            return taskOptional.get();  // Return the found task
        } else {
            throw new RuntimeException("Task not found with id: " + id);
        }
    }

}
