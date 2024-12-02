package ru.t1.java.demo.serviceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void testGetAccountByIdSuccess() {

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Optional<Account> account = accountService.getAccountById(1L);

        assertTrue(account.isPresent());
        assertEquals(1L, account.get().getId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void testGetAccountByIdNotFound() {

        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Account> account = accountService.getAccountById(2L);
        assertTrue(account.isEmpty());

        verify(accountRepository).findById(2L);
    }

    @Test
    void testUpdateAccount() {

        when(accountRepository.save(testAccount)).thenReturn(testAccount);

        accountService.updateAccount(testAccount);

        verify(accountRepository).save(testAccount);
    }

    @Test
    void testUpdateAccountBalanceSuccess() {
        // Given
        BigDecimal newBalance = BigDecimal.valueOf(2000);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        accountService.updateAccountBalance(1L, newBalance);

        // Then
        assertEquals(newBalance, testAccount.getBalance());
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void testUpdateAccountBalanceAccountNotFound() {

        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                accountService.updateAccountBalance(2L, BigDecimal.valueOf(2000))
        );
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(2L);
        verify(accountRepository, never()).save(any());
    }

}
