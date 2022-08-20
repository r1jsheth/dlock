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

    @Around("publicMethod() && distributedLock()")
    fun doUnderLock(pjp: ProceedingJoinPoint): Any? {

        var lock : Lock? = null
        var lockAcquired = false

        try {
            val waitingTime = getWaitingTime(pjp)
            val key = getKey(pjp)

            log.info("Trying to Acquire Lock for $key")
            lock = lockRegistry.obtain(key)
            lockAcquired = lock.tryLock(waitingTime, TimeUnit.SECONDS)
            if (!lockAcquired) {
                throw Exception("Lock is not available for key:$key")
            }
            log.info("Successfully Acquired Lock")

            return runProceedingJoinPoint(pjp)

        } catch (e: DistributedProxyException) {

            log.info("Error: PJP")
            throw e.cause ?: e

        } catch (e : Exception) {

            log.info("Error: Failed to Acquire Lock ${e.message}")
            throw DistributedLockException("Failed to Acquire Lock ${e.message}")

        } finally {
            if (lockAcquired) {
                log.info("Releasing lock")
                lock?.unlock()
            }
        }
    }

    private fun runProceedingJoinPoint(pjp: ProceedingJoinPoint) : Any? {
        try {
            return pjp.proceed()
        } catch (e: Exception) {
            throw DistributedProxyException(e)
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

                if ((annotation is KeyVariable) && (args[i] is String) && (args[i] as String).isNotEmpty()) {

                    return args[i] as String
                }
            }
        }

        throw Exception("Invalid or Empty KeyVariable")
    }

    private fun getWaitingTime(pjp: ProceedingJoinPoint): Long {

        return pjp.target.javaClass.getAnnotation(DistributedLock::class.java).waitingTime
    }
}