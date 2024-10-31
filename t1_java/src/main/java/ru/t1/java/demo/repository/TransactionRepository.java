package ru.t1.java.demo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TypeEnum;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
//    public Optional<Transaction> findByClientIdAndAccountId(Long clientId, Long accountId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT t FROM Transaction t WHERE t.clientId = :clientId AND t.accountId = :accountId ORDER BY t.id DESC LIMIT 1")
    Optional<Transaction> findLatestByClientIdAndAccountId(Long clientId, Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<Transaction> findTopByOrderByIdDesc();

}