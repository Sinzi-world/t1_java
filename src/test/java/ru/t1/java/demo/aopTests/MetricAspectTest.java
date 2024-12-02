package ru.t1.java.demo.aopTests;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.aop.MetricAspect;
import ru.t1.java.demo.kafka.KafkaProducer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricAspectTest {

    @InjectMocks
    private MetricAspect metricAspect;

    @Mock
    private KafkaProducer kafkaProducer;

    @Test
    void testMethodExecutionTimeExceeded() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Metric metric = mock(Metric.class);

        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("testMethod()");

        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(150);
            return "Success";
        });
        when(metric.value()).thenReturn(100L);

        metricAspect.measureMethodExecution(joinPoint, metric);
        verify(kafkaProducer).sendMessage(eq("your-topic-name"), contains("превысил время выполнения"));
    }


    @Test
    void testMethodExecutionThrowsException() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Metric metric = mock(Metric.class);

        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("testMethod()");

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test Exception"));
        when(metric.value()).thenReturn(100L);

        Throwable thrown = assertThrows(RuntimeException.class, () -> metricAspect.measureMethodExecution(joinPoint, metric));
        assertEquals("Test Exception", thrown.getMessage());
        verify(kafkaProducer).sendMessage(eq("your-topic-name"), contains("завершился с ошибкой: Test Exception"));
    }

}
