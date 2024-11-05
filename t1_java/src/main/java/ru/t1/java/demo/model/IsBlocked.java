package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blocked")
public class IsBlocked {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name =  "account_id")
    private Long accountId;

    @Column(name = "is_blocked")
    private boolean isBlocked;

}
