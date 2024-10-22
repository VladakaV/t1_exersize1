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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

//    @ManyToOne
//    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @Column(name = "cliend_id")
    private Long client_id;

//    @ManyToOne
//    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @Column(name = "account_id")
    private Long account_id;

}