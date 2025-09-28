package org.example.springsecuritywithmongandredis.security.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class AuthUserDto(
    @field:NotBlank(message = "Фамилия обязательна")
    @field:Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    val lastname: String,

    @field:NotBlank(message = "Имя обязательно")
    @field:Size(max = 50, message = "Имя не должно превышать 50 символов")
    val firstname: String,

    @field:Size(max = 50, message = "Отчество не должно превышать 50 символов")
    val middlename: String? = null,

    @field:NotNull(message = "ID email обязателен")
    val email: String,

    @field:NotBlank(message = "Номер телефона обязателен")
    val phoneNumber: String,
)