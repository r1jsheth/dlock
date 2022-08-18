package distributed.lock

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val timeout: Int = 30
)