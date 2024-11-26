package ru.t1.java.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionAcceptMessageDto {
    private Long accountId;
    private Long transactionId;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private BigDecimal balance;
}

