package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.model.enums.OperationEnum;
import ru.t1.java.demo.service.impl.TransactionErrorsServiceImpl;
import ru.t1.java.demo.service.impl.TransactionProcessingServiceImpl;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaErrorTransactionsConsumer {
    private final TransactionErrorsServiceImpl transactionErrorsServiceImpl;

    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "t1_demo_client_transaction_errors",
            containerFactory = "kafkaListenerContainerFactoryString")
    public void listener(@Payload String id,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        try {

            transactionErrorsServiceImpl.sendAboutUnblock(Long.valueOf(id));

        } finally {

            ack.acknowledge();

        }


        log.debug("Errors consumer: записи обработаны");
    }
}
