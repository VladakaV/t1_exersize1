package ru.t1.java.demo.mapper;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;

@Component
public class TransactionMapperImplem {
    public Transaction toEntity(TransactionDto transactionDto) {
        Transaction transaction = new Transaction();

        transaction.setClient_id(transactionDto.getClientId());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setAccount_id(transactionDto.getAccount_id());

        return transaction;
    }
}
