package distributed.lock.repository

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.integration.redis.util.RedisLockRegistry
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.stereotype.Component

@Component
class RedisLockRepository : LockRepository {

    override fun getLockRegistry(): LockRegistry {
        //TODO : Errors
        val jedisConnectionFactory = JedisConnectionFactory()
        jedisConnectionFactory.hostName = "localhost"
        jedisConnectionFactory.port = 6379
        jedisConnectionFactory.afterPropertiesSet()
        return RedisLockRegistry(jedisConnectionFactory, "lock-key")
    }
}