package ru.t1.java.demo.aopTests;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.aop.LogDataSourceErrorAspect;
import ru.t1.java.demo.kafka.KafkaProducer;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogDataSourceErrorAspectTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @InjectMocks
    private LogDataSourceErrorAspect logDataSourceErrorAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.toShortString()).thenReturn("mockMethod()");
    }

    @Test
    void testMethodExecutesSuccessfully() throws Throwable {

        when(joinPoint.proceed()).thenReturn("success");

        Object result = logDataSourceErrorAspect.logErrorAndSendToKafka(joinPoint);

        assertEquals("success", result);
        verify(kafkaProducer, never()).sendMessage(anyString(), anyString());
        verify(dataSourceErrorLogRepository, never()).save(any());
    }

    @Test
    void testErrorSentToDatabaseWhenKafkaFails() throws Throwable {

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test Exception"));
        doThrow(new RuntimeException("Kafka Exception")).when(kafkaProducer).sendMessage(anyString(), anyString());

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                logDataSourceErrorAspect.logErrorAndSendToKafka(joinPoint));

        assertEquals("Test Exception", thrown.getMessage());
        verify(dataSourceErrorLogRepository).save(argThat(log ->
                log.getMessage().equals("Test Exception") &&
                        log.getMethodSignature().equals("mockMethod()")
        ));
    }

    @Test
    void testErrorSentToKafka() throws Throwable {

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test Exception"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                logDataSourceErrorAspect.logErrorAndSendToKafka(joinPoint));

        assertEquals("Test Exception", thrown.getMessage());
        verify(kafkaProducer).sendMessage(eq("your-error-topic"), contains("Ошибка в методе mockMethod()"));
        verify(dataSourceErrorLogRepository, never()).save(any());
    }
}
