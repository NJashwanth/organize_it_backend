package com.jashwanth_projects.organize_it_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.model.TaskGroupResponse;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskGroupControllerUnitTest {

    @Mock
    private TaskGroupRepository taskGroupRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskGroupController taskGroupController;

    @Test
    void getAllGroupsWithTasks_expandsTasksForEachGroup() {
        TaskGroup g1 = new TaskGroup("g1", "Group 1", "desc", Instant.now(), Instant.now(), null, null, true);
        Task task = new Task();
        task.setId("t1");

        when(taskGroupRepository.findAll()).thenReturn(List.of(g1));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(task));

        List<TaskGroupResponse> result = taskGroupController.getAllGroupsWithTasks();

        assertEquals(1, result.size());
        assertEquals("g1", result.get(0).getId());
        assertEquals("t1", result.get(0).getTasks().get(0).getId());
    }

    @Test
    void getGroupById_returnsGroupWithTasks() {
        TaskGroup g1 = new TaskGroup("g1", "Group 1", "desc", Instant.now(), Instant.now(), null, null, true);
        Task task = new Task();
        task.setId("t1");

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(g1));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(task));

        TaskGroupResponse result = taskGroupController.getGroupById("g1");

        assertEquals("g1", result.getId());
        assertEquals(1, result.getTasks().size());
    }

    @Test
    void getGroupById_throws_whenMissing() {
        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskGroupController.getGroupById("missing"));
    }

    @Test
    void createGroup_setsTimestamps_andInitializesTaskListWhenNull() {
        TaskGroup input = new TaskGroup();
        input.setName("Group 1");
        input.setTasksList(null);

        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskGroup created = taskGroupController.createGroup(input);

        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertNotNull(created.getTasksList());
    }

    @Test
    void createGroup_preservesTaskListWhenProvided() {
        TaskGroup input = new TaskGroup();
        input.setName("Group 1");
        input.setTasksList(new ArrayList<>(List.of("t1")));

        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskGroup created = taskGroupController.createGroup(input);

        assertEquals(1, created.getTasksList().size());
        assertEquals("t1", created.getTasksList().get(0));
    }

    @Test
    void updateGroup_updatesTaskList_whenProvided() {
        TaskGroup existing = new TaskGroup();
        existing.setId("g1");
        existing.setName("Old");
        existing.setTasksList(new ArrayList<>(List.of("old")));

        TaskGroup payload = new TaskGroup();
        payload.setName("New");
        payload.setDescription("New desc");
        payload.setTasksList(List.of("t1", "t2"));

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskGroup updated = taskGroupController.updateGroup("g1", payload);

        assertEquals("New", updated.getName());
        assertEquals(2, updated.getTasksList().size());
    }

    @Test
    void updateGroup_keepsTaskList_whenNotProvided() {
        TaskGroup existing = new TaskGroup();
        existing.setId("g1");
        existing.setTasksList(new ArrayList<>(List.of("t1")));

        TaskGroup payload = new TaskGroup();
        payload.setName("New");
        payload.setDescription("New desc");
        payload.setTasksList(null);

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskGroup updated = taskGroupController.updateGroup("g1", payload);

        assertEquals(1, updated.getTasksList().size());
        assertEquals("t1", updated.getTasksList().get(0));
    }

    @Test
    void updateGroup_throws_whenMissing() {
        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskGroupController.updateGroup("missing", new TaskGroup()));
    }

    @Test
    void deleteGroup_detachesTasksAndDeletes() {
        TaskGroup group = new TaskGroup();
        group.setId("g1");

        Task t1 = new Task();
        t1.setId("t1");
        t1.setGroupId("g1");

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(t1));

        String message = taskGroupController.deleteGroup("g1");

        assertTrue(message.contains("deleted"));
        assertEquals(null, t1.getGroupId());
        verify(taskRepository).save(t1);
        verify(taskGroupRepository).deleteById("g1");
    }

    @Test
    void deleteGroup_throws_whenMissing() {
        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskGroupController.deleteGroup("missing"));
        verify(taskRepository, never()).save(any(Task.class));
    }
}
