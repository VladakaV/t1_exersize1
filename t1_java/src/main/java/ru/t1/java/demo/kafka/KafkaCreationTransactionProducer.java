package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.impl.AccountServiceImpl;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaCreationTransactionProducer {

    private final KafkaTemplate<String, OperationInfoAbstractDto> template;
    @Autowired
    private  ClientServiceImpl clientService;
    @Autowired
    private AccountServiceImpl accountService;

    public String send(OperationInfoAbstractDto o) {

        try {
            Client foundClient = clientService.getClientById(o.getClientId());
            Optional<Account> foundAccount = accountService.findByClientIdAndType(o.getClientId(), o.getCardType());
            if (foundClient != null && foundAccount.isPresent()) {
                template.send("t1_demo_client_transactions", o).get();
                return "Message sent to Kafka topic about operation";

            }
            else if (foundClient == null) {
                return "Client not found";
            }
            else {
                return "There is no card type at this account";
            }

        } catch (Exception ex) {

            log.error(ex.getMessage(), ex);

        } finally {

            template.flush();

        }
        return "Message sent to Kafka topic about operation";
    }


}
