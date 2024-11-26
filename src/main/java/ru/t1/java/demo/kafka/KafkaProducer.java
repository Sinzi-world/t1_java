package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object message) {
        try {
            log.info("Отправка сообщения в топик {}: {}", topic, message);
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение в топик {}: {}", topic, e.getMessage(), e);
        }
    }
}

