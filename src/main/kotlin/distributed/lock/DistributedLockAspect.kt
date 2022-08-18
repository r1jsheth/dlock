package distributed.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock

@Aspect
@Component
class DistributedLockAspect (
        @Qualifier("redis") private val lockRegistry: LockRegistry
) {

    private val log = LoggerFactory.getLogger(DistributedLockAspect::class.java)

    @Pointcut("within(@distributed.lock.DistributedLock *)")
    fun distributedLock() {
    }

    @Pointcut("execution(public * *(..))")
    fun publicMethod() {
    }

    protected fun releaseLock(cacheKey: String) {

        log.info("Releasing lock for `$cacheKey`")

        val lock = lockRegistry.obtain(cacheKey)

        lock.unlock()
    }

    @Around("publicMethod() && distributedLock()")
    fun doUnderLock(pjp: ProceedingJoinPoint): Any? {
        var lock : Lock? = null
        var lockAcquired = false
        try {
            val timeOut = getTimeOut(pjp)

            val cacheKey = getKey(pjp)

            log.info("Trying to Acquire Lock for $cacheKey")

            lock = lockRegistry.obtain(cacheKey)

            lockAcquired = lock.tryLock(timeOut.toLong(), TimeUnit.SECONDS)

            if (!lockAcquired) {
                throw Exception("Lock is not available for key:$cacheKey")
            }
            log.info("Successfully Acquired Lock")

            try {
                return pjp.proceed()
            } catch (e: Exception) {
                throw DistributedProxyException(e)
            }
        } catch (e: DistributedProxyException) {
            throw e.cause ?: e
        } catch (e : java.lang.Exception) {

            log.info("Failed to Acquire Lock ${e.message}")

            throw DistributedLockException("Failed to Acquire Lock ${e.message}")
        } finally {
            log.info("Releasing lock for")
            if (lockAcquired)
                lock?.unlock()
        }
    }


    private fun getKey(pjp: ProceedingJoinPoint) : String {

        val args : Array<Any> = pjp.args

        val className: Class<*> = pjp.target.javaClass

        val methodName = pjp.signature.name

        val argClass = (pjp.signature as MethodSignature).parameterTypes

        val method = className.getMethod(methodName, *argClass)

        val parameterAnnotations: Array<Array<Annotation>> = method.parameterAnnotations

        assert(args.size == parameterAnnotations.size)

        for(i in args.indices) {
            for(annotation in parameterAnnotations[i]) {
                if (annotation is KeyVariable) {
                    return args[i] as String
                }
            }
        }

        throw DistributedLockException("KeyVariable Not Provided")
    }

    private fun getTimeOut(pjp: ProceedingJoinPoint): Int {

        return pjp.target.javaClass.getAnnotation(DistributedLock::class.java).timeout
    }
}