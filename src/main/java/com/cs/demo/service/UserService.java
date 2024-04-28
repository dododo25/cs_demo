package com.cs.demo.service;

import com.cs.demo.model.UserData;
import com.cs.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public List<UserData> findAll() {
        return repository.findAll();
    }

    public List<UserData> findAllByRange(LocalDate from, LocalDate to) {
        return repository.fillAllByRange(from, to);
    }

    public Optional<UserData> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<UserData> findByMail(String mail) {
        return repository.findByMail(mail);
    }

    public UserData save(UserData data) {
        return repository.save(data);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
