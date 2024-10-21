package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Client;

import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Aspect
@Component
@Order(0)
public class LoggingAspect {

    @Pointcut("within(ru.t1.java.demo.*)")
    public void loggingMethods() {

    }

//    @Before("@annotation(LogExecution)")
//    @Order(1)
//    public void logAnnotationBefore(JoinPoint joinPoint) {
//        log.info("ASPECT BEFORE ANNOTATION: Call method: {}", joinPoint.getSignature().getName());
//    }

    @Before("@annotation(HandlingResult)")
    @Order(0)
    public void logBefore(JoinPoint joinPoint) {
        log.info("BEFORE: {}", joinPoint.getSignature().getName());
    }

    @After("@annotation(HandlingResult)")
    @Order(0)
    public void logExceptionAnnotation(JoinPoint joinPoint) {
        log.error("AFTER: {}", joinPoint.getSignature().getName());
    }

    @AfterReturning(
            pointcut = "@annotation(HandlingResult)",
            returning = "result")
    public void handleResult(JoinPoint joinPoint, Object result) {
        log.info("AFTER RETURNING {}", joinPoint.getSignature().toShortString());
    }

    @Around("@annotation(ReplaceResult)")
    @Order(1)
    public Object replace(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("AROUND ADVICE START");


        Client client = Client.builder()
                .build();
        client.setId(42L);

        try {
            Object proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error(throwable.getMessage());
            throw  throwable;
        }

        log.info("AROUND ADVICE END");
        return client;
    }


    @AfterThrowing(pointcut = "@annotation(LoggableException)",
            throwing = "e")
    @Order(0)
    public void handleException(JoinPoint joinPoint, Exception e) {
        log.info("AFTER EXCEPTION {}",
                joinPoint.getSignature().toShortString());
        log.info("Произошла ошибка: ", e);

    }

}
