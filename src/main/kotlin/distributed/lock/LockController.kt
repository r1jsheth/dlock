package distributed.lock

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/lock")
@RestController()
class MainController {

    @DistributedLock()
    @GetMapping("/test/{v1}/{v2}/{v3}")
    fun test(@PathVariable v1: String, @PathVariable v2: String, @PathVariable @KeyVariable v3 : String) : String {
        println("Function Runs!!!")
        return "Check"
    }
}