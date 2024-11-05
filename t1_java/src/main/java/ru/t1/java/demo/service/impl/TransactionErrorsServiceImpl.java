package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.model.ErrorTransaction;
import ru.t1.java.demo.repository.ErrorTransactionRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionErrorsService;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionErrorsServiceImpl implements TransactionErrorsService {
    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;
    private final ErrorTransactionRepository errorTransactionRepository;

    public void sendAboutUnblock(Long transactionId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "); // Генерация или получение JWT-токена

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Отправляем запрос на разблокировку счета
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/kafka/unblock/" + transactionId, // URL для разблокировки счета
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Счёт {} успешно разблокирован.");

            Optional<ErrorTransaction> errorTransaction = errorTransactionRepository.findById(transactionId);
            if (errorTransaction.isPresent()) {

                errorTransactionRepository.delete(errorTransaction.get());
            }

        } else {
            log.warn("Не удалось разблокировать счёт: отказ.");

            Optional<ErrorTransaction> errorTransaction = errorTransactionRepository.findById(transactionId);
            if (!errorTransaction.isPresent()) {
                ErrorTransaction errorTransaction1 = new ErrorTransaction();
                errorTransaction1.setTransactionId(transactionId);
                errorTransactionRepository.save(errorTransaction1);
            }

        }

    }
}
