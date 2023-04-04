package ca.aversa.insessionservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HealthController {

    @GetMapping("/health")
    fun healthCheck(): String {
        return "Healthy"
    }
}