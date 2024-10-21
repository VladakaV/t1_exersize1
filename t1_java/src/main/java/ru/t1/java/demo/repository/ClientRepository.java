package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.Transaction;
import ru.t1.java.demo.model.Client;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);

    @Transactional(propagation = NOT_SUPPORTED)
    @Transaction
    Client findClientByFirstName(String firstName);
}