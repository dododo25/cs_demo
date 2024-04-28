package com.cs.demo.controller;

import com.cs.demo.exception.UserControllerBadRequestException;
import com.cs.demo.service.UserService;
import com.cs.demo.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
public class UserController {

    private static final String MAIL_PATTERN_VALID_SYMBOLS_PART = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+";

    private static final String MAIL_PATTERN_VALID_DOMAIN_PART = "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

    private static final Pattern MAIL_PATTERN = Pattern.compile(String.format("^%1$s(?:\\.%1$s)*@(?:%2$s\\.)+%2$s$",
            MAIL_PATTERN_VALID_SYMBOLS_PART, MAIL_PATTERN_VALID_DOMAIN_PART));

    @Autowired
    private UserService service;

    @Value("${spring.application.minAge}")
    private int minAge;

    @GetMapping(value = "/users")
    public List<UserData> getAllUsers() {
        return service.findAll();
    }

    @GetMapping(value = "/users", params = {"from", "to"})
    public List<UserData> getAllUsersByRange(@RequestParam("from") LocalDate from, @RequestParam("to") LocalDate to) {
        if (from.isAfter(to)) {
            throw new UserControllerBadRequestException("from value must be less than to value");
        }

        return service.findAllByRange(from, to);
    }

    @GetMapping(value = "/users/{id}")
    public UserData getUserById(@PathVariable("id") Long id) {
        return service.findById(id)
                .orElseThrow(() -> new UserControllerBadRequestException("unknown id"));
    }

    @PostMapping(value = "/users")
    public UserData addUser(@RequestBody UserData userData) {
        return validateAndInsertUser(userData, false);
    }

    @PutMapping(value = "/users/{id}")
    public UserData updateUser(@PathVariable("id") Long id, @RequestBody UserData userData) {
        service.findById(id).ifPresent(data ->
                userData.setId(data.getId()));

        return validateAndInsertUser(userData, true);
    }

    @PatchMapping("/users/{id}")
    public UserData editUser(@PathVariable("id") Long id, @RequestBody UserData userData) {
        UserData data = service.findById(id).orElse(null);

        if (data == null) {
            throw new UserControllerBadRequestException("unknown user");
        }

        data = UserData.Builder.basedAt(data)
                .setId(data.getId())
                .setMail(userData.getMail() != null ? userData.getMail() : data.getMail())
                .setFirstName(userData.getFirstName() != null ? userData.getFirstName() : data.getFirstName())
                .setLastName(userData.getLastName() != null ? userData.getLastName() : data.getLastName())
                .setBirthDate(userData.getBirthDate() != null ? userData.getBirthDate() : data.getBirthDate())
                .setAddress(userData.getAddress() != null ? userData.getAddress() : data.getAddress())
                .setTel(userData.getTel() != null ? userData.getTel() : data.getTel())
                .build();

        return validateAndInsertUser(data, true);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    private UserData validateAndInsertUser(UserData userData, boolean additionalMailCheck) {
        validateUserMail(userData, additionalMailCheck);
        validateUserFirstName(userData);
        validateUserLastName(userData);
        validateUserAge(userData);

        return service.save(userData);
    }

    private void validateUserMail(UserData userData, boolean additionalCheck) {
        String mail = userData.getMail();

        if (mail == null) {
            throw new UserControllerBadRequestException("unknown email value");
        }

        if (!MAIL_PATTERN.matcher(mail).find()) {
            throw new UserControllerBadRequestException("invalid email regex");
        }

        UserData userDataToFound = service.findByMail(mail).orElse(null);

        if (userDataToFound != null) {
            if (additionalCheck && Objects.equals(userData.getId(), userDataToFound.getId())) {
                return;
            }

            throw new UserControllerBadRequestException("user with this email already exists");
        }
    }

    private void validateUserFirstName(UserData userData) {
        String firstName = userData.getFirstName();

        if (firstName == null) {
            throw new UserControllerBadRequestException("unknown first name value");
        }
    }

    private void validateUserLastName(UserData userData) {
        String lastName = userData.getLastName();

        if (lastName == null) {
            throw new UserControllerBadRequestException("unknown last name value");
        }
    }

    private void validateUserAge(UserData userData) {
        LocalDate birthDate = userData.getBirthDate();

        if (birthDate == null) {
            throw new UserControllerBadRequestException("unknown birth date value");
        }

        LocalDate date = LocalDate.now();
        int age = date.minusYears(birthDate.getYear()).getYear();

        if (age < 0) {
            throw new UserControllerBadRequestException("enter a valid birth date");
        }

        if (age < minAge) {
            throw new UserControllerBadRequestException("user`s age must be more than 18");
        }
    }
}
