package ru.t1.java.demo.service.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.t1.java.demo.service.TransactionPermissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
public class TransactionPermissionServiceImplTest {

    @Autowired
    private TransactionPermissionService transactionPermissionService;

    @Test
    public void testTransactionPermission() {
        WireMock.stubFor(get(urlPathMatching("/transaction/check/\\d+"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"allowed\": true}")));

        boolean result = transactionPermissionService.checkTransactionPermission(1L);
        assertThat(result).isTrue();
    }
}
