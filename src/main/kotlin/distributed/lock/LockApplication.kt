package distributed.lock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class LockApplication

fun main(args: Array<String>) {
	runApplication<LockApplication>(*args)
}

@RequestMapping("/dt")
@RestController()
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

	@GetMapping("asd")
	fun asas() {

	}

	@DistributedLock(clazz = MainController::class)
	@GetMapping("/test/{v1}/{v2}/{v3}")
	fun test(@PathVariable v1: String, @PathVariable v2: String, @PathVariable @KeyVariable v3 : String) : String {
		return "Check"
	}
}