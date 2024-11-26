package ru.t1.java.demo.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.enums.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.model.enums.AccountStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final Random random = new Random();

    public void generateData(int numberOfClients, int numberOfAccounts, int numberOfTransactions) {
        // Создание клиентов
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            Client client = new Client();
            client.setFirstName("FirstName" + i);
            client.setLastName("LastName" + i);
            client.setMiddleName("MiddleName" + i);
            client.setClientId(UUID.randomUUID());
            clients.add(client);
        }
        clientRepository.saveAll(clients);

        // Создание аккаунтов
        List<Account> accounts = new ArrayList<>();
        for (Client client : clients) {
            for (int j = 0; j < numberOfAccounts; j++) {
                Account account = new Account();
                account.setAccountId(UUID.randomUUID());
                account.setClientId(client);
                account.setAccountType(random.nextBoolean() ? AccountType.DEBIT : AccountType.CREDIT);
                account.setAccountStatus(random.nextBoolean() ? AccountStatus.OPEN : AccountStatus.CLOSED);
                account.setBalance(BigDecimal.valueOf(random.nextDouble() * 10000));
                account.setFrozenAmount(BigDecimal.valueOf(random.nextDouble() * 10000));
                accounts.add(account);
            }
        }
        accountRepository.saveAll(accounts);

        // Создание транзакций
        List<Transaction> transactions = new ArrayList<>();
        for (Account account : accounts) {
            for (int k = 0; k < numberOfTransactions; k++) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(UUID.randomUUID());
                transaction.setStatus(random.nextBoolean() ? TransactionStatus.ACCEPTED : TransactionStatus.REJECTED);
                transaction.setAccountId(account);
                transaction.setTransactionAmount(BigDecimal.valueOf(random.nextDouble() * 1000));
                transaction.setTransactionTime(LocalDateTime.now().minusDays(random.nextInt(30)));
                transactions.add(transaction);
            }
        }
        transactionRepository.saveAll(transactions);
    }
}


