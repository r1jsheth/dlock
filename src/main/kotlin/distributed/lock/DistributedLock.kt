package distributed.lock

@Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val waitingTime: Long = 30
)