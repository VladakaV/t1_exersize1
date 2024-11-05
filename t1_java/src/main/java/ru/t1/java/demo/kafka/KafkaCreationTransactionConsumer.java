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
import ru.t1.java.demo.service.impl.TransactionProcessingServiceImpl;


@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaCreationTransactionConsumer {

    private final TransactionProcessingServiceImpl transactionProcessingService;

    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "t1_demo_client_transactions",
            containerFactory = "kafkaListenerContainerFactoryOperation")
    public void listener(@Payload OperationInfoAbstractDto transactionInfo,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        try {

            if (transactionInfo.getOperation() == OperationEnum.CANCEL) {
                transactionProcessingService.cancelOperation(transactionInfo);
            }
            else {
                transactionProcessingService.processTransaction(transactionInfo);
            }


        } finally {

            ack.acknowledge();

        }


        log.debug("Operation consumer: записи обработаны");
    }
}
