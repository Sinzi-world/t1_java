package ru.t1.java.demo.aop;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Aspect
@Component
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogRepository errorLogRepository;

    @Autowired
    public LogDataSourceErrorAspect(DataSourceErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    // Перехватывает исключения при выполнении любых методов CRUD в репозиториях
    @AfterThrowing(pointcut = "execution(* ru.t1.java.demo.repository.*.*(..))", throwing = "ex")
    public void logDataSourceError(Exception ex) {
        // Создаем новую запись об ошибке
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .message(ex.getMessage())
                .stackTrace(getStackTraceAsString(ex))
                .methodSignature("")
                .build();

        // Сохраняем запись в таблице DataSourceErrorLog
        errorLogRepository.save(errorLog);
    }

    // Преобразование stack trace в строку для сохранения в базе данных
    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
