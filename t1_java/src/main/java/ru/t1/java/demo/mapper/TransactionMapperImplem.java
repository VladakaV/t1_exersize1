package ru.t1.java.demo.mapper;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;

@Component
public class TransactionMapperImplem {
    public Transaction toEntity(TransactionDto transactionDto) {
        Transaction transaction = new Transaction();

        transaction.setClientId(transactionDto.getClientId());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setAccountId(transactionDto.getAccount_id());

        return transaction;
    }
}
