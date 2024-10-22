package ru.t1.java.demo.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.TransactionDto;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTimeProducer {

//    private final KafkaTemplate<String, String> template;
//
//    public void send(String message) {
//        try {
//
//            template.send("t1_demo_metric_trace", message).get();
//
//        } catch (Exception ex) {
//
//            log.error(ex.getMessage(), ex);
//
//        } finally {
//
//            template.flush();
//
//        }
//    }
}
