package com.jashwanth_projects.organize_it_backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Document(collection = "taskGroups")
public class TaskGroup {

    @Id
    private String id;
    @NotBlank(message = "name must not be blank")
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    // store task IDs to avoid duplicating task documents
    private List<String> tasksList = new ArrayList<>();
    private String ownerId;
    private boolean isActive = true;

    public TaskGroup() {
    }

    public TaskGroup(String id, String name, String description, Instant createdAt, Instant updatedAt, List<String> tasksList, String ownerId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (tasksList != null) this.tasksList = tasksList;
        this.ownerId = ownerId;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<String> getTasksList() {
        return tasksList;
    }

    public void setTasksList(List<String> tasksList) {
        this.tasksList = tasksList;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskGroup taskGroup = (TaskGroup) o;
        return isActive == taskGroup.isActive && Objects.equals(id, taskGroup.id) && Objects.equals(name, taskGroup.name) && Objects.equals(description, taskGroup.description) && Objects.equals(createdAt, taskGroup.createdAt) && Objects.equals(updatedAt, taskGroup.updatedAt) && Objects.equals(tasksList, taskGroup.tasksList) && Objects.equals(ownerId, taskGroup.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, createdAt, updatedAt, tasksList, ownerId, isActive);
    }

    @Override
    public String toString() {
        return "TaskGroup{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", tasksList=" + tasksList +
                ", ownerId='" + ownerId + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
