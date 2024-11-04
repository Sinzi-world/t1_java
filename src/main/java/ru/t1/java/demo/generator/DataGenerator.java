package ru.t1.java.demo.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final Random random = new Random();



    public void generateData(int numberOfClients, int numberOfAccounts, int numberOfTransactions) {
        List<Client> clients = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            Client client = new Client();
            client.setFirstName("FirstName" + i);
            client.setLastName("LastName" + i);
            client.setMiddleName("MiddleName" + i);
            clients.add(client);
        }
        clientRepository.saveAll(clients);

        for (Client client : clients) {
            for (int j = 0; j < numberOfAccounts; j++) {
                Account account = new Account();
                account.setClientId(client);
                account.setAccountType(random.nextBoolean() ? AccountType.DEBIT : AccountType.CREDIT);
                account.setBalance(BigDecimal.valueOf(random.nextDouble() * 10000));
                accountRepository.save(account);
            }
        }

        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            for (int k = 0; k < numberOfTransactions; k++) {
                Transaction transaction = new Transaction();
                transaction.setAccountId(account);
                transaction.setTransactionAmount(BigDecimal.valueOf(random.nextDouble() * 1000));
                transaction.setTransactionTime(LocalDateTime.now().minusDays(random.nextInt(30))); // случайное время за последний месяц
                transactionRepository.save(transaction);
            }
        }
    }
}
