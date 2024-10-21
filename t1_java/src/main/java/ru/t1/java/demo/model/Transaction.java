package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction extends AbstractPersistable<Long> {

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

}