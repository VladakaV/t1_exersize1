package ru.t1.java.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.ReactiveOffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.CreationAccountDto;
import ru.t1.java.demo.model.enums.TypeEnum;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account saveAccount(Account account) {
        Account saved = accountRepository.save(account);
        return saved;
    }

    public Optional<Account> findByClientIdAndType(Long clientId, TypeEnum type) {
        return accountRepository.findByClientIdAndType(clientId, type);
    }

    public boolean createAccount(CreationAccountDto creationAccountDto) {
        Optional<Account> foundAccount = accountRepository.findByClientIdAndType(creationAccountDto.getClientId(),
                creationAccountDto.getType());

        if (foundAccount.isPresent()) {
            return false;
        }
        else {
            Account account = new Account();

            account.setClientId(creationAccountDto.getClientId());
            account.setType(creationAccountDto.getType());
            account.setBalance(BigDecimal.ZERO);

            accountRepository.save(account);

            return true;
        }
    }
}
