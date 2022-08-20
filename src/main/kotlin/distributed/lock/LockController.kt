package distributed.lock

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Executors

@RequestMapping("/lock")
@RestController()
class MainController (
        private val component: LockComponent
) {

    private val executorService = Executors.newFixedThreadPool(3)
    @GetMapping("/test")
    fun test() : String {
        for (i in 1..3) {
            executorService.execute { component.initFunction() }
        }
        return "Check"
    }
}

