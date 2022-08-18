package distributed.lock.repository

import org.springframework.integration.support.locks.LockRegistry

interface LockRepository {

    fun getLockRegistry(): LockRegistry
}