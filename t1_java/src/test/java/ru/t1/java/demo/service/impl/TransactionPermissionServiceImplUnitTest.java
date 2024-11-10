package ru.t1.java.demo.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.T1JavaDemoApplication;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = T1JavaDemoApplication.class)
public class TransactionPermissionServiceImplUnitTest {

    @Autowired
    private WebClient webClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8080));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        WireMock.stubFor(get(urlPathMatching("/transaction/check/\\d+"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"allowed\": true}")));
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testTransactionPermission() {
        webClient.get()
                .uri("http://localhost:8080/transaction/check/1")
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> System.out.println("Request finished"))
                .subscribe(response -> System.out.println("Response: " + response));
    }
}
