package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.java.demo.model.TimeLimitExceedLog;

public interface TimeLimitExceedLogRepository extends JpaRepository<TimeLimitExceedLog, Long> {
}
