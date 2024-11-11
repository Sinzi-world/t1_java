package ru.t1.java.demo.aop;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.util.Arrays;


@Data
@Aspect
@Component
@Slf4j
public class LogDataSourceErrorAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSourceErrorLogRepository errorLogRepository;

    @Value("${t1.kafka.topic.metrics}")
    private String metricsTopic;

    public LogDataSourceErrorAspect(KafkaTemplate<String, String> kafkaTemplate,
                                    DataSourceErrorLogRepository errorLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.errorLogRepository = errorLogRepository;
    }


    @Around("@annotation(LogDataSourceError)")
    public Object logErrorAndSendToKafka(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            log.error("Ошибка при выполнении метода: {}", ex.getMessage(), ex);
            sendErrorToKafka(ex, joinPoint);
            saveErrorToDatabase(ex, joinPoint);
            throw ex;
        }
    }

    private void sendErrorToKafka(Exception ex, ProceedingJoinPoint joinPoint) {
        try {
            String message = String.format(
                    "Error in method: %s, Message: %s, StackTrace: %s",
                    joinPoint.getSignature().getName(),
                    ex.getMessage(),
                    Arrays.toString(ex.getStackTrace())
            );
            Message<String> kafkaMessage = MessageBuilder.withPayload(message)
                    .setHeader("errorType", "DATA_SOURCE")
                    .build();
            kafkaTemplate.send(metricsTopic, String.valueOf(kafkaMessage)).get();
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение в Kafka: {}", e.getMessage(), e);
        }
    }

    private void saveErrorToDatabase(Exception ex, ProceedingJoinPoint joinPoint) {
        DataSourceErrorLog errorLog = new DataSourceErrorLog(
                Arrays.toString(ex.getStackTrace()),
                ex.getMessage(),
                joinPoint.getSignature().toString()
        );
        errorLogRepository.save(errorLog);
    }
}


