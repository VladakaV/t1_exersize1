//package ru.t1.java.demo.kafka;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//import ru.t1.java.demo.model.Account;
//import ru.t1.java.demo.model.Client;
//import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
//import ru.t1.java.demo.model.dto.TransactionDto;
//import ru.t1.java.demo.service.impl.AccountServiceImpl;
//import ru.t1.java.demo.service.impl.ClientServiceImpl;
//
//import java.util.Optional;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class KafkaCreationTransactionProducer {
//
//    private final KafkaTemplate<String, OperationInfoAbstractDto> template;
//    @Autowired
//    private  ClientServiceImpl clientService;
//    @Autowired
//    private AccountServiceImpl accountService;
//
//    public String send(OperationInfoAbstractDto o) {
//
//        try {
//            Client foundClient = clientService.getClientById(o.getClientId());
//            Optional<Account> foundAccount = accountService.findByClientIdAndType(o.getClientId(), o.getCardType());
//            if (foundClient != null && foundAccount.isPresent()) {
//                template.send("t1_demo_client_transactions", o).get();
//                return "Message sent to Kafka topic about operation";
//
//            }
//            else if (foundClient == null) {
//                return "Client not found";
//            }
//            else {
//                return "There is no card type at this account";
//            }
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
//        return "Message sent to Kafka topic about operation";
//    }
//
//
//}

package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.TransactionAllowResponse;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.service.impl.AccountServiceImpl;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaCreationTransactionProducer {

    private final KafkaTemplate<String, OperationInfoAbstractDto> template;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ClientServiceImpl clientService;

    @Autowired
    private AccountServiceImpl accountService;

    private boolean checkTransactionPermission(Long clientId) {
        String url = "http://localhost:8089/transaction/check/" + clientId;
        try {
            TransactionAllowResponse response = restTemplate.getForObject(url, TransactionAllowResponse.class);
            return response != null && response.isAllowed();
        } catch (Exception e) {
            log.error("Error checking transaction permission for client {}: {}", clientId, e.getMessage());
            return false;
        }
    }

    public String send(OperationInfoAbstractDto o) {
        try {
            Client foundClient = clientService.getClientById(o.getClientId());
            Optional<Account> foundAccount = accountService.findByClientIdAndType(o.getClientId(), o.getCardType());

            if (foundClient == null) {
                return "Client not found";
            }

            if (foundAccount.isEmpty()) {
                return "There is no card type at this account";
            }

            // Проверка разрешения через WireMock-заглушку
            if (!checkTransactionPermission(o.getClientId())) {
                return "Transaction is not allowed";
            }

            // Отправка сообщения в Kafka при успешной проверке
            template.send("t1_demo_client_transactions", o).get();
            return "Message sent to Kafka topic about operation";

        } catch (Exception ex) {
            log.error("Error sending message to Kafka: {}", ex.getMessage(), ex);
            return "Failed to send message";
        } finally {
            template.flush();
        }
    }
}

