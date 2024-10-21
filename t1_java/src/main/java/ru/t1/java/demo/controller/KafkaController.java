package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.model.dto.ClientDto;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaClientProducer<ClientDto> kafkaClientProducer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ClientDto clientDto) {
        kafkaClientProducer.sendTo("t1_demo_client_registration", clientDto);
        return ResponseEntity.ok("Message sent to Kafka topic");
    }
}
