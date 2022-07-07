package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.util.concurrent.TimeUnit;


public class DistributedLockAspect {

    // TODO: Configure-able via annotation
    private static final long LOCK_TIMEOUT = 30;
    // TODO: Configure-able via annotation
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final String CUSTOM_LOCK_KEY = "CUSTOM_KEY";

    @Autowired
    private RedisTemplate<String, Boolean> redisTemplate;

    protected boolean acquireLock(String cacheKey) {
        System.out.println("Acquiring lock for key: " + cacheKey);

        Boolean currentLockValue = this.redisTemplate.boundValueOps(cacheKey).get();
        System.out.println("Current value for lock '" + cacheKey + "': " + currentLockValue);

        if (currentLockValue) {
            throw new RuntimeException("Lock already acquired");
        }

        redisTemplate.boundValueOps(cacheKey).set(true, LOCK_TIMEOUT, TIMEOUT_UNIT);
        return true;
    }

    protected boolean releaseLock(String cacheKey) {
        System.out.println("Releasing lock for '" + cacheKey);
        Boolean currentLockValue = this.redisTemplate.boundValueOps(cacheKey).getAndDelete();
        return currentLockValue;
    }

//    @Around(value = "@annotation(distributedLock)", argNames = "proceedingJoinPoint,distributedLock")
    public Object doUnderLock(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String cacheKey = getLockKey(proceedingJoinPoint);

        acquireLock(cacheKey);

        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            System.out.println("Proceeding failed: " + throwable);
            throw throwable;
        } finally {
            releaseLock(cacheKey);
        }
    }

    private String getLockKey(ProceedingJoinPoint proceedingJoinPoint) {
        return proceedingJoinPoint.getArgs().length > 0 ? proceedingJoinPoint.getArgs()[0].toString() : CUSTOM_LOCK_KEY;

    }

}