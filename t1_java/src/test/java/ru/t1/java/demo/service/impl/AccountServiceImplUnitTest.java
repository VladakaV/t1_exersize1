package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.CreationAccountDto;
import ru.t1.java.demo.model.enums.TypeEnum;
import ru.t1.java.demo.repository.AccountRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceImplUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount_whenAccountNotExist_returnsTrue() {
        CreationAccountDto creationAccountDto = new CreationAccountDto();
        creationAccountDto.setClientId(1L);
        creationAccountDto.setType(TypeEnum.CREDIT_TYPE);

        when(accountRepository.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(new Account());

        boolean result = accountService.createAccount(creationAccountDto);

        assertTrue(result);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_whenAccountExists_returnsFalse() {
        CreationAccountDto creationAccountDto = new CreationAccountDto();
        creationAccountDto.setClientId(1L);
        creationAccountDto.setType(TypeEnum.CREDIT_TYPE);

        Account existingAccount = new Account();
        existingAccount.setClientId(1L);
        existingAccount.setType(TypeEnum.CREDIT_TYPE);

        when(accountRepository.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE)).thenReturn(Optional.of(existingAccount));

        boolean result = accountService.createAccount(creationAccountDto);

        assertFalse(result);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testFindByClientIdAndType_whenAccountExists_returnsAccount() {
        // Arrange
        Account account = new Account();
        account.setClientId(1L);
        account.setType(TypeEnum.CREDIT_TYPE);

        when(accountRepository.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE);

        assertTrue(result.isPresent());
        assertEquals(account, result.get());
    }

    @Test
    void testFindByClientIdAndType_whenAccountDoesNotExist_returnsEmpty() {
        when(accountRepository.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.findByClientIdAndType(1L, TypeEnum.CREDIT_TYPE);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindById_whenAccountExists_returnsAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setClientId(1L);
        account.setType(TypeEnum.CREDIT_TYPE);

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(account, result.get());
    }

    @Test
    void testFindById_whenAccountDoesNotExist_returnsEmpty() {
        when(accountRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.findById(1);

        assertFalse(result.isPresent());
    }
}
