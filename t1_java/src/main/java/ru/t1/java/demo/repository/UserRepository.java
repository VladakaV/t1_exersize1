package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String username);

    Boolean existsByLogin(String username);

    Boolean existsByEmail(String email);
}
