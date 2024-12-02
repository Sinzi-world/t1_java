package ru.t1.java.demo.intefrationTests;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
public class TransactionControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateTransaction() throws Exception {

        stubFor(post(urlEqualTo("/kafka/transactions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setTransactionAmount(BigDecimal.valueOf(100));
        transactionDto.setTransactionTime(LocalDateTime.now());
        transactionDto.setTransactionStatus(TransactionStatus.REQUESTED);

        HttpEntity<TransactionDto> request = new HttpEntity<>(transactionDto);

        ResponseEntity<String> response = restTemplate.exchange(
                "/transactions", HttpMethod.POST, request, String.class);

        assertEquals(201, response.getStatusCodeValue());
        verify(postRequestedFor(urlEqualTo("/kafka/transactions")));
    }

    @Test
    void testGetAllTransactions() {

        stubFor(get(urlEqualTo("/transactions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withStatus(200)));

        ResponseEntity<String> response = restTemplate.getForEntity("/transactions", String.class);

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("[]", response.getBody());
        verify(getRequestedFor(urlEqualTo("/transactions")));
    }
}
