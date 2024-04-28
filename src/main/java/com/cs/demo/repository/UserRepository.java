package com.cs.demo.repository;

import com.cs.demo.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {

    @Query("SELECT u FROM UserData u WHERE u.mail = ?1")
    Optional<UserData> findByMail(String mail);

    @Query("SELECT u FROM UserData u WHERE ?1 <= u.birthDate AND u.birthDate < ?2")
    List<UserData> fillAllByRange(LocalDate from, LocalDate to);

}
