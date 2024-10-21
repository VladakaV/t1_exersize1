package ru.t1.java.demo.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseWebClient {

    @Value("${integration.retry-count}")
    private Integer retryCount;
    @Value("${integration.retry-backoff}")
    private Integer retryBackoff;

    private final WebClient webClient;

    public <T, R> ResponseEntity<R> post(Function<UriBuilder, URI> function, T request, Class<R> clazz) {
        try {
            WebClient.RequestHeadersSpec<?> requestHeadersSpec = webClient.post()
                    .uri(function)
                    .body(BodyInserters.fromValue(request))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.ALL);
            ResponseEntity<R> responseEntity = requestHeadersSpec
                    .retrieve()
                    .toEntity(clazz)
                    .retryWhen(Retry.fixedDelay(retryCount, Duration.ofMillis(retryBackoff))
                            .filter(this::isRequestTimeout))
                    .block();
            return responseEntity;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRequestTimeout(Throwable throwable) {
        return (throwable instanceof WebClientResponseException) &&
               (!((WebClientResponseException) throwable).getStatusCode().equals(HttpStatus.REQUEST_TIMEOUT));
    }


}
