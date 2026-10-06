package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.service.TaskService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.example.taskmanager.exception.TaskNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.mockito.Mockito.doThrow;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {

        // Arrange
        TaskResponse response = new TaskResponse(
                1L,
                "Test Task",
                "Test Description",
                false,
                1L
        );

        when(taskService.createTask(any(TaskRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Test Task",
                                            "description": "Test Description",
                                            "completed": false,
                                            "userId": 1
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.userId").value(1));
    }


    @Test
    void createTask_shouldReturnBadRequestWhenUserIdIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "Test Task",
                                        "description": "Test Description",
                                        "completed": false
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verify(
                taskService,
                never()
        ).createTask(any(TaskRequest.class));
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {

        // Arrange
        TaskResponse response = new TaskResponse(
                1L,
                "Test Task",
                "Test Description",
                false,
                1L
        );

        when(taskService.getTaskById(1L))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(
                        get("/api/tasks/{id}", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getTaskById_shouldReturnNotFoundWhenTaskDoesNotExist()
            throws Exception {

        // Arrange
        when(taskService.getTaskById(999L))
                .thenThrow(
                        new TaskNotFoundException(
                                "Task not found with id: 999"
                        )
                );

        // Act & Assert
        mockMvc.perform(
                        get("/api/tasks/{id}", 999L)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() throws Exception {

        // Arrange
        TaskResponse response = new TaskResponse(
                2L,
                "Updated Task",
                "Updated Description",
                true,
                1L
        );

        when(taskService.updateTask(
                any(Long.class),
                any(TaskRequest.class)
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(
                        put("/api/tasks/{id}", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "Updated Task",
                                        "description": "Updated Description",
                                        "completed": true,
                                        "userId": 1
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }


    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {

        // Act & Assert
        mockMvc.perform(
                        delete("/api/tasks/{id}", 2L)
                )
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(2L);
    }

    @Test
    void deleteTask_shouldReturnNotFoundWhenTaskDoesNotExist()
            throws Exception {

        // Arrange
        doThrow(
                new TaskNotFoundException(
                        "Task not found with id: 999"
                )
        ).when(taskService).deleteTask(999L);

        // Act & Assert
        mockMvc.perform(
                        delete("/api/tasks/{id}", 999L)
                )
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(999L);
    }

    @Test
    void updateTask_shouldReturnBadRequestWhenUserIdIsMissing()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/{id}", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "Updated Task",
                                        "description": "Updated Description",
                                        "completed": true
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verify(
                taskService,
                never()
        ).updateTask(
                any(Long.class),
                any(TaskRequest.class)
        );
    }
}