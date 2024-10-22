package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.mapper.TransactionMapperImplem;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionService transactionService;
    private final TransactionMapperImplem transactionMapper;

    @KafkaListener(
        groupId = "${t1.kafka.consumer.group-id}",
        topics = "t1_demo_transactions",
        containerFactory = "kafkaListenerContainerFactoryTransaction"
        )
    public void listenToTransaction(@Payload TransactionDto transactionDto,
                            Acknowledgment ack,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

    try {

        Transaction transaction = transactionMapper.toEntity(transactionDto);

        System.out.println(transaction);

        transactionService.saveTransaction(transaction);
    } finally {

        ack.acknowledge();
    }

    log.debug("Transaction consumer: запись обработана");
    }
}
