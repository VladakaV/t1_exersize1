package ru.t1.java.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.TimeLimitExceedLog;
import ru.t1.java.demo.service.impl.TimeLimitExceedLogImpl;

@Aspect
@Component
public class TimeLoggingAspect {

    @Value("${track.time-limit-exceed}")
    private long timeLimit;

    private TimeLimitExceedLogImpl timeLimitExceedLog;

    @Autowired
    public TimeLoggingAspect(TimeLimitExceedLogImpl timeLimitExceedLog) {
        this.timeLimitExceedLog = timeLimitExceedLog;
    }

    @Around("@annotation(ru.t1.java.demo.annotations.TrackExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > timeLimit) {

            TimeLimitExceedLog log = new TimeLimitExceedLog();

            log.setMethodSignature(joinPoint.getSignature().toString());
            log.setExecutionTime(executionTime);

            timeLimitExceedLog.saveTimeLog(log);

        }

        return result;
    }
}
