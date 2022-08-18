package distributed.lock

import distributed.lock.repository.RedisLockRepository
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock

@Aspect
@Component
class DistributedLockAspect (private val redisLockRepository: RedisLockRepository) {

    private val lockRegistry: LockRegistry = redisLockRepository.getLockRegistry()

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    fun distributedLock() {

    }

    protected fun acquireLock(cacheKey: String, lockTimeout: Int) {

        val lock : Lock = lockRegistry.obtain(cacheKey)

        val isLockAcquired = lock.tryLock(lockTimeout.toLong(), TimeUnit.SECONDS)

        println("Lock acquired for key: `$cacheKey`:$isLockAcquired")

        if (!isLockAcquired) {
            throw DistributedLockException("Lock is not available for key:$cacheKey")
        }
    }

    protected fun releaseLock(cacheKey: String) {

        println("Releasing lock for `$cacheKey`")

        val lock = lockRegistry.obtain(cacheKey)

        lock.unlock()
    }

    @Around("distributedLock()")
    fun doUnderLock(pjp: ProceedingJoinPoint): Any {

        val timeOut = getTimeOut(pjp)

        val cacheKey = getKey(pjp)

        try {

            acquireLock(cacheKey, timeOut)

            val pjpReturn = pjp.proceed()

            releaseLock(cacheKey)

            return pjpReturn

        } catch (e: DistributedLockException) {
            throw e
        } catch (e : Exception) {
            releaseLock(cacheKey)
            throw e
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

        for(i in 1..args.size) {

            for(annotation in parameterAnnotations[i]) {

                if (!(annotation is KeyVariable)) {
                    continue
                }

                return args[i] as String

            }
        }

        throw Exception("KeyVariable Not Provided")
    }

    private fun getTimeOut(pjp: ProceedingJoinPoint): Int {

        val signature = pjp.signature as MethodSignature

        return signature.method.getAnnotation(DistributedLock::class.java).timeout
    }
}