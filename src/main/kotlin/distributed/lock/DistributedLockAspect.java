package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DistributedLockAspect {

    // 30 seconds
    private static final long LOCK_TIMEOUT = 30000;

    @Autowired
    private RedisTemplate<String, Boolean> redisTemplate;

    protected boolean acquireLock(String cacheKey) {
        System.out.println("Acquiring lock for key: " + cacheKey);

        Boolean currentLockValue = this.redisTemplate.boundValueOps(cacheKey).get();
        System.out.println("Current value for lock '" + cacheKey + "': " + currentLockValue);

        if (currentLockValue) {
            throw new RuntimeException("Lock already acquired");
        }

        // TODO: Figure out timeout
        return true;
    }

    protected boolean releaseLock(String cacheKey) {
        System.out.println("Releasing lock for '" + cacheKey);
        Boolean currentLockValue = this.redisTemplate.boundValueOps(cacheKey).getAndDelete();
        return true;
    }

//    @Around(value = "@annotation(distributedLock)", argNames = "proceedingJoinPoint,distributedLock")
    public Object doUnderLock(ProceedingJoinPoint proceedingJoinPoint, String key) throws Throwable {
        String cacheKey = evalCacheKeyOriginal(proceedingJoinPoint, key);

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

    private String evalCacheKeyOriginal(ProceedingJoinPoint proceedingJoinPoint, String key) {
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("args", proceedingJoinPoint.getArgs());

        return parser.parseExpression(key).getValue(context).toString();
    }

}