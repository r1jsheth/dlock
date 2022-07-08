package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;


@Aspect
@Component
public class DistributedLockAspect {

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    public void distributedLock() {}

    @Autowired
    private Jedis jedis;

    protected void acquireLock(String cacheKey, int lockTimeout) throws DistributedLockException {
        String currentLockValue = jedis.get(cacheKey);
        System.out.println("Current value for lock '" + cacheKey + "': " + currentLockValue);

        // TODO: remove true as string, problem jedis.set() expects only string as a second param
        if (currentLockValue != null && currentLockValue.equals("true")) {
            String message = "Lock already acquired for (" + cacheKey + "). Locked by another request";
            throw new DistributedLockException(message);
        }

        SetParams params = new SetParams().ex((long) lockTimeout);
        System.out.println("Acquiring lock for key: `" + cacheKey + "`" + "with timeout `" + lockTimeout + "`");
        jedis.set(cacheKey, "true", params);
    }

    protected void releaseLock(String cacheKey) {
        System.out.println("Releasing lock for `" + cacheKey + "`");
        jedis.del(cacheKey);
    }

    @Around("distributedLock()")
    public Object doUnderLock(ProceedingJoinPoint pjp) throws Throwable {
        String cacheKey = getLockKey(pjp);
        int timeOut = getTimeOut(pjp);

        Object returnVal = null;

        try {
            acquireLock(cacheKey, timeOut);
            returnVal = pjp.proceed();
        } catch (DistributedLockException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable throwable) {
            // Something unexpected happened in the method (pjp)
            throwable.printStackTrace();
            throw throwable;
        } finally {
            releaseLock(cacheKey);
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

}