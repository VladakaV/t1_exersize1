package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.KafkaTimeProducer;
import ru.t1.java.demo.model.TimeLimitExceedLog;
import ru.t1.java.demo.service.impl.TimeLimitExceedLogImpl;

import java.util.Arrays;

@Aspect
@Component
public class TimeLoggingAspect {

    @Value("${track.time-limit-exceed}")
    private long timeLimit;

    private TimeLimitExceedLogImpl timeLimitExceedLog;
    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public TimeLoggingAspect(TimeLimitExceedLogImpl timeLimitExceedLog,
                             KafkaTemplate kafkaTemplate) {
        this.timeLimitExceedLog = timeLimitExceedLog;
        this.kafkaTemplate = kafkaTemplate;
    }

    //можно проверить post запросом на /admin
    @Around("@annotation(ru.t1.java.demo.annotations.TrackExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > timeLimit) {

//            TimeLimitExceedLog log = new TimeLimitExceedLog();
//
//            log.setMethodSignature(joinPoint.getSignature().toString());
//            log.setExecutionTime(executionTime);
//
//            timeLimitExceedLog.saveTimeLog(log);

            String methodName = joinPoint.getSignature().toString();
            Object[] methodArgs = joinPoint.getArgs();
            String params = methodArgs.length > 0 ? Arrays.toString(methodArgs) : "No parameters";

            String message = String.format("Method: %s, Execution Time: %d ms, Parameters: %s",
                    methodName, executionTime, params);

//            kafkaTimeProducer.send(message);
            kafkaTemplate.send("t1_demo_metric_trace", message);

        }

        return result;
    }
}
