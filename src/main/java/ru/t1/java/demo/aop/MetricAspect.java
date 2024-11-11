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

import java.util.Arrays;

@Slf4j
@Data
@Aspect
@Component
public class MetricAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${t1.kafka.topic.metrics}")
    private String metricsTopic;

    @Around("@annotation(metric)")
    public Object measureMethodExecution(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        if (executionTime > metric.thresholdMillis()) {
            String methodName = joinPoint.getSignature().getName();
            String parameters = Arrays.toString(joinPoint.getArgs());

            String message = String.format("Method %s executed in %d ms. Parameters: %s",
                    methodName, executionTime, parameters);
            Message<String> kafkaMessage = MessageBuilder.withPayload(message)
                    .setHeader("errorType", "METRICS")
                    .build();
            kafkaTemplate.send(metricsTopic, String.valueOf(kafkaMessage)).get();
            log.info("Sent metrics message: {}", message);
        }
        return result;
    }
}
