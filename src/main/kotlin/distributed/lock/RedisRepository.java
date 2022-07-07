package distributed.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisRepository {
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory
                = new JedisConnectionFactory();
        // TODO: Add url and port to environment
        jedisConFactory.setHostName("localhost");
        jedisConFactory.setPort(6379);
        return jedisConFactory;
    }


    @Bean
    public RedisTemplate<String, Boolean> getRedisTemplate() {
        System.out.println("initiating new redis template::: `getRedisTemplate`");
        RedisTemplate<String, Boolean> template = new RedisTemplate<String, Boolean>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
