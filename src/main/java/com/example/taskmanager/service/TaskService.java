package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.specification.TaskSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Page<TaskResponse> getAllTasks(
            String keyword,
            Boolean completed,
            Pageable pageable
    ) {

        Page<Task> tasks =
                taskRepository.findAll(
                        TaskSpecification.filter(
                                keyword,
                                completed
                        ),
                        pageable
                );

        return tasks.map(this::convertToResponse);
    }

    public TaskResponse getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        return convertToResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
//        throw new RuntimeException("Intentional failure for transaction rollback test"); - Transaction rollback check
        return convertToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(
            Long id,
            TaskRequest request
    ) {

        Task task = taskRepository.findById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());
        task.setUser(user);

        Task updatedTask = taskRepository.save(task);

        return convertToResponse(updatedTask);
    }

    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        taskRepository.delete(task);
    }

    private TaskResponse convertToResponse(Task task) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getUser().getId()
        );
    }
}