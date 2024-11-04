package ru.t1.java.demo.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {


    private final DataGenerator dataGenerator;

    @Override
    public void run(String... args) throws Exception {
        dataGenerator.generateData(10, 5, 20); // Генерация 10 клиентов, 5 аккаунтов на клиента и 20 транзакций на аккаунт
    }
}

