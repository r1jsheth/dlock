package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistributedLockAspect {

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    public void distributedLock() {}

    @Autowired
    private Jedis jedis;

    protected boolean acquireLock(String cacheKey, int lockTimeout, TimeUnit timeUnit) throws DistributedLockException {
        System.out.println("Acquiring lock for key: " + cacheKey);

        String currentLockValue = jedis.get(cacheKey);

        System.out.println("Current value for lock '" + cacheKey + "': " + currentLockValue);

        if (currentLockValue != null && currentLockValue == "true") {
            String message = "Lock already acquired for (" + cacheKey + "). Locked by another request";
            throw new DistributedLockException(message);
        }

//        redisTemplate.boundValueOps(cacheKey).set(true, lockTimeout, timeUnit);
        jedis.set(cacheKey, "true");
        return true;
    }

    protected boolean releaseLock(String cacheKey) {
        System.out.println("Releasing lock for '" + cacheKey);
        jedis.del(cacheKey);
        return true;
    }

    @Around("distributedLock()")
    public Object doUnderLock(ProceedingJoinPoint pjp) {
        String cacheKey = getLockKey(pjp);
        int timeOut = getTimeOut(pjp);
        TimeUnit timeOutUnit = getTimeUnit(pjp);

        Object returnVal = null;
        boolean isLockAcquired = false;

        try {
            isLockAcquired = acquireLock(cacheKey, timeOut, timeOutUnit);
            returnVal = pjp.proceed();
        } catch (DistributedLockException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (isLockAcquired) {
                releaseLock(cacheKey);
            }
        }
        return returnVal;
    }

    private String getLockKey(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod().getAnnotation(DistributedLock.class).key();
    }

    private int getTimeOut(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod().getAnnotation(DistributedLock.class).timeout();
    }

    private TimeUnit getTimeUnit(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod().getAnnotation(DistributedLock.class).timeoutUnit();
    }

}