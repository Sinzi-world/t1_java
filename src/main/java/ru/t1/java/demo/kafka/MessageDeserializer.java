package ru.t1.java.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class MessageDeserializer<T> extends JsonDeserializer<T> {

    private static String getMessage(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            return super.deserialize(topic, headers, data);
        } catch (Exception e) {
            log.error("Произошла ошибка в топике {} при десериализации сообщения {}: {}",
                    topic, getMessage(data), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try{
            return super.deserialize(topic, data);
        } catch (Exception e) {
            log.error("Произошла ошибка в топике {} при десериализации сообщения {}: {}",
            topic, getMessage(data), e.getMessage(), e);
            return null;
        }
    }
}