package com.jashwanth_projects.organize_it_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jashwanth_projects.organize_it_backend.model.Task;

public interface TaskRepository extends MongoRepository<Task, String> {
    // Find tasks by their group id
    java.util.List<Task> findByGroupId(String groupId);
}
