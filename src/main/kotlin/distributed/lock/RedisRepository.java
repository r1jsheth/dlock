package distributed.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisRepository {
    @Bean
    Jedis jedisConnectionFactory() {
        // TODO: Figure out the config
        Jedis jedis = new Jedis("localhost", 6379);
        return jedis;
    }

}
