package com.jashwanth_projects.organize_it_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.model.TaskPriority;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@WithMockUser
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private TaskGroupRepository taskGroupRepository;

    @Test
    void getAllTasks_returnsAllTasks() throws Exception {
        Task first = new Task();
        first.setId("t1");
        first.setTitle("Task 1");

        Task second = new Task();
        second.setId("t2");
        second.setTitle("Task 2");

        when(taskRepository.findAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"))
                .andExpect(jsonPath("$[1].id").value("t2"));
    }

    @Test
    void createTask_addsTaskIdToGroup() throws Exception {
        Task input = new Task();
        input.setTitle("T1");
        input.setDescription("d");
        input.setOwnerId("owner1");
        input.setGroupId("g1");
        input.setPriority(TaskPriority.MEDIUM);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId("t1");
            return t;
        });

        TaskGroup g = new TaskGroup();
        g.setId("g1");
        g.setName("Group 1");
        g.setTasksList(new ArrayList<>());

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(g));

        mockMvc.perform(post("/tasks")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"));

        ArgumentCaptor<TaskGroup> captor = ArgumentCaptor.forClass(TaskGroup.class);
        verify(taskGroupRepository).save(captor.capture());
        TaskGroup savedG = captor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(savedG.getTasksList().contains("t1"));
    }

    @Test
    void createTask_withoutGroup_doesNotTouchGroupRepository() throws Exception {
        Task input = new Task();
        input.setTitle("T1");
        input.setDescription("d");
        input.setOwnerId("owner1");

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId("t1");
            return t;
        });

        mockMvc.perform(post("/tasks")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"));

        verify(taskGroupRepository, never()).findById(any(String.class));
        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    void getTaskById_returnsTask_whenFound() throws Exception {
        Task task = new Task();
        task.setId("t1");
        task.setTitle("T1");
        when(taskRepository.findById("t1")).thenReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"));
    }

    @Test
    void getTaskById_returnsServerError_whenNotFound() throws Exception {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(get("/tasks/missing")));
    }

    @Test
    void updateTask_updatesAndMovesAcrossGroups_whenGroupChanges() throws Exception {
        Task existing = new Task();
        existing.setId("t1");
        existing.setTitle("Old");
        existing.setGroupId("g1");

        Task payload = new Task();
        payload.setTitle("New");
        payload.setDescription("new desc");
        payload.setIsCompleted(true);
        payload.setPriority(TaskPriority.HIGH);
        payload.setGroupId("g2");

        TaskGroup oldGroup = new TaskGroup();
        oldGroup.setId("g1");
        oldGroup.setTasksList(new ArrayList<>(List.of("t1", "t2")));

        TaskGroup newGroup = new TaskGroup();
        newGroup.setId("g2");
        newGroup.setTasksList(new ArrayList<>());

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(oldGroup));
        when(taskGroupRepository.findById("g2")).thenReturn(Optional.of(newGroup));

        mockMvc.perform(put("/tasks/t1")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.groupId").value("g2"));

        verify(taskGroupRepository, times(2)).save(any(TaskGroup.class));
    }

    @Test
    void updateTask_returnsServerError_whenTaskMissing() throws Exception {
        Task payload = new Task();
        payload.setTitle("New");

        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(put("/tasks/missing")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))));
    }

    @Test
    void deleteTask_deletesAndRemovesFromGroup_whenGrouped() throws Exception {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setTasksList(new ArrayList<>(List.of("t1", "t2")));

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));

        mockMvc.perform(delete("/tasks/t1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Task with id t1 has been deleted successfully."));

        verify(taskGroupRepository).save(any(TaskGroup.class));
        verify(taskRepository).deleteById("t1");
    }

    @Test
    void deleteTask_returnsServerError_whenTaskMissing() throws Exception {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(delete("/tasks/missing").with(csrf())));
    }

    @Test
    void createTask_doesNotSaveGroup_whenGroupAlreadyContainsTask() throws Exception {
        Task input = new Task();
        input.setTitle("T1");
        input.setGroupId("g1");

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId("t1");
            return t;
        });

        TaskGroup g = new TaskGroup();
        g.setId("g1");
        g.setTasksList(new ArrayList<>(List.of("t1")));
        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(g));

        mockMvc.perform(post("/tasks").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());

        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    void updateTask_sameGroup_doesNotUpdateGroupLists() throws Exception {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId("g1");

        Task payload = new Task();
        payload.setTitle("New");
        payload.setGroupId("g1");

        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/tasks/t1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value("g1"));

        verify(taskGroupRepository, never()).findById(any(String.class));
        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    void deleteTask_withNoGroup_deletesTaskOnly() throws Exception {
        Task existing = new Task();
        existing.setId("t1");
        existing.setGroupId(null);
        when(taskRepository.findById("t1")).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/tasks/t1").with(csrf()))
                .andExpect(status().isOk());

        verify(taskRepository).deleteById("t1");
        verify(taskGroupRepository, never()).findById(any(String.class));
    }
}
