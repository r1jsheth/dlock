package distributed.lock

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LockComponent (
        private val lockComponentLocked: LockComponentLocked
        ) {

    private val log = LoggerFactory.getLogger(LockComponent::class.java)

    fun initFunction() {
        try {
            val returnType = this.lockComponentLocked.testLock("lockKey", "2", "3")
            log.info("returnType = $returnType")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

@DistributedLock
@Component
class LockComponentLocked {

    private val log = LoggerFactory.getLogger(LockComponentLocked::class.java)

    fun testLock(@KeyVariable v1: String, v2: String, v3: String) {
        log.info("Starting the function")
        Thread.sleep(5000)
        log.info("Exiting the function")
        throw TestException()
    }
}