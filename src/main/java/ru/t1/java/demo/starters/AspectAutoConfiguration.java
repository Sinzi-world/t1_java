package ru.t1.java.demo.starters;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.t1.java.demo.aop.LogDataSourceErrorAspect;
import ru.t1.java.demo.aop.MetricAspect;

@Configuration
@ComponentScan(basePackageClasses = {MetricAspect.class, LogDataSourceErrorAspect.class})
public class AspectAutoConfiguration {
}
