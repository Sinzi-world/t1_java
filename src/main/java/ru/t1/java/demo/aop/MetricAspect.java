package ru.t1.java.demo.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MetricAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${t1.kafka.topic.metrics}")
    private String metricsTopic;

    public MetricAspect(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("@annotation(metric)")
    public Object measureMethodExecution(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("Ошибка выполнения метода {}: {}", joinPoint.getSignature(), ex.getMessage());
            sendMetricToKafka(joinPoint, System.currentTimeMillis() - startTime, true, ex.getMessage());
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > metric.value()) {
                sendMetricToKafka(joinPoint, executionTime, false, null);
            }
        }
    }

    private void sendMetricToKafka(ProceedingJoinPoint joinPoint, long executionTime, boolean isError, String errorMessage) {
        try {
            String methodName = joinPoint.getSignature().toShortString();
            String message = isError
                    ? String.format("Метод %s завершился с ошибкой: %s. Время выполнения: %d мс", methodName, errorMessage, executionTime)
                    : String.format("Метод %s превысил время выполнения: %d мс", methodName, executionTime);
            kafkaTemplate.send(metricsTopic, message);
        } catch (Exception e) {
            log.error("Не удалось отправить метрику в Kafka: {}", e.getMessage(), e);
        }
    }
}

