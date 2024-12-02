package ru.t1.java.demo.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.TransactionAcceptMessageDto;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;

@Slf4j
@Data
@Component
public class KafkaListeners {

    @Value("${t1.kafka.topic.accept_transactions}")
    private String acceptTransactionsTopic;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final KafkaProducer kafkaProducer;

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

    @KafkaListener(topics = "${t1.kafka.topic.transactions}", groupId = "${t1.kafka.consumer.group-id}")
    public void listen(Transaction transaction) {
        log.info("Получено сообщение о транзакции: {}", transaction);

        Account account = transaction.getAccountId();
        if (account.getAccountStatus() == AccountStatus.OPEN) {
            transaction.setStatus(TransactionStatus.REQUESTED);
            transactionService.createTransaction(transaction);

            BigDecimal newBalance = account.getBalance().add(transaction.getTransactionAmount());
            accountService.updateAccountBalance(account.getId(), newBalance);

            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.REQUESTED);
        }
    }
}
