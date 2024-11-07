package ru.t1.java.demo.service;


import ru.t1.java.demo.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {

    public void saveTransaction(Transaction transaction);

    public Optional<Transaction> findById(Long id);
}
