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

@SpringBootTest
@AutoConfigureMockMvc
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



}