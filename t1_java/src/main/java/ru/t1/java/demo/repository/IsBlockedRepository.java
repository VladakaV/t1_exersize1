package ru.t1.java.demo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.IsBlocked;

import java.util.Optional;

public interface IsBlockedRepository  extends JpaRepository<IsBlocked, Long> {

    public Optional<IsBlocked> findByAccountId(Long id);

    @Modifying
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "UPDATE IsBlocked SET isBlocked = false WHERE accountId = :accountId")
    public void setUnblocked(Long accountId);

    @Modifying
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "UPDATE IsBlocked SET isBlocked = true WHERE accountId = :accountId")
    public void setBlocked(Long accountId);

}
