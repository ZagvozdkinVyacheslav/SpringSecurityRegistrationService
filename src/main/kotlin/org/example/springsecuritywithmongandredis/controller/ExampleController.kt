package org.example.springsecuritywithmongandredis.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/docs")
class ExampleController {

    @GetMapping("/doc")
    fun getDoc(
        @RequestParam("id") id: Long
    ){
        println("поиск в базе документа")
    }


    @PostMapping("/doc")
    fun addDoc(){

    }
}
