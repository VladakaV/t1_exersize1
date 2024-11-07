package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.CreationAccountDto;
import ru.t1.java.demo.model.enums.TypeEnum;

import java.util.Optional;

public interface AccountService {

    Account saveAccount(Account account);

    public Optional<Account> findByClientIdAndType(Long clientId, TypeEnum type);

    public boolean createAccount(CreationAccountDto creationAccountDto);

    public Optional<Account> findById(int id);
}
