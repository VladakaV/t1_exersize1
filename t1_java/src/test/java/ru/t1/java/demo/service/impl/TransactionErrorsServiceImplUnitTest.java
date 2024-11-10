package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.model.ErrorTransaction;
import ru.t1.java.demo.repository.ErrorTransactionRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionErrorsServiceImplUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ErrorTransactionRepository errorTransactionRepository;

    @InjectMocks
    private TransactionErrorsServiceImpl transactionErrorsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendAboutUnblock_whenResponseIsSuccessful_deletesErrorTransaction() {
        Long transactionId = 123L;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = ResponseEntity.ok("Success");
        when(restTemplate.exchange(
                "http://localhost:8080/kafka/unblock/" + transactionId,
                HttpMethod.POST,
                requestEntity,
                String.class
        )).thenReturn(response);

        ErrorTransaction errorTransaction = new ErrorTransaction();
        errorTransaction.setTransactionId(transactionId);
        when(errorTransactionRepository.findById(transactionId)).thenReturn(Optional.of(errorTransaction));

        transactionErrorsService.sendAboutUnblock(transactionId);

        verify(errorTransactionRepository, times(1)).delete(errorTransaction);
    }

    @Test
    void testSendAboutUnblock_whenResponseIsNotSuccessful_savesErrorTransaction() {
        Long transactionId = 123L;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = ResponseEntity.status(500).body("Error");
        when(restTemplate.exchange(
                "http://localhost:8080/kafka/unblock/" + transactionId,
                HttpMethod.POST,
                requestEntity,
                String.class
        )).thenReturn(response);

        when(errorTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        transactionErrorsService.sendAboutUnblock(transactionId);

        ArgumentCaptor<ErrorTransaction> captor = ArgumentCaptor.forClass(ErrorTransaction.class);
        verify(errorTransactionRepository, times(1)).save(captor.capture());
        assertEquals(transactionId, captor.getValue().getTransactionId());
    }

    @Test
    void testSendAboutUnblock_whenTransactionNotFound_doesNotSaveErrorTransaction() {
        Long transactionId = 123L;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = ResponseEntity.status(500).body("Error");
        when(restTemplate.exchange(
                "http://localhost:8080/kafka/unblock/" + transactionId,
                HttpMethod.POST,
                requestEntity,
                String.class
        )).thenReturn(response);

        when(errorTransactionRepository.findById(transactionId)).thenReturn(Optional.of(new ErrorTransaction()));

        transactionErrorsService.sendAboutUnblock(transactionId);

        verify(errorTransactionRepository, never()).save(any(ErrorTransaction.class));
    }
}

