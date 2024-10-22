package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;
import ru.t1.java.demo.model.enums.TypeEnum;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Long id;

//   @ManyToOne
//   @JoinColumn(name = "client_id", referencedColumnName = "id")
   @Column(name = "client_id")
   private Long client_id;

   @Column(name = "type")
   private TypeEnum type;

   @Column(name = "balance")
   private BigDecimal balance;
}
