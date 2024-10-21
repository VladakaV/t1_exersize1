package ru.t1.java.demo.aop;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.t1.java.demo.model.Client;

import java.sql.SQLException;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Aspect
@Component
@Transactional(propagation = NOT_SUPPORTED)
@RequiredArgsConstructor
public class TransactionAspect {

    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    @Pointcut("@annotation(ru.t1.java.demo.aop.Transaction)")
    public void join() {

    }

    @Around("@annotation(ru.t1.java.demo.aop.Transaction)")
    public void wrapMethod(final ProceedingJoinPoint joinPoint) throws Throwable {

        var transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {

            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Client) {
                    Client client = (Client) arg;
                    entityManager.persist(client);
                }
            }

            transactionManager.commit(transactionStatus);
        }  catch (Exception exception){
            transactionManager.rollback(transactionStatus);
        }


//        transactionTemplate.execute(status -> {
//            try {
//                Object proceed = joinPoint.proceed();
//                transactionManager.commit(status);
//                return proceed;
//            } catch (Throwable e) {
//                transactionManager.rollback(status);
//                throw new RuntimeException(e);
//            } finally {
//                status.flush();
//            }
//        });


    }

}
