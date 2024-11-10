package ru.t1.java.demo.service.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.T1JavaDemoApplication;
import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
classes = {T1JavaDemoApplication.class})
@WireMockTest
public class TransactionPermissionTest {
    //не получилось по итогу настроить совместимость зависимостей, не запустится

    @Autowired
    private WebClient webClient;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @Test
    public void testTransactionPermission() {
        WireMock.stubFor(get(urlPathMatching("/transaction/check/\\d+"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"allowed\": true}")));

        Boolean allowed = webClient.get()
                .uri("/transaction/check/1")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        assertThat(allowed).isTrue();
    }
}
