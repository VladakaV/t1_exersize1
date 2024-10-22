package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.ClientDto;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountProducer {

    private final KafkaTemplate<String, AccountDto> template;

    public void send(AccountDto o) {
        try {

            template.send("t1_demo_accounts", o).get();

        } catch (Exception ex) {

            log.error(ex.getMessage(), ex);

        } finally {

            template.flush();

        }
    }

}
