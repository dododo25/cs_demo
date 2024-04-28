package com.cs.demo.service;

import com.cs.demo.model.UserData;
import com.cs.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@SpringBootTest
class UserServiceTests {

    @Autowired
    private UserService userService;

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
    void testFindAllShouldReturnObject() {
        UserData user1 = userRepository.save(userData);
        UserData user2 = userRepository.save(UserData.Builder.basedAt(userData)
                .setId(null)
                .setMail("another.test@example.com")
                .setBirthDate(LocalDate.now())
                .build());

        List<UserData> users = userService.findAll();

        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals(user1.getId(), users.get(0).getId());
        Assertions.assertEquals(user2.getId(), users.get(1).getId());
    }

    @Test
    void testFindAllByRangeShouldReturnObject() {
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

        List<UserData> users = userService.findAllByRange(
                LocalDate.of(1999, 12, 1),
                LocalDate.of(2001, 1, 1));

        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals(user1.getId(), users.get(0).getId());
        Assertions.assertEquals(user2.getId(), users.get(1).getId());
    }

    @Test
    void testFindByIdShouldReturnObject() {
        UserData user = userRepository.save(userData);

        Assertions.assertTrue(userService.findById(user.getId())
                .map(u -> Objects.equals(u.getId(), user.getId()))
                .orElse(false));
    }

    @Test
    void testFindUserByMailShouldReturnObject() {
        UserData user = userRepository.save(userData);

        Assertions.assertTrue(userService.findByMail(user.getMail())
                .map(u -> Objects.equals(u.getId(), user.getId()))
                .orElse(false));
    }

    @Test
    void testSaveShouldReturnObject() {
        UserData user = userService.save(userData);

        Assertions.assertEquals(user.getMail(), userData.getMail());
        Assertions.assertEquals(user.getFirstName(), userData.getFirstName());
        Assertions.assertEquals(user.getLastName(), userData.getLastName());
        Assertions.assertEquals(user.getBirthDate(), userData.getBirthDate());
        Assertions.assertEquals(user.getAddress(), userData.getAddress());
        Assertions.assertEquals(user.getTel(), userData.getTel());
    }

    @Test
    void testDeleteByIdShouldReturnObject() {
        UserData user = userRepository.save(userData);

        userService.deleteById(user.getId());
        userService.findById(user.getId())
                .ifPresent(u -> Assertions.fail());
    }
}
