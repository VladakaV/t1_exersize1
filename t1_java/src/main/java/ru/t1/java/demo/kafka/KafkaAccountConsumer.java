package ru.t1.java.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.mapper.AccountMapperImplem;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

@Slf4j
@Component
public class KafkaAccountConsumer {


    private final AccountService accountService;

    private final AccountMapperImplem accountMapper;

    @Autowired
    public KafkaAccountConsumer(AccountService accountService, AccountMapperImplem accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @KafkaListener(groupId = "${t1.kafka.consumer.group-id}",
            topics = "t1_demo_accounts",
            containerFactory = "kafkaListenerContainerFactoryAccount"
          )
    public void listenToAccount(@Payload AccountDto accountDto,
                                Acknowledgment ack,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Account consumer: Обработка сообщения о счете");

        try {
            Account account = accountMapper.toEntity(accountDto);

            accountService.saveAccount(account);

        } finally {

            ack.acknowledge();
        }

        log.debug("Account consumer: запись обработана");
    }
}
