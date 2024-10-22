package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.ClientDto;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaClientProducer<T extends ClientDto> {

    private final KafkaTemplate<String, ClientDto> template;

    public void send(Long clientId) {
        try {
            template.sendDefault(UUID.randomUUID().toString(), new ClientDto("random", "random", "random")).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }

    public void sendTo(String topic, ClientDto o) {
        try {
            template.send(topic, o).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }

}
