package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.kafka.KafkaErrorTransactionsProducer;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.model.enums.OperationEnum;
import ru.t1.java.demo.model.enums.TypeEnum;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.IsBlockedRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionProcessingServiceImplUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IsBlockedRepository isBlockedRepository;

    @Mock
    private KafkaErrorTransactionsProducer kafkaErrorTransactionsProducer;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionProcessingServiceImpl transactionProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Инициализация моков
    }

    @Test
    void testProcessTransaction_whenAccountIsNotBlocked_processesTransaction() {
        Long clientId = 1L;
        TypeEnum cardType = TypeEnum.CREDIT_TYPE;
        BigDecimal amount = new BigDecimal("100");
        OperationInfoAbstractDto transactionInfo = new OperationInfoAbstractDto();
        transactionInfo.setClientId(clientId);
        transactionInfo.setCardType(cardType);
        transactionInfo.setAmount(amount);
        transactionInfo.setOperation(OperationEnum.MINUS);

        Account account = new Account();
        account.setId(1L);
        account.setClientId(clientId);
        account.setType(TypeEnum.DEBET_TYPE);
        account.setBalance(new BigDecimal("200"));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setClientId(clientId);
        transaction.setAccountId(account.getId());
        transaction.setType(OperationEnum.MINUS);

        IsBlocked isBlocked = new IsBlocked();
        isBlocked.setBlocked(false);

        when(accountRepository.findByClientIdAndType(clientId, cardType)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(isBlockedRepository.findByAccountId(account.getId())).thenReturn(Optional.of(isBlocked));

        transactionProcessingService.processTransaction(transactionInfo);

        verify(accountRepository, times(1)).updateAccountBalanceById(account.getId(), account.getBalance().subtract(amount));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessTransaction_whenAccountIsBlocked_sendsErrorMessage() {
        Long clientId = 1L;
        TypeEnum cardType = TypeEnum.CREDIT_TYPE;
        BigDecimal amount = new BigDecimal("100");
        OperationInfoAbstractDto transactionInfo = new OperationInfoAbstractDto();
        transactionInfo.setClientId(clientId);
        transactionInfo.setCardType(cardType);
        transactionInfo.setAmount(amount);
        transactionInfo.setOperation(OperationEnum.MINUS);

        Account account = new Account();
        account.setId(1L);
        account.setClientId(clientId);
        account.setType(TypeEnum.DEBET_TYPE);
        account.setBalance(new BigDecimal("200"));

        IsBlocked isBlocked = new IsBlocked();
        isBlocked.setBlocked(true);

        when(accountRepository.findByClientIdAndType(clientId, cardType)).thenReturn(Optional.of(account));
        when(isBlockedRepository.findByAccountId(account.getId())).thenReturn(Optional.of(isBlocked));

        transactionProcessingService.processTransaction(transactionInfo);

        verify(kafkaErrorTransactionsProducer, times(1)).send(anyLong());
    }

    @Test
    void testUnblock_whenTransactionIsValid_unblocksAccountAndResendsTransaction() {
        Long transactionId = 1L;
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100");
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("200"));
        account.setClientId(1L);
        account.setType(TypeEnum.CREDIT_TYPE);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAmount(amount);
        transaction.setAccountId(accountId);
        transaction.setType(OperationEnum.MINUS);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(accountId.intValue())).thenReturn(Optional.of(account));
        doNothing().when(isBlockedRepository).setUnblocked(accountId);

        OperationInfoAbstractDto operationInfo = new OperationInfoAbstractDto();
        operationInfo.setOperation(transaction.getType());
        operationInfo.setAmount(transaction.getAmount());
        operationInfo.setClientId(account.getClientId());
        operationInfo.setCardType(account.getType());

        ResponseEntity<String> response = ResponseEntity.ok("Success");
        when(restTemplate.exchange(
                eq("http://localhost:8080/kafka/createTransaction"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        boolean result = transactionProcessingService.unblock(transactionId);

        assertTrue(result);
        verify(isBlockedRepository, times(1)).setUnblocked(accountId);
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testUnblock_whenTransactionIsInvalid_doesNotUnblockAccount() {
        Long transactionId = 1L;
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100");
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("50"));
        account.setClientId(1L);
        account.setType(TypeEnum.CREDIT_TYPE);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAmount(amount);
        transaction.setAccountId(accountId);
        transaction.setType(OperationEnum.MINUS);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(accountId.intValue())).thenReturn(Optional.of(account));

        boolean result = transactionProcessingService.unblock(transactionId);

        assertFalse(result);
        verify(isBlockedRepository, never()).setUnblocked(accountId);
        verify(restTemplate, never()).exchange(anyString(), any(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testBlockOperation_whenAccountIsFound_blocksAccount() {
        Long clientId = 1L;
        Account account = new Account();
        account.setId(1L);
        account.setClientId(clientId);
        account.setType(TypeEnum.DEBET_TYPE);

        when(accountRepository.findByClientIdAndType(clientId, TypeEnum.DEBET_TYPE)).thenReturn(Optional.of(account));

        ResponseEntity<?> response = transactionProcessingService.blockOperation(clientId);

        assertEquals(200, response.getStatusCodeValue());
        verify(isBlockedRepository, times(1)).setBlocked(account.getId());
    }

    @Test
    void testBlockOperation_whenAccountNotFound_returnsBadRequest() {
        Long clientId = 1L;

        when(accountRepository.findByClientIdAndType(clientId, TypeEnum.DEBET_TYPE)).thenReturn(Optional.empty());

        ResponseEntity<?> response = transactionProcessingService.blockOperation(clientId);

        assertEquals(400, response.getStatusCodeValue());
        verify(isBlockedRepository, never()).setBlocked(anyLong());
    }
}

