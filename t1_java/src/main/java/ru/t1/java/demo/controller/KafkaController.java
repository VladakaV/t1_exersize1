package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.model.dto.TransactionDto;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaController {
    private  final KafkaClientProducer<ClientDto> kafkaClientProducer;
    private  final KafkaAccountProducer kafkaAccountProducer;
    private  final KafkaTransactionProducer kafkaTransactionProducer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ClientDto clientDto) {
        kafkaClientProducer.sendTo("t1_demo_client_registration", clientDto);
        return ResponseEntity.ok("Message sent to Kafka topic");
    }

    @PostMapping("/sendAccount")
    public ResponseEntity<String> sendMessageAccount(@RequestBody AccountDto accountDto) {
        kafkaAccountProducer.send(accountDto);
        return ResponseEntity.ok("Message sent to Kafka topic about account");
    }

    @PostMapping("/sendTransaction")
    public ResponseEntity<String> sendMessageTransaction(@RequestBody TransactionDto transactionDto) {
        kafkaTransactionProducer.send(transactionDto);
        return ResponseEntity.ok("Message sent to Kafka topic about transaction");
    }
}
