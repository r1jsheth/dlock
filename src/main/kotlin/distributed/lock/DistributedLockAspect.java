package distributed.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


@Aspect
@Component
public class DistributedLockAspect {

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    public void distributedLock() {}


    private final LockRegistry lockRegistry;

    public DistributedLockAspect () {
        this.lockRegistry = new RedisLockRepository().getLockRegistry();
    }

    protected void acquireLock(String cacheKey, int lockTimeout) throws DistributedLockException, InterruptedException {
        Lock lock = lockRegistry.obtain(cacheKey);
        boolean isLockAcquired = lock.tryLock(lockTimeout, TimeUnit.SECONDS);
        System.out.println("Lock acquired for key: `" + cacheKey + "`:" + isLockAcquired);
        if (!isLockAcquired) {
            throw new DistributedLockException("Lock is not available for key:" + cacheKey);
        }
    }

    protected void releaseLock(String cacheKey) {
        System.out.println("Releasing lock for `" + cacheKey + "`");
        Lock lock = lockRegistry.obtain(cacheKey);
        lock.unlock();
    }

    @Around("distributedLock()")
    public Object doUnderLock(ProceedingJoinPoint pjp) throws Throwable {
        String cacheKey = getLockKey(pjp);
        int timeOut = getTimeOut(pjp);

        Object[] args = pjp.getArgs();
        Class<?> className = pjp.getTarget().getClass();
        String methodName = pjp.getSignature().getName();
        Class<?>[] argClass = ((MethodSignature) pjp.getSignature()).getParameterTypes();
        Method method = className.getMethod(methodName, argClass);
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        String key = "" ;


        // is there a annotation : method.getParameterAnnotations()[i].length > 0
        // is any of the annotation of type :  method.getParameterAnnotations()[2][j].annotationType().name.split("\\.",0)[last element] == KeyVariable
        // pjp.getArgs[i]

        //method.getParameterAnnotations()[2][1] instanceof KeyVariable


        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method any = signature.getMethod().getAnnotation(DistributedLock.class).clazz().getMethod("getKey");
        System.out.println("----- here -----" + any.invoke(signature.getMethod().getAnnotation(DistributedLock.class).clazz().newInstance()));

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
