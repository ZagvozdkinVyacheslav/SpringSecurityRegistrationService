package org.example.springsecuritywithmongandredis.security.dto

data class UserDetails (
    val phoneNumber: String,
    val email: String,
    val role: String
)
