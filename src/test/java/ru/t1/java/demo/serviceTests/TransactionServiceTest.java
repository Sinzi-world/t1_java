package ru.t1.java.demo.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;


    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(1L)
                .transactionAmount(BigDecimal.valueOf(100))
                .status(TransactionStatus.ACCEPTED)
                .transactionTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAllTransactions() {

        List<Transaction> transactions = List.of(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction, result.get(0));
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testCreateTransaction() {

        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(testTransaction);

        assertNotNull(result);
        assertEquals(testTransaction, result);
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    void testUpdateTransactionStatus() {

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        TransactionStatus newStatus = TransactionStatus.CANCELLED;

        transactionService.updateTransactionStatus(1L, newStatus);
        assertEquals(newStatus, testTransaction.getStatus());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    void testUpdateTransactionStatusThrowsExceptionWhenTransactionNotFound() {

        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.updateTransactionStatus(1L, TransactionStatus.REQUESTED));
        assertEquals("Transaction not found", exception.getMessage());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).save(any());
    }
}
