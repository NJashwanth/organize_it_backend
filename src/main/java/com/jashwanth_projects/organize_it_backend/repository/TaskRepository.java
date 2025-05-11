package com.jashwanth_projects.organize_it_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jashwanth_projects.organize_it_backend.model.Task;

public interface TaskRepository extends MongoRepository<Task, String> {
    // You can define custom queries if needed
}
