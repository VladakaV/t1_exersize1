package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.kafka.KafkaErrorTransactionsProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.IsBlocked;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.model.enums.OperationEnum;
import ru.t1.java.demo.model.enums.TypeEnum;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.IsBlockedRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionProcessingService;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingServiceImpl implements TransactionProcessingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IsBlockedRepository isBlockedRepository;
    private final KafkaErrorTransactionsProducer kafkaErrorTransactionsProducer;
    private final RestTemplate restTemplate;

    @Override
    public void processTransaction(OperationInfoAbstractDto transactionInfo) {

        Optional<Account> foundAccount = accountRepository.findByClientIdAndType(transactionInfo.getClientId(), transactionInfo.getCardType());

        if (foundAccount.isPresent()) {

            Transaction transaction = new Transaction();

            transaction.setAmount(transactionInfo.getAmount());
            transaction.setClientId(transactionInfo.getClientId());
            transaction.setAccountId(foundAccount.get().getId());
            transaction.setType(transactionInfo.getOperation());

            transactionRepository.save(transaction);

//            return ResponseEntity.ok(transaction);

            Optional<IsBlocked> isBlocked = isBlockedRepository.findByAccountId(foundAccount.get().getId());

            if (isBlocked.isPresent()) {

                if (isBlocked.get().isBlocked() == false) {

                    if (transactionInfo.getOperation() == OperationEnum.PLUS) {

                        accountRepository.updateAccountBalanceById(foundAccount.get().getId(),
                                foundAccount.get().getBalance().add(transactionInfo.getAmount()));
                    } else if (transactionInfo.getOperation() == OperationEnum.MINUS) {

                        if (foundAccount.get().getBalance().compareTo(transactionInfo.getAmount()) >= 0) {

                            accountRepository.updateAccountBalanceById(foundAccount.get().getId(),
                                    foundAccount.get().getBalance().subtract(transactionInfo.getAmount()));
                            // далее если по итогу кредитный счет <0, то он блокируется
                            Optional<Account> updatedAccount = accountRepository.findById(foundAccount.get().getId().intValue());

                            if (updatedAccount.get().getBalance().compareTo(BigDecimal.ZERO) <= 0
                                    && updatedAccount.get().getType() == TypeEnum.CREDIT_TYPE) {

                                block(updatedAccount.get().getId());
                                log.info("Аккаунт был заблокирован");

                            }

                        }
                        else {
                            log.info("Недостаточно средств");
                        }

                    }
                } else {
                    Long id = transactionRepository.findTopByOrderByIdDesc().
                            map(Transaction::getId).
                            orElse(null);

                    log.info("Found transaction ID: {}", id);

                    if (id != null) {

                        kafkaErrorTransactionsProducer.send(id);

                    }
                }
            }
        } else {
            // отдаем инфу в ответе от сервера, что счет не найден, перепроверьте заполненную инфу
            log.info("Счет не найден");
        }
    }

    @Override
    public void cancelOperation(OperationInfoAbstractDto transactionInfo) {
        log.info("HERE 2");
        Optional<Account> foundAccount = accountRepository.findByClientIdAndType(transactionInfo.getClientId(), transactionInfo.getCardType());

        if (foundAccount.isPresent()) {

            Optional<Transaction> foundTransaction = transactionRepository.findLatestByClientIdAndAccountId(transactionInfo.getClientId(), foundAccount.get().getId());

            if (foundTransaction.isPresent()) {

                OperationEnum operationType = foundTransaction.get().getType();

                switch (operationType) {
                    case PLUS:
                        accountRepository.updateAccountBalanceById(foundAccount.get().getId(), foundAccount.get().getBalance().add(foundTransaction.get().getAmount()));
                    case MINUS:
                        accountRepository.updateAccountBalanceById(foundAccount.get().getId(), foundAccount.get().getBalance().subtract(foundTransaction.get().getAmount()));
                }

                try {
                    transactionRepository.delete(foundTransaction.get());

                    log.info("Transaction deleted!");

                } catch (Exception e) {
                    e.printStackTrace();

                }
            } else {
            }
        } else {
            // отдаем инфу в ответе от сервера, что счет не найден, перепроверьте заполненную инфу
            log.info("Счет не найден");
        }
    }

    @Override
    public boolean unblock(Long transactionId) {

        Optional<Transaction> foundTransaction = transactionRepository.findById(transactionId);
        if (foundTransaction.isPresent()) {
            Optional<Account> foundAccount = accountRepository.findById(foundTransaction.get().getAccountId().intValue());

            if (foundAccount.isPresent()) {
                TypeEnum type = foundAccount.get().getType();

                if (type == TypeEnum.CREDIT_TYPE) {
                    if (foundAccount.get().getBalance().compareTo(foundTransaction.get().getAmount()) > 0
                            && foundTransaction.get().getType() == OperationEnum.MINUS) {
                        try {
                            isBlockedRepository.setUnblocked(foundAccount.get().getId());
                            // обработка транзакации повторно
                            OperationInfoAbstractDto operationInfoAbstractDto = new OperationInfoAbstractDto();

                            operationInfoAbstractDto.setOperation(foundTransaction.get().getType());
                            operationInfoAbstractDto.setAmount(foundTransaction.get().getAmount());
                            operationInfoAbstractDto.setClientId(foundAccount.get().getClientId());
                            operationInfoAbstractDto.setCardType(foundAccount.get().getType());

                            HttpHeaders headers = new HttpHeaders();
                            headers.set("Check", "Bearer "); // Генерация или получение JWT-токена
                            HttpEntity<OperationInfoAbstractDto> requestEntity = new HttpEntity<>(operationInfoAbstractDto, headers);


                            // Отправляем запрос на разблокировку счета
                            ResponseEntity<String> response = restTemplate.exchange(
                                     "http://localhost:8080/kafka/createTransaction",
                                    HttpMethod.POST,
                                    requestEntity,
                                    String.class
                            );

                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (foundTransaction.get().getType() == OperationEnum.PLUS
                            && foundAccount.get().getBalance().compareTo(foundTransaction.get().getAmount()) < 0) {
                        try {
                            isBlockedRepository.setUnblocked(foundAccount.get().getId());
                            // обработка транзакации повторно
                            OperationInfoAbstractDto operationInfoAbstractDto = new OperationInfoAbstractDto();

                            operationInfoAbstractDto.setOperation(foundTransaction.get().getType());
                            operationInfoAbstractDto.setAmount(foundTransaction.get().getAmount());
                            operationInfoAbstractDto.setClientId(foundAccount.get().getClientId());
                            operationInfoAbstractDto.setCardType(foundAccount.get().getType());

                            HttpHeaders headers = new HttpHeaders();
                            headers.set("Check", "Bearer "); // Генерация или получение JWT-токена

                            HttpEntity<OperationInfoAbstractDto> requestEntity = new HttpEntity<>(operationInfoAbstractDto, headers);

                            // Отправляем запрос на разблокировку счета
                            ResponseEntity<String> response = restTemplate.exchange(
                                    "http://localhost:8080/kafka/createTransaction",
                                    HttpMethod.POST,
                                    requestEntity,
                                    String.class
                            );

                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    else {
                        log.info("Счет не может быть разблокирован");
                        return false;
                    }
                }
                else if (type == TypeEnum.DEBET_TYPE) {
                    try {
                        isBlockedRepository.setUnblocked(foundAccount.get().getId());
                        // обработка транзакации повторно
                        OperationInfoAbstractDto operationInfoAbstractDto = new OperationInfoAbstractDto();

                        operationInfoAbstractDto.setOperation(foundTransaction.get().getType());
                        operationInfoAbstractDto.setAmount(foundTransaction.get().getAmount());
                        operationInfoAbstractDto.setClientId(foundAccount.get().getClientId());
                        operationInfoAbstractDto.setCardType(foundAccount.get().getType());

                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Check", "Bearer "); // Генерация или получение JWT-токена

                        HttpEntity<OperationInfoAbstractDto> requestEntity = new HttpEntity<>(operationInfoAbstractDto, headers);

                        // Отправляем запрос на разблокировку счета
                        ResponseEntity<String> response = restTemplate.exchange(
                                "http://localhost:8080/kafka/createTransaction",
                                HttpMethod.POST,
                                requestEntity,
                                String.class
                        );

                        return true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
      return true;
    }

    public void block(Long id) {
        isBlockedRepository.setBlocked(id);
    }

    public ResponseEntity<?> blockOperation(Long clientId) {
        Optional<Account> foundAccount = accountRepository.findByClientIdAndType(clientId, TypeEnum.DEBET_TYPE);
        if (foundAccount.isPresent()) {
            isBlockedRepository.setBlocked(foundAccount.get().getId());
            log.info("Дебетовый счет заблокирован");
            return ResponseEntity.ok().build();
        }
        else {
            log.info("Дебетового счета нет");
            return ResponseEntity.badRequest().build();
        }
    }
}



