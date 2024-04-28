package com.cs.demo.controller;

import com.cs.demo.model.UserData;
import com.cs.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UserData userData;

    @BeforeEach
    void setup() {
        userData = UserData.Builder
                .fromScratch()
                .setMail("test@example.com")
                .setFirstName("firstName")
                .setLastName("lastName")
                .setBirthDate(LocalDate.of(1999, 12, 31))
                .setAddress("Example str., 1")
                .setTel("+1555232323")
                .build();
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        userData = null;
    }

    @Test
    void testGetAllUsersShouldDoneWell() throws Exception {
        userRepository.save(userData);
        userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.now())
                .build());

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testFindAllUsersByRangeShouldDoneWell() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build());

        userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("yetAnotherTest@example.com")
                .setBirthDate(LocalDate.of(2001, 1, 1))
                .build());

        mockMvc.perform(get(String.format("/users?from=%s&to=%s",
                        LocalDate.of(1999, 12, 1),
                        LocalDate.of(2001, 1, 1)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]['id']").value(user1.getId()))
                .andExpect(jsonPath("$[1]['id']").value(user2.getId()));
    }

    @Test
    void testFindAllUsersShouldGet4xxErrorWhenRangeIsInvalid() throws Exception {
        mockMvc.perform(get(String.format("/users?from=%s&to=%s",
                        LocalDate.of(2001, 1, 1),
                        LocalDate.of(1999, 12, 1)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testFindUserByIdShouldDoneWell() throws Exception {
        UserData user = userRepository.save(userData);

        mockMvc.perform(get(String.format("/users/%d", user.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['id']").value(user.getId()))
                .andExpect(jsonPath("$['mail']").value(user.getMail()))
                .andExpect(jsonPath("$['firstName']").value(user.getFirstName()))
                .andExpect(jsonPath("$['lastName']").value(user.getLastName()))
                .andExpect(jsonPath("$['birthDate']").value(user.getBirthDate().toString()));
    }

    @Test
    void testAddNewUserShouldDoneWell() throws Exception {
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['id']").exists())
                .andExpect(jsonPath("$['mail']").value(userData.getMail()))
                .andExpect(jsonPath("$['firstName']").value(userData.getFirstName()))
                .andExpect(jsonPath("$['lastName']").value(userData.getLastName()))
                .andExpect(jsonPath("$['birthDate']").value(userData.getBirthDate().toString()));
    }

    @Test
    void testAddUserShouldGet4xxErrorWhenEmailIsInvalid() throws Exception {
        userRepository.save(userData);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddUserShouldGet4xxErrorWhenBirthDateInvalid() throws Exception {
        UserData user1 = UserData.Builder.basedAt(userData)
                .setBirthDate(LocalDate.now())
                .build();
        UserData user2 = UserData.Builder.basedAt(userData)
                .setBirthDate(LocalDate.now().plusYears(1))
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testUpdateExistingUserShouldDoneWell() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.basedAt(userData)
                .setId(user1.getId())
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build();

        mockMvc.perform(put(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['id']").value(user2.getId()))
                .andExpect(jsonPath("$['mail']").value(user2.getMail()))
                .andExpect(jsonPath("$['firstName']").value(user2.getFirstName()))
                .andExpect(jsonPath("$['lastName']").value(user2.getLastName()))
                .andExpect(jsonPath("$['birthDate']").value(user2.getBirthDate().toString()));
    }

    @Test
    void testUpdateExistingUserShouldGet4xxErrorWhenEmailIsInvalid() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build();

        userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build());

        mockMvc.perform(put(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testUpdateExistingUserShouldGet4xxErrorWhenAgeIsInvalid() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.basedAt(userData)
                .setId(null)
                .setBirthDate(LocalDate.now())
                .build();

        userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build());

        mockMvc.perform(put(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testEditExistingUserShouldDoneWell() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.fromScratch()
                .setFirstName("anotherFirstName")
                .build();

        mockMvc.perform(patch(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['id']").value(user1.getId()))
                .andExpect(jsonPath("$['mail']").value(user1.getMail()))
                .andExpect(jsonPath("$['firstName']").value(user2.getFirstName()))
                .andExpect(jsonPath("$['lastName']").value(user1.getLastName()))
                .andExpect(jsonPath("$['birthDate']").value(user1.getBirthDate().toString()));
    }

    @Test
    void testEditShouldGet4xxErrorWhenUserNotExists() throws Exception {
        UserData user2 = UserData.Builder.fromScratch()
                .setBirthDate(LocalDate.now())
                .build();

        mockMvc.perform(patch(String.format("/users/%d", 0))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testEditExistingUserShouldGet4xxErrorWhenEmailIsInvalid() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.fromScratch()
                .setMail("another.test@example.com")
                .build();

        userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.of(2000, 12, 31))
                .build());

        mockMvc.perform(patch(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testEditExistingUserShouldGet4xxErrorWhenAgeIsInvalid() throws Exception {
        UserData user1 = userRepository.save(userData);
        UserData user2 = UserData.Builder.fromScratch()
                .setBirthDate(LocalDate.now())
                .build();

        mockMvc.perform(patch(String.format("/users/%d", user1.getId()))
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDeleteExistingUserShouldDoneWell() throws Exception {
        userData = userRepository.save(userData);

        mockMvc.perform(delete(String.format("/users/%d", userData.getId()))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get(String.format("/users/%d", userData.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
