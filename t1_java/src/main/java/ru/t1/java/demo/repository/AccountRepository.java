package ru.t1.java.demo.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.enums.TypeEnum;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<Account> findByClientIdAndType(Long clientId, TypeEnum type);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :balance WHERE a.id = :id")
    public void updateAccountBalanceById(Long id, BigDecimal balance);

}
