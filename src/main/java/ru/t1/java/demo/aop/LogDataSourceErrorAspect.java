package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Aspect
@Component
public class LogDataSourceErrorAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    @Value("${t1.kafka.topic.errors}")
    private String errorTopic;

    public LogDataSourceErrorAspect(KafkaTemplate<String, String> kafkaTemplate, DataSourceErrorLogRepository dataSourceErrorLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.dataSourceErrorLogRepository = dataSourceErrorLogRepository;
    }

    @Around("@annotation(LogDataSourceError)")
    public Object logErrorAndSendToKafka(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            log.error("Ошибка при выполнении метода: {}", ex.getMessage(), ex);
            boolean kafkaFailed = !sendErrorToKafka(ex, joinPoint);
            if (kafkaFailed) {
                saveErrorToDatabase(ex, joinPoint);
            }
            throw ex;
        }
    }

    private boolean sendErrorToKafka(Exception ex, ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().toShortString();
            String message = String.format("Ошибка в методе %s: %s", methodName, ex.getMessage());
            kafkaTemplate.send(errorTopic, message);
            return true; // Kafka отправка успешна
        } catch (Exception kafkaEx) {
            log.error("Не удалось отправить ошибку в Kafka: {}", kafkaEx.getMessage(), kafkaEx);
            return false; // Kafka отправка провалилась
        }
    }

    private void saveErrorToDatabase(Exception ex, ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(getStackTraceAsString(ex))
                .message(ex.getMessage())
                .methodSignature(methodName)
                .build();
        dataSourceErrorLogRepository.save(errorLog);
        log.info("Ошибка записана в базу данных: {}", errorLog);
    }

    private String getStackTraceAsString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}



