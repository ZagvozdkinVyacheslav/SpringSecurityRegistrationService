package org.example.springsecuritywithmongandredis.controller

import jakarta.validation.Valid
import org.example.springsecuritywithmongandredis.security.dto.AuthUserDto
import org.example.springsecuritywithmongandredis.security.dto.EmailVerifyResponseDto
import org.example.springsecuritywithmongandredis.security.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/")
class AuthController(
    private val authService: AuthService
) {



    @PostMapping("/login")
    fun enterInSystem(
        @RequestBody @Valid
        authUserDto: AuthUserDto
    ): ResponseEntity<AuthUserDto>{
        val user = authService.enterInSystem(authUserDto)
        return user
    }

    @GetMapping("/email_verify")
    fun verifyEmail(
        @RequestParam("email") email: String,
        @RequestParam("verify_code") verifyCode: Int
    ): ResponseEntity<EmailVerifyResponseDto>{
        return authService.verifyEmail(email, verifyCode)
    }

    @GetMapping("user")
    fun getUser(
        @RequestParam("email") email: String
    ): AuthUserDto{
        return authService.getUser(email)
    }


    @PostMapping("/user/default/")
    fun createDefaultUser(
        @RequestBody @Valid
        authUserDto: AuthUserDto
    ): ResponseEntity<AuthUserDto>{
        val user = authService.createUser(authUserDto)
        return ResponseEntity.status(201).body(user)
    }

    @PostMapping("/user/admin/")
    fun createAdminUser(){

    }



}
