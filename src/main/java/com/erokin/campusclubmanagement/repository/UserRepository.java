package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByStudentNumber(String studentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentNumber(String studentNumber);
}

