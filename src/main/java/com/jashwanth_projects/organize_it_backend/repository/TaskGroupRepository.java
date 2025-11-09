package com.jashwanth_projects.organize_it_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jashwanth_projects.organize_it_backend.model.TaskGroup;

public interface TaskGroupRepository extends MongoRepository<TaskGroup, String> {
    // Additional query methods for TaskGroup can be added here when needed
}
