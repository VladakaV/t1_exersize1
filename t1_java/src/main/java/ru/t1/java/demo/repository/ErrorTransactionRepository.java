package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.java.demo.model.ErrorTransaction;

public interface ErrorTransactionRepository extends JpaRepository<ErrorTransaction, Long> {

}
