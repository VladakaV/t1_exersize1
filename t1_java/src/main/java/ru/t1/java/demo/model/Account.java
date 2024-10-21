package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.t1.java.demo.model.enums.TypeEnum;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Long id;

   @ManyToOne
   @JoinColumn(name = "client_id", referencedColumnName = "id")
   private Client client;

   @Column(name = "type")
   private TypeEnum type;

   @Column(name = "balance")
   private BigDecimal balance;
}
