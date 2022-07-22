package distributed.lock

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

@RestController
class MainController {

	public fun getKey(): String {
		return "wow this is working";
	}

	@DistributedLock(key = "users", clazz = MainController::class)
	@GetMapping("/users")
	fun doThis(): String {
		val keyValue: String = "abc";
		println("just starting the fun - 1")
		println("starting the fun - 2")

		return "Hello, World!";
	}
}