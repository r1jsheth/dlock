package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;


@Aspect
@Component
public class DistributedLockAspect {

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    public void distributedLock() {}


    private LockRegistry lockRegistry;

    public DistributedLockAspect () {
        this.lockRegistry = new RedisLockRepository().getLockRegistry();
    }

    protected void acquireLock(String cacheKey, int lockTimeout) throws DistributedLockException {
        Lock lock = lockRegistry.obtain(cacheKey);
        boolean isLockAvailable = lock.tryLock();
        System.out.println("Current lock-value for key `" + cacheKey + "`:" + isLockAvailable);
        if (isLockAvailable) {
            System.out.println("Lock is available, now locking...");
            lock.lock();
        }
    }

    protected void releaseLock(String cacheKey) {
        System.out.println("Releasing lock for `" + cacheKey + "`");
        lockRegistry.obtain(cacheKey).unlock();
    }

    @Around("distributedLock()")
    public Object doUnderLock(ProceedingJoinPoint pjp) throws Throwable {
        String cacheKey = getLockKey(pjp);
        int timeOut = getTimeOut(pjp);

        Object returnVal;

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