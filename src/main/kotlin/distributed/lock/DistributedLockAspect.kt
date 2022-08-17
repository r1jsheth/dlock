package distributed.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
class DistributedLockAspect {

    @Pointcut("@annotation(distributed.lock.DistributedLock)")
    fun distributedLock() {
    }

    private val lockRegistry: LockRegistry

    init {
        lockRegistry = RedisLockRepository().lockRegistry
    }

    @Throws(DistributedLockException::class, InterruptedException::class)
    protected fun acquireLock(cacheKey: String, lockTimeout: Int) {
        val lock = lockRegistry.obtain(cacheKey)
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
    @Throws(Throwable::class)
    fun doUnderLock(pjp: ProceedingJoinPoint): Any {

        var cacheKey : String = getLockKey(pjp)

        val timeOut = getTimeOut(pjp)

        cacheKey = getKey(pjp)

        // is there a annotation : method.getParameterAnnotations()[i].length > 0
        // is any of the annotation of type :  method.getParameterAnnotations()[2][j].annotationType().name.split("\\.",0)[last element] == KeyVariable
        // pjp.getArgs[i]

        //method.getParameterAnnotations()[2][1] instanceof KeyVariable
        val returnVal: Any

        returnVal = try {
            acquireLock(cacheKey, timeOut)
            pjp.proceed()
        } catch (e: DistributedLockException) {
            e.printStackTrace()
            throw e
        } catch (throwable: Throwable) {
            // Something unexpected happened in the method (pjp)
            throwable.printStackTrace()
            throw throwable
        } finally {
            //            releaseLock(cacheKey);
        }
        return returnVal
    }


    private fun getKey(pjp: ProceedingJoinPoint) : String {

        val args : Array<Any> = pjp.args

        val className: Class<*> = pjp.target.javaClass

        val methodName = pjp.signature.name

        val argClass = (pjp.signature as MethodSignature).parameterTypes

        val method = className.getMethod(methodName, *argClass)

        var parameterAnnotations: Array<Array<Annotation>> = method.parameterAnnotations

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

    private fun getLockKey(pjp: ProceedingJoinPoint): String {
        val signature = pjp.signature as MethodSignature
        return signature.method.getAnnotation(DistributedLock::class.java).key
    }

    private fun getTimeOut(pjp: ProceedingJoinPoint): Int {
        val signature = pjp.signature as MethodSignature
        return signature.method.getAnnotation(DistributedLock::class.java).timeout
    }
}