package com.jashwanth_projects.organize_it_backend.controller;

import java.time.Instant;
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
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskGroupRepository taskGroupRepository;

    // Get all tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Create a new task
    @PostMapping
    public Task createTask(@Valid @RequestBody Task task) {
        Instant now = Instant.now();
        if (task.getCreatedAt() == null) task.setCreatedAt(now);
        task.setUpdatedAt(now);

        Task saved = taskRepository.save(task);

        // If task belongs to a group, add to group's tasksList
        String gid = saved.getGroupId();
        if (gid != null) {
            taskGroupRepository.findById(gid).ifPresent(g -> {
                if (g.getTasksList() == null) g.setTasksList(new java.util.ArrayList<>());
                if (!g.getTasksList().contains(saved.getId())) {
                    g.getTasksList().add(saved.getId());
                    taskGroupRepository.save(g);
                }
            });
        }

        return saved;
    }

    @PutMapping("/{id}")
    public Task updateTaskupdateTask(@PathVariable String id, @Valid @RequestBody Task taskDetails) {
        // Check if the task exists
        Optional<Task> taskOptional = taskRepository.findById(id);

        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            // Track old group
            String oldGroupId = task.getGroupId();

            // Update task properties
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setIsCompleted(taskDetails.getIsCompleted());
            task.setPriority(taskDetails.getPriority());
            task.setGroupId(taskDetails.getGroupId()); // can be null
            task.setUpdatedAt(Instant.now());

            Task saved = taskRepository.save(task);  // Save updated task

            String newGroupId = saved.getGroupId();
            // If group changed, update group lists
            if (oldGroupId != null && !oldGroupId.equals(newGroupId)) {
                taskGroupRepository.findById(oldGroupId).ifPresent(g -> {
                    if (g.getTasksList() != null) {
                        g.getTasksList().remove(saved.getId());
                        taskGroupRepository.save(g);
                    }
                });
            }

            if (newGroupId != null && !newGroupId.equals(oldGroupId)) {
                taskGroupRepository.findById(newGroupId).ifPresent(g -> {
                    if (g.getTasksList() == null) g.setTasksList(new java.util.ArrayList<>());
                    if (!g.getTasksList().contains(saved.getId())) {
                        g.getTasksList().add(saved.getId());
                        taskGroupRepository.save(g);
                    }
                });
            }

            return saved;
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
            Task task = taskOptional.get();
            String gid = task.getGroupId();
            if (gid != null) {
                taskGroupRepository.findById(gid).ifPresent(g -> {
                    if (g.getTasksList() != null) {
                        g.getTasksList().remove(task.getId());
                        taskGroupRepository.save(g);
                    }
                });
            }

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
