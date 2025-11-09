package com.jashwanth_projects.organize_it_backend.model;


import java.time.Instant;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Document(collection = "tasks") // This tells Spring Data MongoDB that this entity should be stored in the 'tasks' collection
public class Task {

    @Id
    private String id;
    private String title;
    private String description;
    private boolean isCompleted;
    // ID of the TaskGroup this task belongs to (optional)
    private String groupId;
    // Owner of the task (user id or similar)
    @NotBlank(message = "ownerId must not be blank")
    private String ownerId;
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private TaskPriority priority;

    // No-arg constructor required by some frameworks
    public Task() {
    }

    // Full constructor
    public Task(String id, String title, String description, boolean isCompleted, TaskPriority priority, String groupId, String ownerId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.groupId = groupId;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    // Override equals() and hashCode() to properly compare Task objects
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return isCompleted == task.isCompleted &&
                Objects.equals(id, task.id) &&
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                Objects.equals(groupId, task.groupId) &&
                Objects.equals(ownerId, task.ownerId) &&
                Objects.equals(createdAt, task.createdAt) &&
                Objects.equals(updatedAt, task.updatedAt) &&
                priority == task.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, isCompleted, groupId, ownerId, createdAt, updatedAt, priority);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isCompleted=" + isCompleted +
                ", groupId='" + groupId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", priority=" + priority +
                '}';
    }
}
