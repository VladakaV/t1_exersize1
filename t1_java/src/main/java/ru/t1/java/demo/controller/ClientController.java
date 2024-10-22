package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.annotations.TrackExecutionTime;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LoggableException;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.model.enums.Metrics;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.MetricService;
import ru.t1.java.demo.util.ClientMapper;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final KafkaClientProducer kafkaClientProducer;
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final MetricService metricService;

    @Value("${t1.kafka.topic.client_registration}")
    private String topic;

    @HandlingResult
    @GetMapping(value = "/parse")
    @LoggableException
    public void parseSource() {
        clientRepository.save(Client.builder()
                .firstName("John42")
                .build());
        clientRepository.findClientByFirstName("John42");
        metricService.incrementByName(Metrics.CLIENT_CONTROLLER_REQUEST_COUNT.getValue());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }

    @GetMapping("/register")
    public ResponseEntity<Client> register(@RequestBody ClientDto clientDto) {
        log.info("Registering client: {}", clientDto);
        Client client = clientService.registerClient(
                clientMapper.toEntityWithId(clientDto)
        );
//        log.info("Client registered: {}", client.getId());
        return ResponseEntity.ok().body(client);
    }


    @PostMapping("/admin")
    @TrackExecutionTime
    public void oneSecond() {
        try {
            Thread.sleep(1500);
        }
        catch (Exception e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }


}
