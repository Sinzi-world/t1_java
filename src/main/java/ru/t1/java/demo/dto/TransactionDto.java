package ru.t1.java.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private Long transactionId;
    private Long accountId;
    private BigDecimal transactionAmount;
    private TransactionStatus transactionStatus;
    private LocalDateTime transactionTime;
}
