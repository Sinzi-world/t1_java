package ru.t1.java.demo.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.TransactionAcceptMessageDto;

import java.math.BigDecimal;

@Slf4j
@Data
@Service
public class KafkaTransactionService {

    @Value("${t1.kafka.topic.accept_transactions}")
    private String acceptTransactionsTopic;

    private final KafkaProducer kafkaProducer;


    public void sendTransactionAcceptMessage(Account account, Transaction transaction, BigDecimal newBalance) {
        TransactionAcceptMessageDto message = new TransactionAcceptMessageDto(
                account.getClientId().getId(),
                account.getId(),
                transaction.getTransactionTime(),
                transaction.getTransactionAmount(),
                newBalance
        );
        kafkaProducer.sendMessage(acceptTransactionsTopic, message);
        log.info("Отправлено сообщение в топик t1_demo_transaction_accept: {}", message);
    }
}

