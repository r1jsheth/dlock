package distributed.lock

import org.redisson.Redisson
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class LockApplication

fun main(args: Array<String>) {
	runApplication<LockApplication>(*args)
}

const val REDIS_PORT = 6379;

@RestController
class MainController {

	@GetMapping("/users")
	fun diThis(): String {
		try {

			val config = Config()
			config.useClusterServers()
				.addNodeAddress("redis://127.0.0.1:$REDIS_PORT")

			val redissonClient = Redisson.create()
			var buckets = redissonClient.buckets;
			println("bucektsss:::$buckets")

			var keys = redissonClient.getKeys();

			var allKeys = keys.getKeys();

			for (key in allKeys) {
				println("key::::$key")
			}


		} catch (e: Exception) {
			println("WOW, something bad::: $e")
		}


		return "Hello, World!";
	}
}