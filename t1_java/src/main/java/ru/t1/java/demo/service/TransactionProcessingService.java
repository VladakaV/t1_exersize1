package ru.t1.java.demo.service;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import ru.t1.java.demo.model.dto.OperationInfoAbstractDto;

public interface TransactionProcessingService {

    public void processTransaction(OperationInfoAbstractDto transaction);

    public void cancelOperation(OperationInfoAbstractDto transaction);

    public boolean unblock(Long id);

    public void block(Long id);

    public ResponseEntity<?> blockOperation(Long clientId);
}
