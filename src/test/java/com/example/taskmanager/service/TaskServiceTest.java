package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.example.taskmanager.exception.TaskNotFoundException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldCreateTaskSuccessfully() {

        // Arrange
        TaskRequest request = new TaskRequest();

        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setCompleted(false);
        request.setUserId(1L);

        User user = new User();

        user.setId(1L);
        user.setName("Rahul");
        user.setEmail("rahul@example.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Task savedTask = new Task();

        savedTask.setId(1L);
        savedTask.setTitle("Test Task");
        savedTask.setDescription("Test Description");
        savedTask.setCompleted(false);
        savedTask.setUser(user);

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        // Act
        TaskResponse response =
                taskService.createTask(request);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertFalse(response.isCompleted());
        assertEquals(1L, response.getUserId());
    }
    @Test
    void createTask_shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange
        TaskRequest request = new TaskRequest();

        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setCompleted(false);
        request.setUserId(999L);

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception =
                assertThrows(
                        TaskNotFoundException.class,
                        () -> taskService.createTask(request)
                );

        assertEquals(
                "User not found with id: 999",
                exception.getMessage()
        );
        verify(taskRepository, org.mockito.Mockito.never())
                .save(any(Task.class));
    }

    @Test
    void updateTask_shouldUpdateTaskSuccessfully() {

        // Arrange
        Long taskId = 2L;

        TaskRequest request = new TaskRequest();

        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setCompleted(true);
        request.setUserId(1L);

        User user = new User();

        user.setId(1L);
        user.setName("Rahul");
        user.setEmail("rahul@example.com");

        Task existingTask = new Task();

        existingTask.setId(taskId);
        existingTask.setTitle("Old Task");
        existingTask.setDescription("Old Description");
        existingTask.setCompleted(false);
        existingTask.setUser(user);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(existingTask));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(existingTask);

        // Act
        TaskResponse response =
                taskService.updateTask(taskId, request);

        // Assert
        assertEquals(2L, response.getId());
        assertEquals("Updated Task", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertEquals(true, response.isCompleted());
        assertEquals(1L, response.getUserId());

        verify(taskRepository).save(existingTask);
    }


    @Test
    void updateTask_shouldThrowExceptionWhenTaskDoesNotExist() {

        // Arrange
        Long taskId = 999L;

        TaskRequest request = new TaskRequest();

        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setCompleted(true);
        request.setUserId(1L);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception =
                assertThrows(
                        TaskNotFoundException.class,
                        () -> taskService.updateTask(taskId, request)
                );

        assertEquals(
                "Task not found with id: 999",
                exception.getMessage()
        );

        verify(userRepository, org.mockito.Mockito.never())
                .findById(1L);

        verify(taskRepository, org.mockito.Mockito.never())
                .save(any(Task.class));
    }

}