package ru.t1.java.demo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.kafka.KafkaCreationTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.ErrorTransaction;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.repository.ErrorTransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionRetryService;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransactionRetryServiceImpl implements TransactionRetryService {
    @Autowired
    private ErrorTransactionRepository errorTransactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private KafkaCreationTransactionProducer kafkaCreationTransactionProducer;

    @Autowired
    private AccountService accountService;

    @Scheduled(fixedDelayString = "${retry.interval}")
    public void retryFailedTransactions() {
        log.warn("Транзакции из БД были отправлены на повторную обработку");
        List<ErrorTransaction> failedTransactions = errorTransactionRepository.findAll();
        for (ErrorTransaction transaction : failedTransactions) {
            Optional<Transaction> foundTransaction = transactionService.findById(transaction.getTransactionId());
            if (foundTransaction.isPresent()) {

                Optional<Account> foundAccount = accountService.findById(foundTransaction.get().getAccountId().intValue());
                if (foundAccount.isPresent()) {
                    OperationInfoAbstractDto operationInfoAbstractDto = new OperationInfoAbstractDto();

                    operationInfoAbstractDto.setClientId(foundTransaction.get().getClientId());
                    operationInfoAbstractDto.setOperation(foundTransaction.get().getType());
                    operationInfoAbstractDto.setAmount(foundTransaction.get().getAmount());
                    operationInfoAbstractDto.setCardType(foundAccount.get().getType());

                    kafkaCreationTransactionProducer.send(operationInfoAbstractDto);

                }

            }
        }
    }
}
