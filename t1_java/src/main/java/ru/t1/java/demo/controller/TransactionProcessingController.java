package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.kafka.KafkaCreationTransactionProducer;
import ru.t1.java.demo.model.dto.CreationAccountDto;
import ru.t1.java.demo.model.dto.MessageResponse;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;
import ru.t1.java.demo.service.TransactionProcessingService;
import ru.t1.java.demo.service.impl.AccountServiceImpl;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingController {
    private final KafkaCreationTransactionProducer kafkaCreationTransactionProducer;
    private final TransactionProcessingService transactionProcessingService;
    private final AccountServiceImpl accountService;

    @PostMapping("/createTransaction")
    public ResponseEntity<?> createTransaction(@RequestBody OperationInfoAbstractDto operationInfo) {
        String message = kafkaCreationTransactionProducer.send(operationInfo);
        return ResponseEntity.ok(new MessageResponse(message));
    }
    // пример входных данных JSON на эндпоинт выше
    // для операции отмена:
//     {
//        "client_id" : 1,
//        "operation" : "CANCEL",
//        "card_type" : "DEBET_TYPE"
//    }

    // для остальных операций:
//    {
//            "client_id" : 1,
//            "operation" : "PLUS",
//            "card_type" : "DEBET_TYPE",
//            "amount" : 34343
//    }

    @PostMapping("/unblock/{id}")
    public ResponseEntity<?> unblock(@PathVariable Long id) {
        boolean response = transactionProcessingService.unblock(id);
        if (response) {
            return ResponseEntity.ok(new MessageResponse("Счёт разблокирован и транзакция обработана."));
        } else {
            return ResponseEntity.ok(new MessageResponse("Не удалось разблокировать счёт."));
        }
    }

    @PostMapping("/block/{clientId}")
    public ResponseEntity<?> block(@PathVariable Long clientId) {
        ResponseEntity<?> response = transactionProcessingService.blockOperation(clientId);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(new MessageResponse("Счет заблокирован"));
        }
        else {
            return ResponseEntity.badRequest().body((new MessageResponse("Не удалось заблокировать счет")));
        }
    }

    @PostMapping("/createAccount")
    public ResponseEntity<?> createAccount(@RequestBody CreationAccountDto creationAccountDto) {
        boolean isCreated = accountService.createAccount(creationAccountDto);
        if (isCreated == true) {
            return ResponseEntity.ok(new MessageResponse("Account created"));
        }
        else {
            return ResponseEntity.ok(new MessageResponse("Account already exists"));
        }
    }
}
