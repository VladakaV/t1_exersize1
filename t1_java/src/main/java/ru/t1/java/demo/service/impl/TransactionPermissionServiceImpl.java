package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.service.TransactionPermissionService;

@Service
@RequiredArgsConstructor
public class TransactionPermissionServiceImpl implements TransactionPermissionService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String EXTERNAL_SERVICE_URL = "http://localhost:8080/transaction/check/";

    public boolean checkTransactionPermission(Long transactionId) {
        Boolean allowed = webClientBuilder.baseUrl(EXTERNAL_SERVICE_URL)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("{id}").build(transactionId))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        return allowed != null && allowed;
    }
}

