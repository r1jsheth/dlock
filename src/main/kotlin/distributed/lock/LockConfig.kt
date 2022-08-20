package distributed.lock

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.integration.redis.util.RedisLockRegistry
import org.springframework.integration.support.locks.LockRegistry

@Configuration
class LockConfig  {

    @Bean
    @Qualifier("redis")
    fun getLockRegistry(): LockRegistry {
        val jedisConnectionFactory = JedisConnectionFactory()
        jedisConnectionFactory.afterPropertiesSet()
        return RedisLockRegistry(jedisConnectionFactory, "lock-key")
    }
}