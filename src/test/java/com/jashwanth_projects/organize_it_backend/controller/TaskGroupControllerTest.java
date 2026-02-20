package com.jashwanth_projects.organize_it_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskGroupController.class)
public class TaskGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskGroupRepository taskGroupRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    @WithMockUser
    void getAllGroupsWithTasks_returnsGroupsAndTasks() throws Exception {
        TaskGroup g = new TaskGroup("g1", "Group 1", "desc", Instant.now(), Instant.now(), null, null, true);
        Task t = new Task();
        t.setId("t1");
        t.setTitle("T1");
        t.setDescription("d");
        t.setOwnerId("owner1");

        when(taskGroupRepository.findAll()).thenReturn(List.of(g));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(t));

        mockMvc.perform(get("/task-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("g1"))
                .andExpect(jsonPath("$[0].tasks[0].id").value("t1"));
    }

    @Test
    void taskGroupEndpoints_requireAuth() throws Exception {
        mockMvc.perform(get("/task-groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getGroupById_returnsGroupWithTasks() throws Exception {
        TaskGroup group = new TaskGroup("g1", "Group 1", "desc", Instant.now(), Instant.now(), null, null, true);
        Task task = new Task();
        task.setId("t1");

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(task));

        mockMvc.perform(get("/task-groups/g1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("g1"))
                .andExpect(jsonPath("$.tasks[0].id").value("t1"));
    }

    @Test
    @WithMockUser
    void getGroupById_returnsServerError_whenMissing() throws Exception {
        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(get("/task-groups/missing")));
    }

    @Test
    @WithMockUser
    void createGroup_initializesTasksListWhenNull() throws Exception {
        TaskGroup input = new TaskGroup();
        input.setName("My Group");
        input.setDescription("desc");
        input.setTasksList(null);

        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(invocation -> {
            TaskGroup g = invocation.getArgument(0);
            g.setId("g1");
            return g;
        });

        mockMvc.perform(post("/task-groups")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("g1"))
                .andExpect(jsonPath("$.tasksList").isArray());

        verify(taskGroupRepository).save(argThat(g -> g.getCreatedAt() != null && g.getUpdatedAt() != null));
    }

    @Test
    @WithMockUser
    void createGroup_returnsBadRequest_whenNameBlank() throws Exception {
        TaskGroup input = new TaskGroup();
        input.setName("   ");

        mockMvc.perform(post("/task-groups")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(taskGroupRepository, never()).save(any(TaskGroup.class));
    }

    @Test
    @WithMockUser
    void updateGroup_updatesProvidedTaskList() throws Exception {
        TaskGroup existing = new TaskGroup();
        existing.setId("g1");
        existing.setName("Old");
        existing.setTasksList(new ArrayList<>(List.of("t1")));

        TaskGroup payload = new TaskGroup();
        payload.setName("New Name");
        payload.setDescription("New Desc");
        payload.setTasksList(List.of("t2", "t3"));

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/task-groups/g1")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.tasksList[0]").value("t2"));
    }

    @Test
    @WithMockUser
    void updateGroup_keepsExistingTaskList_whenPayloadTaskListNull() throws Exception {
        TaskGroup existing = new TaskGroup();
        existing.setId("g1");
        existing.setName("Old");
        existing.setTasksList(new ArrayList<>(List.of("t1")));

        TaskGroup payload = new TaskGroup();
        payload.setName("New Name");
        payload.setDescription("New Desc");
        payload.setTasksList(null);

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(existing));
        when(taskGroupRepository.save(any(TaskGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/task-groups/g1")
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasksList[0]").value("t1"));
    }

    @Test
    @WithMockUser
    void updateGroup_returnsServerError_whenMissing() throws Exception {
        TaskGroup payload = new TaskGroup();
        payload.setName("New Name");

        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(put("/task-groups/missing")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))));
    }

    @Test
    @WithMockUser
    void deleteGroup_detachesTasksAndDeletesGroup() throws Exception {
        TaskGroup group = new TaskGroup();
        group.setId("g1");
        group.setName("Group 1");

        Task t1 = new Task();
        t1.setId("t1");
        t1.setGroupId("g1");

        Task t2 = new Task();
        t2.setId("t2");
        t2.setGroupId("g1");

        when(taskGroupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(taskRepository.findByGroupId("g1")).thenReturn(List.of(t1, t2));

        mockMvc.perform(delete("/task-groups/g1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Group g1 deleted"));

        verify(taskRepository).save(argThat(t -> "t1".equals(t.getId()) && t.getGroupId() == null));
        verify(taskRepository).save(argThat(t -> "t2".equals(t.getId()) && t.getGroupId() == null));
        verify(taskGroupRepository).deleteById("g1");
    }

    @Test
    @WithMockUser
    void deleteGroup_returnsServerError_whenMissing() throws Exception {
        when(taskGroupRepository.findById("missing")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
            () -> mockMvc.perform(delete("/task-groups/missing").with(csrf())));
    }
}
