package distributed.lock

import org.redisson.Redisson
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit


@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class LockApplication

fun main(args: Array<String>) {
	runApplication<LockApplication>(*args)
}

const val REDIS_PORT = 6379;

@RestController
class MainController {

	@DistributedLock(key = "users", timeout = 10, timeoutUnit = TimeUnit.SECONDS)
	@GetMapping("/users")
	fun diThis(): String {
		println("just starting the fun - 1")
		println("starting the fun - 2")
		try {

//			val config = Config()
//			config.useClusterServers()
//				.addNodeAddress("redis://127.0.0.1:$REDIS_PORT")
//
//			val redissonClient = Redisson.create()
//			var buckets = redissonClient.buckets;
//			println("bucektsss:::$buckets")
//
//			var keys = redissonClient.getKeys();
//
//			var allKeys = keys.getKeys();
//
//			for (key in allKeys) {
//				println("key::::$key")
//			}
//			val redisTemplate: RedisTemplate<String, Boolean> = getRedisTemplate()
			println("just checkkk")

		} catch (e: Exception) {
			println("WOW, something bad::: $e")
		}


		return "Hello, World!";
	}
}