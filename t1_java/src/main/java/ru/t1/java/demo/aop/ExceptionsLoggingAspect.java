package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.service.impl.DataSourceErrorLogImpl;

import java.util.Arrays;

@Aspect
@Component
public class ExceptionsLoggingAspect {

    @Autowired
    private DataSourceErrorLogImpl dataSourceErrorLog;

    @Around("@annotation(ru.t1.java.demo.annotations.ExceptionsAnnotation)")
    public Object logExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            System.out.println("Тестирование аспекта");
            return joinPoint.proceed();

        } catch (Exception e) {
            System.out.println("Тестирование аспекта, отловил ошибку");
            DataSourceErrorLog log = new DataSourceErrorLog();

            log.setStackTrace(Arrays.toString(e.getStackTrace()));
            log.setMessage(e.getMessage());
            log.setMethodSignature(joinPoint.getSignature().toString());

            dataSourceErrorLog.saveLog(log);

            throw e;
        }
    }
}
