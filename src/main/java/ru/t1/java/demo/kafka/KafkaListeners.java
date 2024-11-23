package ru.t1.java.demo.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;

@Slf4j
@Data
@Component
public class KafkaListeners {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;

    @KafkaListener(topics = "${t1.kafka.topic.accounts}", groupId = "${t1.kafka.consumer.group-id}")
    public void listenAccount(AccountDto accountDto) {
        log.info("Received Account message: {}", accountDto);

        Client client = clientRepository.findById(accountDto.getClientId()).orElseThrow(() -> new RuntimeException("Client not found"));

        Account account = Account.builder()
                .clientId(client)
                .accountType(AccountType.valueOf(accountDto.getAccountType()))
                .balance(accountDto.getBalance())
                .build();

        accountRepository.save(account);
        log.info("Saved Account to DB: {}", account);
    }

    // Слушаем топик t1_demo_transactions
    @KafkaListener(topics = "${t1.kafka.topic.transactions}", groupId = "${t1.kafka.consumer.group-id}")
    public void listenTransaction(TransactionDto transactionDto) {
        log.info("Received Transaction message: {}", transactionDto);

        Account account = accountRepository.findById(transactionDto.getAccountId()).orElseThrow(() -> new RuntimeException("Account not found"));

        Transaction transaction = Transaction.builder()
                .accountId(account)
                .transactionAmount(transactionDto.getTransactionAmount())
                .transactionTime(transactionDto.getTransactionTime())
                .build();

        transactionRepository.save(transaction);
        log.info("Saved Transaction to DB: {}", transaction);
    }
}
