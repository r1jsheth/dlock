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

	@DistributedLock(key = "users")
	@GetMapping("/users")
	fun diThis(): String {
		println("just starting the fun - 1")
		println("starting the fun - 2")

		return "Hello, World!";
	}
}