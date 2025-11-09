package com.jashwanth_projects.organize_it_backend.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.model.TaskGroupResponse;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/task-groups")
public class TaskGroupController {

    @Autowired
    private TaskGroupRepository taskGroupRepository;

    @Autowired
    private TaskRepository taskRepository;

    // Fetch all groups and include the full task objects for each group
    @GetMapping
    public List<TaskGroupResponse> getAllGroupsWithTasks() {
        List<TaskGroup> groups = taskGroupRepository.findAll();
        List<TaskGroupResponse> result = new ArrayList<>();

        for (TaskGroup group : groups) {
            List<Task> tasks = taskRepository.findByGroupId(group.getId());
            TaskGroupResponse resp = new TaskGroupResponse(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getCreatedAt(),
                    group.getUpdatedAt(),
                    tasks
            );
            result.add(resp);
        }

        return result;
    }

    // Get a single group by id (with tasks)
    @GetMapping("/{id}")
    public TaskGroupResponse getGroupById(@PathVariable String id) {
        TaskGroup group = taskGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("Group not found: " + id));
        List<Task> tasks = taskRepository.findByGroupId(group.getId());
        return new TaskGroupResponse(group.getId(), group.getName(), group.getDescription(), group.getCreatedAt(), group.getUpdatedAt(), tasks);
    }

    // Create a new group
    @PostMapping
    public TaskGroup createGroup(@Valid @RequestBody TaskGroup group) {
        Instant now = Instant.now();
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        if (group.getTasksList() == null) {
            group.setTasksList(new ArrayList<>());
        }
        return taskGroupRepository.save(group);
    }

    // Update an existing group
    @PutMapping("/{id}")
    public TaskGroup updateGroup(@PathVariable String id, @Valid @RequestBody TaskGroup groupDetails) {
        TaskGroup group = taskGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("Group not found: " + id));
        group.setName(groupDetails.getName());
        group.setDescription(groupDetails.getDescription());
        group.setUpdatedAt(Instant.now());
        // optionally update task list only if provided
        if (groupDetails.getTasksList() != null) {
            group.setTasksList(groupDetails.getTasksList());
        }
        return taskGroupRepository.save(group);
    }

    // Delete a group
    @DeleteMapping("/{id}")
    public String deleteGroup(@PathVariable String id) {
        TaskGroup group = taskGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("Group not found: " + id));
        // Optionally: clear groupId from member tasks
        List<Task> tasks = taskRepository.findByGroupId(group.getId());
        for (Task t : tasks) {
            t.setGroupId(null);
            taskRepository.save(t);
        }
        taskGroupRepository.deleteById(id);
        return "Group " + id + " deleted";
    }
}
