package com.jashwanth_projects.organize_it_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.model.TaskPriority;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskControllerUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskGroupRepository taskGroupRepository;

    @InjectMocks
    private TaskController taskController;

    @Test
    void createTask_setsTimestamps_andAddsTaskToGroup() {
        Task input = new Task();
        input.setTitle("T1");
        input.setGroupId("g1");

        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setTasksList(new ArrayList<>());

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId("t1");
            return t;
        });
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));

        Task created = taskController.createTask(input);

        assertEquals("t1", created.getId());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertTrue(group.getTasksList().contains("t1"));
        verify(taskGroupRepository).save(group);
    }

    @Test
    void createTask_keepsCreatedAt_whenProvided() {
        Task input = new Task();
        Instant fixed = Instant.parse("2026-01-01T00:00:00Z");
        input.setCreatedAt(fixed);

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task created = taskController.createTask(input);

        assertEquals(fixed, created.getCreatedAt());
    }

    @Test
    void createTask_groupAlreadyContainsTask_doesNotSaveGroup() {
        Task input = new Task();
        input.setGroupId("g1");

        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setTasksList(new ArrayList<>(List.of("t1")));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId("t1");
            return t;
        });
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));

        taskController.createTask(input);

        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    void updateTask_movesBetweenGroups_whenGroupChanges() {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        TaskGroup oldGroup = new TaskGroup();
        oldGroup.setId("g1");
        oldGroup.setTasksList(new ArrayList<>(List.of("t1", "t2")));

        TaskGroup newGroup = new TaskGroup();
        newGroup.setId("g2");
        newGroup.setTasksList(null);

        Task update = new Task();
        update.setTitle("Updated");
        update.setDescription("desc");
        update.setIsCompleted(true);
        update.setPriority(TaskPriority.HIGH);
        update.setGroupId("g2");

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(oldGroup));
        when(taskGroupRepository.findById("g2")).thenReturn(Optional.of(newGroup));

        Task saved = taskController.updateTaskupdateTask("t1", update);

        assertEquals("g2", saved.getGroupId());
        assertTrue(newGroup.getTasksList().contains("t1"));
        verify(taskGroupRepository, times(2)).save(any(TaskGroup.class));
    }

    @Test
    void updateTask_sameGroup_doesNotTouchGroupRepository() {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        Task update = new Task();
        update.setTitle("Updated");
        update.setGroupId("g1");

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task saved = taskController.updateTaskupdateTask("t1", update);

        assertEquals("g1", saved.getGroupId());
        verify(taskGroupRepository, never()).findById(any(String.class));
    }

    @Test
    void updateTask_throws_whenTaskMissing() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskController.updateTaskupdateTask("missing", new Task()));
    }

    @Test
    void deleteTask_detachesFromGroupAndDeletes() {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setTasksList(new ArrayList<>(List.of("t1")));

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));

        String message = taskController.deleteTask("t1");

        assertTrue(message.contains("deleted successfully"));
        verify(taskGroupRepository).save(group);
        verify(taskRepository).deleteById("t1");
    }

    @Test
    void deleteTask_withNoGroup_deletesTaskOnly() {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId(null);

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));

        taskController.deleteTask("t1");

        verify(taskRepository).deleteById("t1");
        verify(taskGroupRepository, never()).findById(any(String.class));
    }

    @Test
    void deleteTask_withGroupButNullTaskList_skipsGroupSave() {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setTasksList(null);

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));

        taskController.deleteTask("t1");

        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    void deleteTask_throws_whenMissing() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskController.deleteTask("missing"));
    }

    @Test
    void getTaskById_returnsTask_whenFound() {
        Task task = new Task();
        task.setId("t1");
        when(taskRepository.findById("t1")).thenReturn(Optional.of(task));

        Task found = taskController.getTaskById("t1");

        assertEquals("t1", found.getId());
    }

    @Test
    void getTaskById_throws_whenMissing() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskController.getTaskById("missing"));
    }
}
