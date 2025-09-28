package org.example.springsecuritywithmongandredis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringSecurityWithMongoAndRedisApplication

fun main(args: Array<String>) {
    runApplication<SpringSecurityWithMongoAndRedisApplication>(*args)
}
