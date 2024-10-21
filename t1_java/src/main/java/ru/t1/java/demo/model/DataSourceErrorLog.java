package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Getter
@Setter
@Table(name = "data_source")
public class DataSourceErrorLog extends AbstractPersistable<Long> {
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "message")
    private String message;

    @Column(name = "method_signature")
    private String methodSignature;
}
