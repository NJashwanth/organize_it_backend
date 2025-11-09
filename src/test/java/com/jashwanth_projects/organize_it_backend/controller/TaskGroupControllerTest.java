package com.jashwanth_projects.organize_it_backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskGroupController.class)
public class TaskGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskGroupRepository taskGroupRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
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
}
