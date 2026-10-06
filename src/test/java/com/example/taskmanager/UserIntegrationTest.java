package com.example.taskmanager;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldCreateUser() throws Exception {

        String requestBody = """
                {
                    "name": "Integration User",
                    "email": "integration@example.com"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        assertThat(userRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldCreateTaskForUser() throws Exception {

        User user = new User();

        user.setName("Task User");
        user.setEmail("taskuser@example.com");

        User savedUser = userRepository.save(user);

        String requestBody = """
                {
                    "title": "Integration Task",
                    "description": "Test task with user relationship",
                    "completed": false,
                    "userId": %d
                }
                """.formatted(savedUser.getId());

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        assertThat(taskRepository.count())
                .isEqualTo(1);

        Task savedTask = taskRepository.findAll()
                .get(0);

        assertThat(savedTask.getTitle())
                .isEqualTo("Integration Task");

        assertThat(savedTask.getUser())
                .isNotNull();

        assertThat(savedTask.getUser().getId())
                .isEqualTo(savedUser.getId());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        String requestBody = """
            {
                "title": "Invalid User Task",
                "description": "Task with non-existent user",
                "completed": false,
                "userId": 999999
            }
            """;

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenTaskRequestIsInvalid() throws Exception {

        String requestBody = """
            {
                "title": "",
                "description": "",
                "completed": false,
                "userId": null
            }
            """;

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetTaskById() throws Exception {

        User user = new User();

        user.setName("Get Task User");
        user.setEmail("gettask@example.com");

        User savedUser = userRepository.save(user);

        Task task = new Task();

        task.setTitle("Get Task");
        task.setDescription("Testing task retrieval");
        task.setCompleted(false);
        task.setUser(savedUser);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/tasks/{id}", savedTask.getId())
                )
                .andExpect(status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.id")
                                .value(savedTask.getId())
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.title")
                                .value("Get Task")
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.userId")
                                .value(savedUser.getId())
                );
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/tasks/{id}", 999999)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTask() throws Exception {

        User user = new User();

        user.setName("Update User");
        user.setEmail("update@example.com");

        User savedUser = userRepository.save(user);

        Task task = new Task();

        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setCompleted(false);
        task.setUser(savedUser);

        Task savedTask = taskRepository.save(task);

        String requestBody = """
            {
                "title": "Updated Title",
                "description": "Updated Description",
                "completed": true,
                "userId": %d
            }
            """.formatted(savedUser.getId());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/tasks/{id}", savedTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.title")
                                .value("Updated Title")
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.completed")
                                .value(true)
                );

        Task updatedTask = taskRepository
                .findById(savedTask.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle())
                .isEqualTo("Updated Title");

        assertThat(updatedTask.getDescription())
                .isEqualTo("Updated Description");

        assertThat(updatedTask.isCompleted())
                .isTrue();

        assertThat(updatedTask.getUser().getId())
                .isEqualTo(savedUser.getId());
    }


    @Test
    void shouldDeleteTask() throws Exception {

        User user = new User();

        user.setName("Delete User");
        user.setEmail("delete@example.com");

        User savedUser = userRepository.save(user);

        Task task = new Task();

        task.setTitle("Delete Task");
        task.setDescription("Task to be deleted");
        task.setCompleted(false);
        task.setUser(savedUser);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/tasks/{id}", savedTask.getId())
                )
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(savedTask.getId()))
                .isEmpty();
    }


}