package ru.t1.java.demo.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Getter
@Setter
public class TimeLimitExceedLog extends AbstractPersistable<Long> {
    private String methodSignature;

    private long executionTime;
}
