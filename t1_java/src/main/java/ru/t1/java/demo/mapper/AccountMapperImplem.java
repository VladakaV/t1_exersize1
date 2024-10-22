package ru.t1.java.demo.mapper;


import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.enums.TypeEnum;

@Component
public class AccountMapperImplem {
    public Account toEntity(AccountDto accountDto) {
        Account account = new Account();

        account.setClient_id(accountDto.getClientId());
        account.setBalance(accountDto.getBalance());

        switch (accountDto.getType()) {
            case DEBET_TYPE -> account.setType(TypeEnum.DEBET_TYPE);
            case CREDIT_TYPE -> account.setType(TypeEnum.CREDIT_TYPE);
        }
        return account;
    }


}
