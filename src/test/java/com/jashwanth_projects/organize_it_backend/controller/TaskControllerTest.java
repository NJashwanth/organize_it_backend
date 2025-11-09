package com.jashwanth_projects.organize_it_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jashwanth_projects.organize_it_backend.model.Task;
import com.jashwanth_projects.organize_it_backend.model.TaskGroup;
import com.jashwanth_projects.organize_it_backend.repository.TaskGroupRepository;
import com.jashwanth_projects.organize_it_backend.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
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
    void createTask_addsTaskIdToGroup() throws Exception {
        Task input = new Task();
        input.setTitle("T1");
        input.setDescription("d");
        input.setOwnerId("owner1");
        input.setGroupId("g1");

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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"));

        ArgumentCaptor<TaskGroup> captor = ArgumentCaptor.forClass(TaskGroup.class);
        verify(taskGroupRepository).save(captor.capture());
        TaskGroup savedG = captor.getValue();
        assert(savedG.getTasksList().contains("t1"));
    }
}
